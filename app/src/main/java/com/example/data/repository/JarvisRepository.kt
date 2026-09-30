package com.example.data.repository

import com.example.data.JarvisDatabase
import com.example.data.model.CommandRecord
import com.example.data.model.EntityRecord
import com.example.data.model.PatternRecord
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val database: JarvisDatabase) {

    val allCommands: Flow<List<CommandRecord>> = database.commandDao().getAllCommands()
    val recentCommands: Flow<List<CommandRecord>> = database.commandDao().getRecentCommands(15)
    val allPatterns: Flow<List<PatternRecord>> = database.patternDao().getAllPatterns()
    val allEntities: Flow<List<EntityRecord>> = database.entityDao().getAllEntities()

    suspend fun recordCommand(command: CommandRecord): Long {
        return database.commandDao().insertCommand(command)
    }

    suspend fun updateFeedback(id: Long, feedback: String) {
        database.commandDao().updateFeedback(id, feedback)
    }

    suspend fun clearHistory() {
        database.commandDao().clearHistory()
    }

    suspend fun insertPattern(pattern: PatternRecord): Long {
        return database.patternDao().insertPattern(pattern)
    }

    suspend fun setPatternEnabled(id: Long, enabled: Boolean) {
        database.patternDao().setEnabled(id, enabled)
    }

    suspend fun setPatternAutoExecute(id: Long, autoExecute: Boolean) {
        database.patternDao().setAutoExecute(id, autoExecute)
    }

    suspend fun deletePattern(pattern: PatternRecord) {
        database.patternDao().deletePattern(pattern)
    }

    suspend fun findEntityByName(name: String): EntityRecord? {
        return database.entityDao().findEntityByName(name)
    }

    suspend fun insertEntity(entity: EntityRecord): Long {
        return database.entityDao().insertEntity(entity)
    }

    suspend fun markEntityAccessed(id: Long) {
        database.entityDao().markAccessed(id)
    }

    suspend fun seedDefaultsIfEmpty() {
        // Seed helpful initial patterns if empty
        val initialPatterns = listOf(
            PatternRecord(
                patternType = "DAILY_ROUTINE",
                description = "Morning Briefing & Setup",
                triggerType = "TIME_BASED",
                triggerParams = "07:30 AM Daily",
                actions = "Check Weather, Unmute Phone, Set Brightness 60%, Open Calendar",
                confidenceScore = 0.96f,
                occurrenceCount = 28,
                enabled = true,
                autoExecute = false
            ),
            PatternRecord(
                patternType = "LOCATION_BASED",
                description = "Office Arrival Work Mode",
                triggerType = "LOCATION_BASED",
                triggerParams = "Office (Geofence 500m)",
                actions = "Turn on WiFi, Enable Vibrate Mode, Launch Gmail, Set Brightness 80%",
                confidenceScore = 0.92f,
                occurrenceCount = 18,
                enabled = true,
                autoExecute = true
            ),
            PatternRecord(
                patternType = "CONDITIONAL",
                description = "Critical Battery Shield",
                triggerType = "CONDITION_BASED",
                triggerParams = "Battery < 20%",
                actions = "Enable Low Power Mode, Reduce Brightness to 25%, Turn off Bluetooth",
                confidenceScore = 0.99f,
                occurrenceCount = 14,
                enabled = true,
                autoExecute = true
            ),
            PatternRecord(
                patternType = "DAILY_ROUTINE",
                description = "Night Sanctuary Mode",
                triggerType = "TIME_BASED",
                triggerParams = "11:00 PM Daily",
                actions = "Enable Night Mode, Set Silent Mode, Set Alarm 07:00 AM, Dim Screen",
                confidenceScore = 0.94f,
                occurrenceCount = 31,
                enabled = true,
                autoExecute = false
            )
        )

        val initialEntities = listOf(
            EntityRecord(
                entityType = "CONTACT",
                entityName = "Mom",
                aliases = "Mother, Mummy, Mom, Maa",
                resolvedValue = "+1-555-0192",
                metadata = "Preferred: WhatsApp, Frequency: Daily",
                confidenceScore = 0.98f,
                accessCount = 42
            ),
            EntityRecord(
                entityType = "CONTACT",
                entityName = "Boss",
                aliases = "Manager, Sir, Team Lead",
                resolvedValue = "+1-555-0144",
                metadata = "Preferred: Phone/Email",
                confidenceScore = 0.89f,
                accessCount = 15
            ),
            EntityRecord(
                entityType = "APP",
                entityName = "WhatsApp",
                aliases = "wa, message, chat",
                resolvedValue = "com.whatsapp",
                confidenceScore = 1.0f,
                accessCount = 85
            ),
            EntityRecord(
                entityType = "APP",
                entityName = "Gmail",
                aliases = "email, mail, Google Mail",
                resolvedValue = "com.google.android.gm",
                confidenceScore = 1.0f,
                accessCount = 50
            ),
            EntityRecord(
                entityType = "LOCATION",
                entityName = "Office",
                aliases = "workplace, work, company",
                resolvedValue = "28.5244, 77.1855",
                metadata = "SSID: Office_Corporate_WiFi",
                confidenceScore = 0.95f,
                accessCount = 24
            ),
            EntityRecord(
                entityType = "SETTING",
                entityName = "Brightness",
                aliases = "luminosity, light, display, screen light",
                resolvedValue = "SCREEN_BRIGHTNESS",
                metadata = "Range 0-100",
                confidenceScore = 0.99f,
                accessCount = 37
            )
        )

        for (pattern in initialPatterns) {
            database.patternDao().insertPattern(pattern)
        }
        for (entity in initialEntities) {
            database.entityDao().insertEntity(entity)
        }
    }
}
