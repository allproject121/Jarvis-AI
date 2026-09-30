package com.example.learning

import com.example.data.model.CommandRecord
import com.example.data.model.PatternRecord
import com.example.data.repository.JarvisRepository

class PatternRecognitionEngine(private val repository: JarvisRepository) {

    suspend fun analyzeHistoryAndLearn(history: List<CommandRecord>): List<PatternRecord> {
        if (history.size < 3) return emptyList()

        val learnedPatterns = mutableListOf<PatternRecord>()

        // 1. Check for repeated commands (e.g. WiFi on or setting brightness)
        val groupedByIntent = history.groupBy { it.parsedIntent }
        for ((intent, commands) in groupedByIntent) {
            if (commands.size >= 3) {
                val mostFrequent = commands.first()
                val patternDesc = "Auto-${intent.replace("_", " ")} routine"
                val existing = learnedPatterns.any { it.description == patternDesc }
                if (!existing) {
                    val newPattern = PatternRecord(
                        patternType = "DAILY_ROUTINE",
                        description = patternDesc,
                        triggerType = "TIME_BASED",
                        triggerParams = "Recognized from ${commands.size} occurrences",
                        actions = mostFrequent.actionsExecuted,
                        confidenceScore = (0.75f + (commands.size * 0.05f)).coerceAtMost(0.99f),
                        occurrenceCount = commands.size,
                        enabled = true,
                        autoExecute = false
                    )
                    learnedPatterns.add(newPattern)
                    repository.insertPattern(newPattern)
                }
            }
        }

        return learnedPatterns
    }
}
