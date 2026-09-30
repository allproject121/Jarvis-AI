package com.example.nlp

import com.example.data.model.CommandRecord

class ContextualUnderstandingEngine {

    fun resolvePronouns(
        currentCommand: String,
        entities: ExtractedEntities,
        recentCommands: List<CommandRecord>
    ): ExtractedEntities {
        var resolvedContact = entities.contact

        // Check pronoun markers: "use", "उसे", "him", "her", "them"
        val lower = currentCommand.lowercase()
        val hasPronoun = lower.contains("use ") || lower.contains("उसे") || lower.contains("him") || lower.contains("her") || lower.contains("them")

        if (resolvedContact == null && hasPronoun) {
            // Find last contact mentioned in recent commands
            for (cmd in recentCommands) {
                val lastText = cmd.originalText.lowercase()
                val contacts = listOf("mom", "dad", "mother", "mummy", "boss", "alex", "raj", "priya")
                val found = contacts.firstOrNull { lastText.contains(it) }
                if (found != null) {
                    resolvedContact = found.replaceFirstChar { it.uppercase() }
                    break
                }
            }
        }

        return entities.copy(contact = resolvedContact)
    }

    fun handleEllipsis(
        currentCommand: String,
        recentCommands: List<CommandRecord>
    ): Pair<JarvisIntent, String>? {
        val lower = currentCommand.lowercase()
        // "aur Mom ko bhi", "and Dad too"
        if (lower.contains("ko bhi") || lower.contains("to bhi") || lower.contains("too") || lower.contains("also")) {
            val lastCmd = recentCommands.firstOrNull()
            if (lastCmd != null) {
                val lastIntent = try {
                    JarvisIntent.valueOf(lastCmd.parsedIntent)
                } catch (_: Exception) {
                    JarvisIntent.SEND_MESSAGE
                }
                return Pair(lastIntent, "Appended recipient to previous action")
            }
        }
        return null
    }

    fun inferImplicitRoutine(command: String): String? {
        val lower = command.lowercase()
        return when {
            lower.contains("office ja rahe") || lower.contains("going to office") || lower.contains("headed to work") ->
                "WORK_COMMUTE_ROUTINE"
            lower.contains("sone ja raha") || lower.contains("going to sleep") || lower.contains("good night") ->
                "NIGHT_SLEEP_ROUTINE"
            lower.contains("gym") || lower.contains("workout") ->
                "WORKOUT_ROUTINE"
            else -> null
        }
    }
}
