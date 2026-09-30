package com.example.prediction

import com.example.data.model.PatternRecord
import com.example.memory.UserActivity
import com.example.memory.UserContext

data class ProactiveSuggestion(
    val id: String,
    val title: String,
    val subtitle: String,
    val commandText: String,
    val iconType: String,
    val confidence: Float,
    val isCritical: Boolean = false
)

class ProactiveActionSuggester {

    fun generateSuggestions(
        userContext: UserContext,
        activePatterns: List<PatternRecord>
    ): List<ProactiveSuggestion> {
        val list = mutableListOf<ProactiveSuggestion>()

        // 1. Critical Battery Shield check
        if (userContext.deviceState.batteryLevel <= 25 && !userContext.deviceState.isCharging) {
            list.add(
                ProactiveSuggestion(
                    id = "sug_battery",
                    title = "Battery is at ${userContext.deviceState.batteryLevel}%",
                    subtitle = "Activate Low Power Shield: Dim screen & toggle Bluetooth",
                    commandText = "अगर battery 20% से कम हो तो low power mode on कर",
                    iconType = "battery",
                    confidence = 0.99f,
                    isCritical = true
                )
            )
        }

        // 2. Activity / Time-based Suggestions
        when (userContext.detectedActivity) {
            UserActivity.WORKING -> {
                list.add(
                    ProactiveSuggestion(
                        id = "sug_work",
                        title = "Focus Work Mode",
                        subtitle = "Enable office WiFi, silence notifications, open Gmail",
                        commandText = "Office mode on कर",
                        iconType = "work",
                        confidence = 0.94f
                    )
                )
                list.add(
                    ProactiveSuggestion(
                        id = "sug_mail",
                        title = "Review Incoming Correspondence",
                        subtitle = "Launch Gmail for morning communications",
                        commandText = "Gmail खोल",
                        iconType = "mail",
                        confidence = 0.88f
                    )
                )
            }

            UserActivity.COMMUTING -> {
                list.add(
                    ProactiveSuggestion(
                        id = "sug_commute",
                        title = "Navigation & Commute",
                        subtitle = "Launch Google Maps with traffic assessment",
                        commandText = "Google Maps खोल",
                        iconType = "map",
                        confidence = 0.92f
                    )
                )
            }

            UserActivity.SLEEPING -> {
                list.add(
                    ProactiveSuggestion(
                        id = "sug_night",
                        title = "Night Sanctuary Mode",
                        subtitle = "Set screen brightness 20%, silent mode, 7 AM alarm",
                        commandText = "Night mode enable कर और alarm set कर",
                        iconType = "night",
                        confidence = 0.96f
                    )
                )
            }

            UserActivity.RELAXING -> {
                list.add(
                    ProactiveSuggestion(
                        id = "sug_family",
                        title = "Connect with Family",
                        subtitle = "Send check-in message to Mom via WhatsApp",
                        commandText = "Mom को message भेज",
                        iconType = "message",
                        confidence = 0.91f
                    )
                )
                list.add(
                    ProactiveSuggestion(
                        id = "sug_backup",
                        title = "Multi-step Cloud Archival",
                        subtitle = "Backup Instagram media directly into Google Drive",
                        commandText = "Instagram पर backup कर और Google Drive में डाल",
                        iconType = "cloud",
                        confidence = 0.87f
                    )
                )
            }

            else -> {}
        }

        // 3. Include any high confidence learned user patterns
        for (pattern in activePatterns.take(2)) {
            if (list.none { it.title == pattern.description }) {
                list.add(
                    ProactiveSuggestion(
                        id = "sug_pat_${pattern.id}",
                        title = pattern.description,
                        subtitle = pattern.actions,
                        commandText = pattern.description,
                        iconType = "auto",
                        confidence = pattern.confidenceScore
                    )
                )
            }
        }

        return list.sortedByDescending { it.confidence }
    }
}
