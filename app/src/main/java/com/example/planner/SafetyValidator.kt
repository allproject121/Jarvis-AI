package com.example.planner

import com.example.nlp.JarvisIntent

class SafetyValidator {

    fun evaluateRisk(intent: JarvisIntent, tasks: List<Task>): Pair<RiskLevel, Boolean> {
        var highestRisk = RiskLevel.LOW

        for (t in tasks) {
            if (t.riskLevel.ordinal > highestRisk.ordinal) {
                highestRisk = t.riskLevel
            }
        }

        val requiresConfirmation = when (highestRisk) {
            RiskLevel.CRITICAL -> true
            RiskLevel.HIGH -> true
            RiskLevel.MEDIUM -> tasks.size > 3 // multiple compound steps
            RiskLevel.LOW -> false
        }

        return Pair(highestRisk, requiresConfirmation)
    }

    fun buildConfirmationMessage(intent: JarvisIntent, tasks: List<Task>): String {
        return when (intent) {
            JarvisIntent.SEND_MESSAGE -> {
                val contact = tasks.firstOrNull()?.parameters?.get("contact") ?: "Contact"
                val platform = tasks.firstOrNull()?.parameters?.get("platform") ?: "Message"
                "Send $platform message to $contact?"
            }
            JarvisIntent.MAKE_CALL -> {
                val contact = tasks.firstOrNull()?.parameters?.get("contact") ?: "Contact"
                "Initiate direct phone call to $contact?"
            }
            JarvisIntent.BACKUP_MEDIA -> {
                "Execute multi-step backup: Fetch media from Instagram and upload to Google Drive?"
            }
            else -> {
                "Execute ${tasks.size} automation step(s)?"
            }
        }
    }
}
