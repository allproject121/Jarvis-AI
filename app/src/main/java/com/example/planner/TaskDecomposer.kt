package com.example.planner

import com.example.data.model.EntityRecord
import com.example.memory.UserContext
import com.example.nlp.JarvisIntent
import com.example.nlp.NLPResult
import java.util.UUID

class TaskDecomposer {

    private val inferenceEngine = ParameterInferenceEngine()
    private val safetyValidator = SafetyValidator()

    fun planWorkflow(
        nlpResult: NLPResult,
        resolvedEntity: EntityRecord?,
        userContext: UserContext
    ): ActionPlan {
        val planId = "plan_" + UUID.randomUUID().toString().take(8)
        val tasks = mutableListOf<Task>()

        when (nlpResult.intent) {
            JarvisIntent.BACKUP_MEDIA -> {
                val params = inferenceEngine.inferParameters("BACKUP_MEDIA", nlpResult.entities, resolvedEntity, userContext)
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Launch Source App",
                        description = "Open Instagram and verify profile session",
                        actionType = "LAUNCH_APP",
                        parameters = mapOf("packageName" to params.getValue("sourcePackage")),
                        riskLevel = RiskLevel.LOW
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_2",
                        name = "Extract Media Feed",
                        description = "Simulate UI navigation to media gallery via Accessibility Service",
                        actionType = "UI_AUTOMATION_SELECT",
                        parameters = mapOf("target" to "media_gallery"),
                        dependsOn = listOf("${planId}_1"),
                        riskLevel = RiskLevel.LOW
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_3",
                        name = "Batch Download Assets",
                        description = "Fetch latest photos to local cache staging area",
                        actionType = "DOWNLOAD_MEDIA",
                        parameters = mapOf("destination" to "/storage/emulated/0/Download/Jarvis"),
                        dependsOn = listOf("${planId}_2"),
                        riskLevel = RiskLevel.MEDIUM
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_4",
                        name = "Open Cloud Storage",
                        description = "Launch Google Drive and verify connection",
                        actionType = "LAUNCH_APP",
                        parameters = mapOf("packageName" to params.getValue("destinationPackage")),
                        dependsOn = listOf("${planId}_3"),
                        riskLevel = RiskLevel.LOW
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_5",
                        name = "Cloud Synchronize",
                        description = "Upload downloaded assets to Drive folder '${params["folderName"]}'",
                        actionType = "UPLOAD_CLOUD",
                        parameters = mapOf("folder" to params.getValue("folderName")),
                        dependsOn = listOf("${planId}_4"),
                        riskLevel = RiskLevel.MEDIUM
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_6",
                        name = "Audit & Completion Notification",
                        description = "Verify cloud hash and notify user: 'Backup complete'",
                        actionType = "NOTIFY_USER",
                        parameters = mapOf("message" to "Instagram media backup successfully synchronized to Google Drive"),
                        dependsOn = listOf("${planId}_5"),
                        riskLevel = RiskLevel.LOW
                    )
                )
            }

