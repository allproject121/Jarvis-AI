package com.example.planner

import com.example.data.model.EntityRecord
import com.example.memory.UserContext
import com.example.nlp.ExtractedEntities

class ParameterInferenceEngine {

    fun inferParameters(
        actionType: String,
        entities: ExtractedEntities,
        resolvedEntity: EntityRecord?,
        userContext: UserContext
    ): Map<String, String> {
        val params = mutableMapOf<String, String>()

        when (actionType) {
            "SEND_MESSAGE" -> {
                params["contact"] = resolvedEntity?.entityName ?: entities.contact ?: "Mom"
                params["phoneNumber"] = resolvedEntity?.resolvedValue ?: "+1-555-0192"
                params["message"] = if (!entities.actionDetail.isNullOrBlank() && entities.actionDetail.contains("urgent")) {
                    "Important: Reaching out regarding our scheduled update."
                } else {
                    "Hi! Reaching out via JARVIS automation."
                }
                params["platform"] = if (resolvedEntity?.metadata?.contains("WhatsApp") == true) "WhatsApp" else "SMS"
            }

            "MAKE_CALL" -> {
                params["contact"] = resolvedEntity?.entityName ?: entities.contact ?: "Mom"
                params["phoneNumber"] = resolvedEntity?.resolvedValue ?: "+1-555-0192"
            }

            "CHANGE_SETTING" -> {
                val setting = entities.settingName ?: "BRIGHTNESS"
                params["setting"] = setting
                val value = entities.settingValue ?: when (setting) {
                    "BRIGHTNESS" -> if (userContext.hourOfDay in 21..24 || userContext.hourOfDay in 0..6) "30" else "80"
                    "VOLUME" -> "70"
                    else -> "1"
                }
                params["value"] = value.toString()
            }

            "SET_ALARM_TIMER" -> {
                val time = entities.timeString ?: if (userContext.hourOfDay >= 20) "07:00 AM Tomorrow" else "09:00 AM"
                params["time"] = time
                params["label"] = "JARVIS Scheduled Alarm"
            }

            "OPEN_APP" -> {
                val app = entities.app ?: "Settings"
                params["appName"] = app
                params["packageName"] = resolvedEntity?.resolvedValue ?: "com.android.settings"
            }

            "BACKUP_MEDIA" -> {
                params["sourceApp"] = "Instagram"
                params["sourcePackage"] = "com.instagram.android"
                params["destinationApp"] = "Google Drive"
                params["destinationPackage"] = "com.google.android.apps.docs"
                params["folderName"] = "JARVIS_Auto_Backup"
            }

            "TURN_ON_MODE" -> {
                params["mode"] = if (userContext.hourOfDay >= 21) "NIGHT_MODE" else "WORK_MODE"
            }
        }

        return params
    }
}
