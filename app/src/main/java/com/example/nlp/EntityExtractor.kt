package com.example.nlp

data class ExtractedEntities(
    val contact: String? = null,
    val app: String? = null,
    val settingName: String? = null,
    val settingValue: Int? = null,
    val timeString: String? = null,
    val location: String? = null,
    val condition: String? = null,
    val actionDetail: String? = null,
    val isUrgent: Boolean = false,
    val isNegated: Boolean = false
)

class EntityExtractor {

    private val commonApps = mapOf(
        "whatsapp" to "com.whatsapp",
        "gmail" to "com.google.android.gm",
        "mail" to "com.google.android.gm",
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "camera" to "camera",
        "settings" to "settings",
        "calendar" to "com.google.android.calendar",
        "drive" to "com.google.android.apps.docs",
        "google drive" to "com.google.android.apps.docs",
        "clock" to "com.google.android.deskclock"
    )

    private val commonContacts = listOf("mom", "dad", "mother", "father", "mummy", "boss", "alex", "raj", "priya", "john", "sister", "brother")

    fun extract(preprocessed: PreprocessedText): ExtractedEntities {
        val text = preprocessed.normalized
        val original = preprocessed.original

        // 1. Contact Extraction
        var foundContact: String? = null
        for (c in commonContacts) {
            if (text.contains(c)) {
                foundContact = c.replaceFirstChar { it.uppercase() }
                break
            }
        }
        // Hindi syntax: "Mom को" or "Raj ko"
        val koRegex = Regex("([a-zA-Z\u0900-\u097F]+)\\s*(?:ko|को)", RegexOption.IGNORE_CASE)
        val koMatch = koRegex.find(original)
        if (koMatch != null) {
            foundContact = koMatch.groupValues[1].replaceFirstChar { it.uppercase() }
        }

        // 2. App Extraction
        var foundApp: String? = null
        for ((name, pkg) in commonApps) {
            if (text.contains(name)) {
                foundApp = name.replaceFirstChar { it.uppercase() }
                break
            }
        }

        // 3. Setting Extraction & Value
        var foundSetting: String? = null
        var foundValue: Int? = null

        if (text.contains("brightness") || text.contains("light") || text.contains("चमक")) {
            foundSetting = "BRIGHTNESS"
            // Extract numeric value
            val numRegex = Regex("(\\d{1,3})")
            val match = numRegex.find(text)
            if (match != null) {
                foundValue = match.groupValues[1].toIntOrNull()?.coerceIn(0, 100)
            }
        } else if (text.contains("wifi") || text.contains("wi-fi")) {
            foundSetting = "WIFI"
        } else if (text.contains("bluetooth")) {
            foundSetting = "BLUETOOTH"
        } else if (text.contains("volume") || text.contains("sound") || text.contains("आवाज़")) {
            foundSetting = "VOLUME"
            val numRegex = Regex("(\\d{1,3})")
            val match = numRegex.find(text)
            if (match != null) {
                foundValue = match.groupValues[1].toIntOrNull()?.coerceIn(0, 100)
            }
        } else if (text.contains("night mode") || text.contains("dark mode")) {
            foundSetting = "NIGHT_MODE"
        } else if (text.contains("silent") || text.contains("mute")) {
            foundSetting = "SILENT_MODE"
        } else if (text.contains("vibrate")) {
            foundSetting = "VIBRATE_MODE"
        }

        // 4. Time Extraction
        var foundTime: String? = null
        if (text.contains("kal") || text.contains("कल") || text.contains("tomorrow")) {
            foundTime = "Tomorrow"
        }
        val timeRegex = Regex("(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm|बजे)?)", RegexOption.IGNORE_CASE)
        val timeMatch = timeRegex.find(text)
        if (timeMatch != null && !timeMatch.value.contains("%")) {
            val t = timeMatch.groupValues[1]
            foundTime = if (foundTime != null) "$foundTime $t" else t
        }

        // 5. Location Extraction
        var foundLocation: String? = null
        val locations = listOf("office", "home", "gym", "work", "दफ़्तर", "घर")
        for (loc in locations) {
            if (text.contains(loc)) {
                foundLocation = loc.replaceFirstChar { it.uppercase() }
                break
            }
        }

        // 6. Condition Extraction
        var foundCondition: String? = null
        if (text.contains("agar") || text.contains("अगर") || text.contains("if ") || text.contains("when ")) {
            foundCondition = if (text.contains("battery") && (text.contains("20") || text.contains("kam") || text.contains("low"))) {
                "battery < 20%"
            } else if (text.contains("office")) {
                "arrive at Office"
            } else {
                "custom condition"
            }
        }

        val isUrgent = text.contains("urgent") || text.contains("jaldi") || text.contains("तुरंत")
        val isNegated = text.contains("don't") || text.contains("mat") || text.contains("मत") || text.contains("na")

        return ExtractedEntities(
            contact = foundContact,
            app = foundApp,
            settingName = foundSetting,
            settingValue = foundValue,
            timeString = foundTime,
            location = foundLocation,
            condition = foundCondition,
            actionDetail = original,
            isUrgent = isUrgent,
            isNegated = isNegated
        )
    }
}
