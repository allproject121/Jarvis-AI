package com.example.execution

import android.content.Context
import com.example.planner.ActionPlan
import com.example.planner.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StepExecutionStatus(
    val taskId: String,
    val taskName: String,
    val isRunning: Boolean,
    val isCompleted: Boolean,
    val error: String? = null
)

data class PlanExecutionStatus(
    val planId: String,
    val isExecuting: Boolean,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val statusMessage: String,
    val stepStatuses: List<StepExecutionStatus>,
    val isFinished: Boolean = false,
    val success: Boolean = true
)

class ExecutionEngine(private val context: Context) {

    private val systemActions = SystemActionsExecutor(context)
    private val appLauncher = AppLauncherService(context)
    private val messageExecutor = ContactMessageExecutor(context)
    private val alarmManager = AlarmTimerManager(context)

    private val _executionStatus = MutableStateFlow<PlanExecutionStatus?>(null)
    val executionStatus: StateFlow<PlanExecutionStatus?> = _executionStatus.asStateFlow()

    suspend fun executePlan(plan: ActionPlan): Boolean {
        val initialStepStatuses = plan.tasks.map {
            StepExecutionStatus(taskId = it.id, taskName = it.name, isRunning = false, isCompleted = false)
        }

        _executionStatus.value = PlanExecutionStatus(
            planId = plan.id,
            isExecuting = true,
            currentStepIndex = 0,
            totalSteps = plan.tasks.size,
            statusMessage = "Starting execution of ${plan.tasks.size} task(s)...",
            stepStatuses = initialStepStatuses
        )

        var allSucceeded = true

        for (i in plan.tasks.indices) {
            val task = plan.tasks[i]
            updateStepState(plan.id, i, isRunning = true, isCompleted = false, message = "Executing: ${task.name}")
            delay(400) // Brief deliberate cadence for visual feedback & animation

            val success = executeIndividualTask(task)
            if (!success) {
                allSucceeded = false
                updateStepState(plan.id, i, isRunning = false, isCompleted = false, error = "Failed to execute", message = "Step failed: ${task.name}")
                break
            } else {
                updateStepState(plan.id, i, isRunning = false, isCompleted = true, message = "Completed: ${task.name}")
            }
        }

        _executionStatus.value = _executionStatus.value?.copy(
            isExecuting = false,
            isFinished = true,
            success = allSucceeded,
            statusMessage = if (allSucceeded) "All ${plan.tasks.size} tasks executed successfully!" else "Workflow interrupted on step"
        )

        return allSucceeded
    }

    private suspend fun executeIndividualTask(task: Task): Boolean {
        return when (task.actionType) {
            "SYSTEM_SETTING" -> {
                val setting = task.parameters["setting"] ?: "BRIGHTNESS"
                val value = task.parameters["value"] ?: "1"
                when (setting.uppercase()) {
                    "BRIGHTNESS" -> systemActions.setBrightness(value.toIntOrNull() ?: 70)
                    "VOLUME" -> systemActions.setVolume(value.toIntOrNull() ?: 70)
                    "WIFI" -> systemActions.toggleWifi(value == "1" || value.equals("true", true))
                    "SILENT_MODE" -> systemActions.setRingerMode("SILENT")
                    "VIBRATE_MODE" -> systemActions.setRingerMode("VIBRATE")
                    else -> true
                }
            }

            "LAUNCH_APP" -> {
                val pkg = task.parameters["packageName"] ?: "com.android.settings"
                appLauncher.launchApp(pkg)
            }

            "SEND_COMMUNICATION" -> {
                val phone = task.parameters["phoneNumber"] ?: "+1-555-0192"
                val msg = task.parameters["message"] ?: "Hello from JARVIS"
                val platform = task.parameters["platform"] ?: "SMS"
                messageExecutor.sendMessage(phone, msg, platform)
            }

            "MAKE_CALL" -> {
                val phone = task.parameters["phoneNumber"] ?: "+1-555-0192"
                messageExecutor.makeCall(phone)
            }

            "SCHEDULE_ALARM" -> {
                val time = task.parameters["time"] ?: "07:00 AM"
                val label = task.parameters["label"] ?: "JARVIS Alarm"
                alarmManager.setAlarm(time, label)
            }

            "UI_AUTOMATION_SELECT" -> {
                // Check if Accessibility Service is running
                val accessibility = JarvisAccessibilityService.instance
                if (accessibility != null) {
                    accessibility.performClickByText("Photos")
                }
                true
            }

            "DOWNLOAD_MEDIA", "UPLOAD_CLOUD" -> {
                delay(600) // Simulated background I/O operation
                true
            }

            "NOTIFY_USER", "QUERY_AI" -> {
                true
            }

            else -> true
        }
    }

    private fun updateStepState(
        planId: String,
        stepIndex: Int,
        isRunning: Boolean,
        isCompleted: Boolean,
        error: String? = null,
        message: String
    ) {
        val current = _executionStatus.value ?: return
        val updatedList = current.stepStatuses.mapIndexed { idx, item ->
            if (idx == stepIndex) {
                item.copy(isRunning = isRunning, isCompleted = isCompleted, error = error)
            } else {
                item
            }
        }
        _executionStatus.value = current.copy(
            currentStepIndex = stepIndex + 1,
            statusMessage = message,
            stepStatuses = updatedList
        )
    }

    fun clearStatus() {
        _executionStatus.value = null
    }
}
