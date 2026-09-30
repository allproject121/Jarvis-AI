package com.example.nlp

enum class JarvisIntent {
    SEND_MESSAGE,
    MAKE_CALL,
    OPEN_APP,
    CHANGE_SETTING,
    LOCK_UNLOCK_SCREEN,
    CREATE_AUTOMATION,
    SEARCH_INFO,
    PLAY_MEDIA,
    MANAGE_FILES,
    CAPTURE_PHOTO,
    SCHEDULE_EVENT,
    SET_ALARM_TIMER,
    QUERY_STATUS,
    ASK_QUESTION,
    TURN_ON_MODE,
    BACKUP_MEDIA,
    UNKNOWN
}

enum class CommandComplexity {
    SIMPLE,        // single action e.g. "WiFi on कर"
    COMPOUND,      // multiple independent actions e.g. "WiFi on कर और Bluetooth on कर"
    SEQUENTIAL,    // multi-step with dependencies e.g. "Instagram photos download करके Drive में डाल"
    CONDITIONAL,   // if-then logic e.g. "अगर battery 20% से कम हो तो low power mode on कर"
    AUTOMATED      // recurring rule e.g. "हर सुबह 6 बजे alarm लगवा"
}

data class IntentClassificationResult(
    val intent: JarvisIntent,
    val confidence: Float,
    val complexity: CommandComplexity,
    val alternativeIntents: List<JarvisIntent> = emptyList()
)

class IntentClassifier {

    fun classify(preprocessed: PreprocessedText, entities: ExtractedEntities): IntentClassificationResult {
        val text = preprocessed.normalized
        val original = preprocessed.original.lowercase()

        // Check complexity
        val complexity = when {
            entities.condition != null || original.contains("अगर") || original.contains("if ") -> CommandComplexity.CONDITIONAL
            original.contains("हर रोज") || original.contains("daily") || original.contains("every") || original.contains("automatically") -> CommandComplexity.AUTOMATED
            (original.contains("aur") || original.contains("and") || original.contains("और")) && (original.contains("drive") || original.contains("then") || original.contains("phir") || original.contains("करके")) -> CommandComplexity.SEQUENTIAL
            original.contains("aur") || original.contains("and") || original.contains("तथा") -> CommandComplexity.COMPOUND
            else -> CommandComplexity.SIMPLE
        }

        // Multi-step backup / drive workflow
        if (text.contains("backup") || (text.contains("drive") && text.contains("instagram"))) {
            return IntentClassificationResult(
                intent = JarvisIntent.BACKUP_MEDIA,
                confidence = 0.95f,
                complexity = CommandComplexity.SEQUENTIAL
            )
        }

        // Message
        if (text.contains("message") || text.contains("bhej") || text.contains("whatsapp") || text.contains("sms") || text.contains("send")) {
            return IntentClassificationResult(
                intent = JarvisIntent.SEND_MESSAGE,
                confidence = 0.96f,
                complexity = complexity,
                alternativeIntents = listOf(JarvisIntent.MAKE_CALL)
            )
        }

        // Call
        if (text.contains("call") || text.contains("phone laga") || text.contains("ring")) {
            return IntentClassificationResult(
                intent = JarvisIntent.MAKE_CALL,
                confidence = 0.97f,
                complexity = complexity
            )
        }

        // Alarm / Timer
        if (text.contains("alarm") || text.contains("timer") || text.contains("बजे") || text.contains("wake me")) {
            return IntentClassificationResult(
                intent = JarvisIntent.SET_ALARM_TIMER,
                confidence = 0.94f,
                complexity = complexity
            )
        }

        // Settings (WiFi, Brightness, Bluetooth, Volume, etc.)
        if (entities.settingName != null || text.contains("wifi") || text.contains("brightness") || text.contains("bluetooth") || text.contains("volume") || text.contains("mode")) {
            val intent = if (text.contains("work mode") || text.contains("night mode") || text.contains("silent")) {
                JarvisIntent.TURN_ON_MODE
            } else {
                JarvisIntent.CHANGE_SETTING
            }
            return IntentClassificationResult(
                intent = intent,
                confidence = 0.98f,
                complexity = complexity
            )
        }

        // Open App
        if (text.contains("khol") || text.contains("open") || text.contains("launch") || text.contains("chala") || entities.app != null) {
            return IntentClassificationResult(
                intent = JarvisIntent.OPEN_APP,
                confidence = 0.95f,
                complexity = complexity
            )
        }

        // Photo / Camera
        if (text.contains("photo") || text.contains("camera") || text.contains("selfie") || text.contains("screenshot") || text.contains("खीच")) {
            return IntentClassificationResult(
                intent = JarvisIntent.CAPTURE_PHOTO,
                confidence = 0.93f,
                complexity = complexity
            )
        }

        // Query Status
        if (text.contains("kitni hai") || text.contains("status") || text.contains("percent") || text.contains("battery")) {
            return IntentClassificationResult(
                intent = JarvisIntent.QUERY_STATUS,
                confidence = 0.91f,
                complexity = complexity
            )
        }

        // Create Automation
        if (complexity == CommandComplexity.AUTOMATED || text.contains("automation") || text.contains("rule")) {
            return IntentClassificationResult(
                intent = JarvisIntent.CREATE_AUTOMATION,
                confidence = 0.92f,
                complexity = CommandComplexity.AUTOMATED
            )
        }

        // General conversational question
        return IntentClassificationResult(
            intent = JarvisIntent.ASK_QUESTION,
            confidence = 0.85f,
            complexity = complexity
        )
    }
}