            JarvisIntent.SEND_MESSAGE -> {
                val params = inferenceEngine.inferParameters("SEND_MESSAGE", nlpResult.entities, resolvedEntity, userContext)
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Resolve Contact Coordinates",
                        description = "Lookup phone number and messaging preference for ${params["contact"]}",
                        actionType = "RESOLVE_CONTACT",
                        parameters = params,
                        riskLevel = RiskLevel.LOW
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_2",
                        name = "Dispatch ${params["platform"]} Message",
                        description = "Send payload to ${params["contact"]} (${params["phoneNumber"]})",
                        actionType = "SEND_COMMUNICATION",
                        parameters = params,
                        dependsOn = listOf("${planId}_1"),
                        riskLevel = RiskLevel.MEDIUM
                    )
                )
            }

            JarvisIntent.MAKE_CALL -> {
                val params = inferenceEngine.inferParameters("MAKE_CALL", nlpResult.entities, resolvedEntity, userContext)
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Resolve Contact Phone",
                        description = "Verify active line for ${params["contact"]}",
                        actionType = "RESOLVE_CONTACT",
                        parameters = params,
                        riskLevel = RiskLevel.LOW
                    )
                )
                tasks.add(
                    Task(
                        id = "${planId}_2",
                        name = "Place Call",
                        description = "Connect dialer line to ${params["contact"]} (${params["phoneNumber"]})",
                        actionType = "MAKE_CALL",
                        parameters = params,
                        dependsOn = listOf("${planId}_1"),
                        riskLevel = RiskLevel.HIGH
                    )
                )
            }

            JarvisIntent.CHANGE_SETTING -> {
                val params = inferenceEngine.inferParameters("CHANGE_SETTING", nlpResult.entities, resolvedEntity, userContext)
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Apply System Setting",
                        description = "Configure ${params["setting"]} to target state ${params["value"]}",
                        actionType = "SYSTEM_SETTING",
                        parameters = params,
                        riskLevel = RiskLevel.LOW
                    )
                )
            }

            JarvisIntent.SET_ALARM_TIMER -> {
                val params = inferenceEngine.inferParameters("SET_ALARM_TIMER", nlpResult.entities, resolvedEntity, userContext)
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Schedule System Alarm",
                        description = "Set alarm trigger for ${params["time"]}",
                        actionType = "SCHEDULE_ALARM",
                        parameters = params,
                        riskLevel = RiskLevel.LOW
                    )
                )
            }

            JarvisIntent.OPEN_APP -> {
                val params = inferenceEngine.inferParameters("OPEN_APP", nlpResult.entities, resolvedEntity, userContext)
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Launch ${params["appName"]}",
                        description = "Bring ${params["appName"]} (${params["packageName"]}) to foreground",
                        actionType = "LAUNCH_APP",
                        parameters = params,
                        riskLevel = RiskLevel.LOW
                    )
                )
            }

            JarvisIntent.CAPTURE_PHOTO -> {
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Launch Camera Capture",
                        description = "Initialize device camera sensor and take photo",
                        actionType = "CAPTURE_PHOTO",
                        riskLevel = RiskLevel.LOW
                    )
                )
            }

            JarvisIntent.TURN_ON_MODE -> {
                val params = inferenceEngine.inferParameters("TURN_ON_MODE", nlpResult.entities, resolvedEntity, userContext)
                val mode = params["mode"] ?: "WORK_MODE"
                if (mode == "WORK_MODE") {
                    tasks.add(
                        Task(
                            id = "${planId}_1",
                            name = "Enable Work WiFi",
                            description = "Activate high-speed office WiFi connection",
                            actionType = "SYSTEM_SETTING",
                            parameters = mapOf("setting" to "WIFI", "value" to "1"),
                            riskLevel = RiskLevel.LOW
                        )
                    )
                    tasks.add(
                        Task(
                            id = "${planId}_2",
                            name = "Silence Disturbances",
                            description = "Set ringer to vibrate mode for meeting focus",
                            actionType = "SYSTEM_SETTING",
                            parameters = mapOf("setting" to "VIBRATE_MODE", "value" to "1"),
                            riskLevel = RiskLevel.LOW
                        )
                    )
                    tasks.add(
                        Task(
                            id = "${planId}_3",
                            name = "Launch Workspace Hub",
                            description = "Open Gmail and Calendar overview",
                            actionType = "LAUNCH_APP",
                            parameters = mapOf("packageName" to "com.google.android.gm"),
                            dependsOn = listOf("${planId}_1"),
                            riskLevel = RiskLevel.LOW
                        )
                    )
                } else {
                    // Night Mode
                    tasks.add(
                        Task(
                            id = "${planId}_1",
                            name = "Dim Display Luminescence",
                            description = "Set screen brightness to comfortable night level 25%",
                            actionType = "SYSTEM_SETTING",
                            parameters = mapOf("setting" to "BRIGHTNESS", "value" to "25"),
                            riskLevel = RiskLevel.LOW
                        )
                    )
                    tasks.add(
                        Task(
                            id = "${planId}_2",
                            name = "Enable Silent Shield",
                            description = "Mute ringer and non-critical sound streams",
                            actionType = "SYSTEM_SETTING",
                            parameters = mapOf("setting" to "SILENT_MODE", "value" to "1"),
                            riskLevel = RiskLevel.LOW
                        )
                    )
                    tasks.add(
                        Task(
                            id = "${planId}_3",
                            name = "Schedule Tomorrow Alarm",
                            description = "Ensure morning wake alarm is active at 07:00 AM",
                            actionType = "SCHEDULE_ALARM",
                            parameters = mapOf("time" to "07:00 AM Tomorrow", "label" to "Morning Wake"),
                            riskLevel = RiskLevel.LOW
                        )
                    )
                }
            }

            else -> {
                // Generic query / task execution
                tasks.add(
                    Task(
                        id = "${planId}_1",
                        name = "Execute JARVIS Intelligence Query",
                        description = "Evaluate response: ${nlpResult.originalText}",
                        actionType = "QUERY_AI",
                        parameters = mapOf("query" to nlpResult.originalText),
                        riskLevel = RiskLevel.LOW
                    )
                )
            }
        }

        val (risk, needsConfirm) = safetyValidator.evaluateRisk(nlpResult.intent, tasks)
        val confirmMsg = if (needsConfirm) safetyValidator.buildConfirmationMessage(nlpResult.intent, tasks) else null
        val totalDuration = tasks.sumOf { it.estimatedMs }

        return ActionPlan(
            id = planId,
            originalIntent = nlpResult.intent,
            originalText = nlpResult.originalText,
            tasks = tasks,
            riskLevel = risk,
            requiresConfirmation = needsConfirm,
            confirmationMessage = confirmMsg,
            estimatedDurationMs = totalDuration
        )
    }
}
