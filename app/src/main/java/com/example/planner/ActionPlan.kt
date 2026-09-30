package com.example.planner

import com.example.nlp.JarvisIntent

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class Task(
    val id: String,
    val name: String,
    val description: String,
    val actionType: String,
    val parameters: Map<String, String> = emptyMap(),
    val dependsOn: List<String> = emptyList(),
    val canRunParallel: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val estimatedMs: Long = 500L
)

data class ActionPlan(
    val id: String,
    val originalIntent: JarvisIntent,
    val originalText: String,
    val tasks: List<Task>,
    val riskLevel: RiskLevel,
    val requiresConfirmation: Boolean,
    val confirmationMessage: String? = null,
    val estimatedDurationMs: Long = 1000L
)
