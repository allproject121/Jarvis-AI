package com.example.data.repository

import com.example.nlp.JarvisIntent
import com.example.planner.ActionPlan
import com.example.planner.RiskLevel
import com.example.planner.Task
import com.google.firebase.Firebase
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

/**
 * Contract for AI-powered multi-step workflow generation from user commands.
 */
interface WorkflowGenerationRepository {
    suspend fun generateWorkflow(
        userCommand: String,
        contextDetails: String? = null
    ): Result<ActionPlan>

    fun generateWorkflowStream(
        userCommand: String,
        contextDetails: String? = null
    ): Flow<String>
}

/**
 * Repository utilizing the Firebase Vertex AI (Firebase AI) SDK to communicate with Gemini
 * for generating complex, ordered, multi-step device automation workflows.
 */
class FirebaseWorkflowRepository(
    private val modelName: String = "gemini-3.5-flash",
    private val firebaseAiProvider: () -> FirebaseAI = { Firebase.ai }
) : WorkflowGenerationRepository {

    companion object {
        private const val SYSTEM_PROMPT_TEXT = """
You are J.A.R.V.I.S., an intelligent phone automation reasoning engine.
Your task is to analyze user voice/text commands and decompose them into an ordered, executable, multi-step automation workflow.

Available Atomic Action Types:
- SYSTEM_SETTING (parameters: "setting" [WIFI, BLUETOOTH, BRIGHTNESS, VOLUME, SILENT_MODE, VIBRATE_MODE], "value")
- LAUNCH_APP (parameters: "packageName", "appName")
- SEND_MESSAGE (parameters: "contact", "platform" [WHATSAPP, SMS], "message")
- MAKE_CALL (parameters: "contact", "phoneNumber")
- SCHEDULE_ALARM (parameters: "time", "label")
- UI_AUTOMATION_SELECT (parameters: "target", "action")
- DOWNLOAD_MEDIA (parameters: "destination")
- UPLOAD_CLOUD (parameters: "folder", "provider")
- LOCK_SCREEN (parameters: "action")
- UNLOCK_SCREEN (parameters: "action")
- NOTIFY_USER (parameters: "message")
- QUERY_AI (parameters: "query")

You MUST respond strictly with a valid JSON object following this schema:
{
  "workflowId": "string",
  "workflowName": "string",
  "summary": "string",
  "intent": "SEND_MESSAGE | MAKE_CALL | OPEN_APP | CHANGE_SETTING | LOCK_UNLOCK_SCREEN | CREATE_AUTOMATION | TURN_ON_MODE | BACKUP_MEDIA | ASK_QUESTION",
  "riskLevel": "LOW | MEDIUM | HIGH | CRITICAL",
  "requiresConfirmation": boolean,
  "confirmationMessage": "string or null",
  "steps": [
    {
      "id": "step_1",
      "name": "string",
      "description": "string",
      "actionType": "string",
      "parameters": { "key": "value" },
      "dependsOn": [],
      "canRunParallel": boolean,
      "riskLevel": "LOW | MEDIUM | HIGH | CRITICAL",
      "estimatedMs": 500
    }
  ]
}
"""
    }

    private val generativeModel by lazy {
        val config = generationConfig {
            temperature = 0.2f
            responseMimeType = "application/json"
        }
        val systemInstruction = content {
            text(SYSTEM_PROMPT_TEXT)
        }
        firebaseAiProvider().generativeModel(
            modelName = modelName,
            generationConfig = config,
            systemInstruction = systemInstruction
        )
    }

    override suspend fun generateWorkflow(
        userCommand: String,
        contextDetails: String?
    ): Result<ActionPlan> = withContext(Dispatchers.IO) {
        try {
            val userPrompt = buildPrompt(userCommand, contextDetails)
            val response = generativeModel.generateContent(userPrompt)
            val responseText = response.text ?: throw IllegalStateException("Received empty response from Gemini")
            val plan = parseWorkflowJson(responseText, userCommand)
            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun generateWorkflowStream(
        userCommand: String,
        contextDetails: String?
    ): Flow<String> = flow {
        val userPrompt = buildPrompt(userCommand, contextDetails)
        generativeModel.generateContentStream(userPrompt).collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }.flowOn(Dispatchers.IO)

    private fun buildPrompt(command: String, contextDetails: String?): String {
        return buildString {
            append("User Automation Command: \"").append(command).append("\"\n")
            if (!contextDetails.isNullOrBlank()) {
                append("Current Device Context:\n").append(contextDetails).append("\n")
            }
            append("Deconstruct this into a safe, sequential, atomic workflow and return valid JSON.")
        }
    }

    internal fun parseWorkflowJson(rawJson: String, originalCommand: String): ActionPlan {
        val cleanedJson = cleanJsonString(rawJson)
        val json = JSONObject(cleanedJson)

        val planId = json.optString("workflowId").ifBlank { "plan_" + UUID.randomUUID().toString().take(8) }
        val intentString = json.optString("intent", "CREATE_AUTOMATION")
        val parsedIntent = parseIntent(intentString)

        val riskLevelString = json.optString("riskLevel", "LOW")
        val riskLevel = parseRiskLevel(riskLevelString)

        val requiresConfirmation = json.optBoolean("requiresConfirmation", riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL)
        val confirmationMessage = json.optString("confirmationMessage").takeIf { it.isNotBlank() }

        val stepsArray = json.optJSONArray("steps")
        val tasks = mutableListOf<Task>()

        if (stepsArray != null) {
            for (i in 0 until stepsArray.length()) {
                val stepObj = stepsArray.getJSONObject(i)
                val taskId = stepObj.optString("id", "${planId}_${i + 1}")
                val name = stepObj.optString("name", "Step ${i + 1}")
                val description = stepObj.optString("description", "")
                val actionType = stepObj.optString("actionType", "SYSTEM_SETTING")

                val paramsMap = mutableMapOf<String, String>()
                val paramsObj = stepObj.optJSONObject("parameters")
                if (paramsObj != null) {
                    val keys = paramsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        paramsMap[key] = paramsObj.optString(key)
                    }
                }

                val dependsOnList = mutableListOf<String>()
                val dependsArray = stepObj.optJSONArray("dependsOn")
                if (dependsArray != null) {
                    for (d in 0 until dependsArray.length()) {
                        dependsOnList.add(dependsArray.getString(d))
                    }
                }

                val stepRisk = parseRiskLevel(stepObj.optString("riskLevel", "LOW"))
                val canParallel = stepObj.optBoolean("canRunParallel", false)
                val estimatedMs = stepObj.optLong("estimatedMs", 500L)

                tasks.add(
                    Task(
                        id = taskId,
                        name = name,
                        description = description,
                        actionType = actionType,
                        parameters = paramsMap,
                        dependsOn = dependsOnList,
                        canRunParallel = canParallel,
                        riskLevel = stepRisk,
                        estimatedMs = estimatedMs
                    )
                )
            }
        }

        if (tasks.isEmpty()) {
            tasks.add(
                Task(
                    id = "${planId}_1",
                    name = json.optString("workflowName", "Automated Task"),
                    description = json.optString("summary", originalCommand),
                    actionType = "QUERY_AI",
                    parameters = mapOf("query" to originalCommand),
                    riskLevel = RiskLevel.LOW
                )
            )
        }

        val totalDuration = tasks.sumOf { it.estimatedMs }

        return ActionPlan(
            id = planId,
            originalIntent = parsedIntent,
            originalText = originalCommand,
            tasks = tasks,
            riskLevel = riskLevel,
            requiresConfirmation = requiresConfirmation,
            confirmationMessage = confirmationMessage,
            estimatedDurationMs = totalDuration
        )
    }

    private fun cleanJsonString(input: String): String {
        var str = input.trim()
        if (str.startsWith("```json")) {
            str = str.substring(7)
        } else if (str.startsWith("```")) {
            str = str.substring(3)
        }
        if (str.endsWith("```")) {
            str = str.substring(0, str.length - 3)
        }
        return str.trim()
    }

    private fun parseIntent(intentStr: String): JarvisIntent {
        return try {
            JarvisIntent.valueOf(intentStr.uppercase())
        } catch (_: Exception) {
            when {
                intentStr.contains("MESSAGE", ignoreCase = true) -> JarvisIntent.SEND_MESSAGE
                intentStr.contains("CALL", ignoreCase = true) -> JarvisIntent.MAKE_CALL
                intentStr.contains("APP", ignoreCase = true) -> JarvisIntent.OPEN_APP
                intentStr.contains("SETTING", ignoreCase = true) -> JarvisIntent.CHANGE_SETTING
                intentStr.contains("MODE", ignoreCase = true) -> JarvisIntent.TURN_ON_MODE
                intentStr.contains("BACKUP", ignoreCase = true) -> JarvisIntent.BACKUP_MEDIA
                intentStr.contains("LOCK", ignoreCase = true) -> JarvisIntent.LOCK_UNLOCK_SCREEN
                else -> JarvisIntent.CREATE_AUTOMATION
            }
        }
    }

    private fun parseRiskLevel(riskStr: String): RiskLevel {
        return try {
            RiskLevel.valueOf(riskStr.uppercase())
        } catch (_: Exception) {
            RiskLevel.LOW
        }
    }
}
