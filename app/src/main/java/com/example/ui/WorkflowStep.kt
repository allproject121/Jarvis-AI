package com.example.ui

enum class WorkflowStepStatus {
    COMPLETED,
    IN_PROGRESS,
    PENDING
}

data class WorkflowStepItem(
    val description: String,
    val status: WorkflowStepStatus
)
