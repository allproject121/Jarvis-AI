package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_API_KEY = "key_gemini_api_key"
        private const val KEY_SELECTED_VOICE = "key_selected_voice"
        private const val KEY_AUTO_TTS = "key_auto_tts"
    }

    var customApiKey: String
        get() = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GEMINI_API_KEY, value.trim()).apply()

    var isAutoTtsEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_TTS, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_TTS, value).apply()

    var selectedVoice: String
        get() = prefs.getString(KEY_SELECTED_VOICE, "Kore") ?: "Kore"
        set(value) = prefs.edit().putString(KEY_SELECTED_VOICE, value).apply()
}
