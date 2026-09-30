package com.example.nlp

class TextPreprocessor {

    private val fillerWords = setOf(
        "bhai", "yaar", "acha", "beta", "please", "kripya", "zara", "sun", "hey", "jarvis", "ok"
    )

    private val abbreviations = mapOf(
        "msg" to "message",
        "txt" to "message",
        "wa" to "whatsapp",
        "insta" to "instagram",
        "yt" to "youtube",
        "gm" to "gmail",
        "cal" to "calendar",
        "pic" to "photo",
        "pics" to "photos"
    )

    fun preprocess(input: String): PreprocessedText {
        var text = input.trim().lowercase()

        // Clean punctuation
        text = text.replace(Regex("[.,!?;:]"), " ")

        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val filteredTokens = tokens.filterNot { fillerWords.contains(it) }

        val expandedTokens = filteredTokens.map { token ->
            abbreviations[token] ?: token
        }

        val cleaned = expandedTokens.joinToString(" ")
        val isHindiOrHinglish = detectHindiOrHinglish(input)

        return PreprocessedText(
            original = input,
            normalized = cleaned,
            tokens = expandedTokens,
            isHindiOrHinglish = isHindiOrHinglish
        )
    }

    private fun detectHindiOrHinglish(text: String): Boolean {
        // Contains Devanagari script or typical Hinglish markers
        val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
        if (hasDevanagari) return true

        val hinglishKeywords = listOf("kar", "khol", "bhej", "lagwa", "laga", "bata", "karo", "de", "hoga", "rahe")
        val lower = text.lowercase()
        return hinglishKeywords.any { lower.contains(it) }
    }
}

data class PreprocessedText(
    val original: String,
    val normalized: String,
    val tokens: List<String>,
    val isHindiOrHinglish: Boolean
)
