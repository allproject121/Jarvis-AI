package com.example.gemini

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Base64
import com.example.BuildConfig
import com.example.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isActionPlan: Boolean = false,
    val imageBitmap: Bitmap? = null
)

class ApiKeyMissingException(message: String = "Gemini API key is not configured.") : Exception(message)
class ApiKeyInvalidException(message: String = "Gemini API key is invalid or rejected by Google.") : Exception(message)

class GeminiClient(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val preferences = AppPreferences(context)

    fun isValidKeyCandidate(key: String): Boolean {
        val trimmed = key.trim()
        if (trimmed.isBlank()) return false
        if (trimmed == "MY_GEMINI_API_KEY" || trimmed == "YOUR_API_KEY") return false
        if (trimmed.equals("null", ignoreCase = true) || trimmed.equals("undefined", ignoreCase = true)) return false
        return trimmed.length >= 10 && !trimmed.contains(" ")
    }

    fun getEffectiveApiKey(): String {
        val custom = preferences.customApiKey.trim()
        if (isValidKeyCandidate(custom)) {
            return custom
        }
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (isValidKeyCandidate(buildKey)) {
            return buildKey
        }
        return ""
    }

    fun isApiKeyConfigured(): Boolean {
        return getEffectiveApiKey().isNotEmpty()
    }

    suspend fun validateApiKey(candidateKey: String): Result<String> = withContext(Dispatchers.IO) {
        val key = candidateKey.trim()
        if (!isValidKeyCandidate(key)) {
            return@withContext Result.failure(
                ApiKeyInvalidException("Invalid key format. Gemini API keys are typically ~39 characters long and start with 'AIzaSy'.")
            )
        }

        try {
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", "System ping check"))
            content.put("parts", parts)
            contents.put(content)
            root.put("contents", contents)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$key"
            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                if (responseBody.contains("API_KEY_INVALID") || responseBody.contains("API key not valid")) {
                    return@withContext Result.failure(ApiKeyInvalidException("Google rejected this key: API key not valid."))
                }
                return@withContext Result.failure(Exception("Google API returned code ${response.code}"))
            }

            Result.success("Neural node connection verified! Key is active.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private var nativeTts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        try {
            nativeTts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    nativeTts?.language = Locale.US
                    isTtsInitialized = true
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Multi-turn chat generation with selectable Gemini models:
     * - "gemini-3.1-pro-preview" for complex tasks
     * - "gemini-3.5-flash" for general tasks
     * - "gemini-3.1-flash-lite" for ultra-fast tasks
     */
    suspend fun generateChatResponse(
        conversationHistory: List<ChatMessage>,
        modelName: String = "gemini-3.5-flash",
        systemRolePrompt: String = "You are J.A.R.V.I.S., the ultimate intelligent phone automation system. Speak with concise, sophisticated British AI wit and utmost competence."
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            val lastUserMsg = conversationHistory.lastOrNull { it.role == "user" }?.text?.lowercase() ?: ""
            val offlineResponse = when {
                lastUserMsg.contains("who are you") || lastUserMsg.contains("kya ho") ->
                    "I am J.A.R.V.I.S., your autonomous phone automation assistant. All local device actions (WiFi, brightness, alarms, volume, messaging shortcuts, and accessibility automations) are operational.\n\nTo link Google's live neural reasoning models, configure your Gemini API Key in the **System** tab under **Gemini API Key Configuration**."
                lastUserMsg.contains("wifi") || lastUserMsg.contains("brightness") || lastUserMsg.contains("alarm") || lastUserMsg.contains("message") ->
                    "Direct phone automation detected. You can speak or trigger this directly on the Voice HUD tab to execute system workflows.\n\nTo enable cloud conversational AI, please enter your Gemini API Key in the **System** tab."
                else ->
                    "Sir, my core phone automation protocols and local engines are active. To enable live Gemini cloud neural cognition ($modelName), please enter your Gemini API Key in the **System** tab under 'Gemini API Key Configuration', or configure `GEMINI_API_KEY` in the AI Studio Secrets panel."
            }
            return@withContext Result.success(offlineResponse)
        }

        try {
            val root = JSONObject()

            // System Instruction
            val sysInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemRolePrompt))
            sysInstruction.put("parts", sysParts)
            root.put("systemInstruction", sysInstruction)

            // Conversation history
            val contents = JSONArray()
            for (msg in conversationHistory.takeLast(10)) {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.role == "user") "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                contentObj.put("parts", parts)
                contents.put(contentObj)
            }
            root.put("contents", contents)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                if (responseBody.contains("API_KEY_INVALID") || responseBody.contains("API key not valid") || (response.code == 400 && responseBody.contains("API key", ignoreCase = true))) {
                    return@withContext Result.failure(
                        ApiKeyInvalidException("Google rejected the Gemini API key as invalid or expired. Please tap 'Configure Key' to update your API key.")
                    )
                }
                return@withContext Result.failure(Exception("Gemini API error ($modelName): ${response.code} $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text != null && text.isNotBlank()) {
                Result.success(text)
            } else {
                Result.success("Systems operational. Standing by for instructions.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Image Generation using gemini-3-pro-image-preview
     * Affordance for image sizes: "1K", "2K", "4K"
     */
    suspend fun generateImage(
        prompt: String,
        imageSize: String = "1K", // "1K", "2K", "4K"
        aspectRatio: String = "1:1"
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                Exception("Gemini API Key required for image generation. Please configure your API key in the System tab.")
            )
        }

        try {
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            content.put("parts", parts)
            contents.put(content)
            root.put("contents", contents)

            val genConfig = JSONObject()
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", aspectRatio)
            imageConfig.put("imageSize", imageSize)
            genConfig.put("imageConfig", imageConfig)

            val modalities = JSONArray()
            modalities.put("TEXT")
            modalities.put("IMAGE")
            genConfig.put("responseModalities", modalities)
            root.put("generationConfig", genConfig)

            // As per instructions, model is gemini-3-pro-image-preview
            val modelName = "gemini-3-pro-image-preview"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // Try fallback to standard flash image if pro-image preview is unavailable
                return@withContext fallbackFlashImage(prompt, aspectRatio, apiKey)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val partsArr = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")

            var foundBase64: String? = null
            if (partsArr != null) {
                for (i in 0 until partsArr.length()) {
                    val p = partsArr.optJSONObject(i)
                    val inlineData = p?.optJSONObject("inlineData")
                    if (inlineData != null) {
                        foundBase64 = inlineData.optString("data")
                        break
                    }
                }
            }

            if (!foundBase64.isNullOrBlank()) {
                val bytes = Base64.decode(foundBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    return@withContext Result.success(bmp)
                }
            }

            fallbackFlashImage(prompt, aspectRatio, apiKey)
        } catch (e: Exception) {
            fallbackFlashImage(prompt, aspectRatio, apiKey)
        }
    }

    private suspend fun fallbackFlashImage(prompt: String, aspectRatio: String, apiKey: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            content.put("parts", parts)
            contents.put(content)
            root.put("contents", contents)

            val genConfig = JSONObject()
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", aspectRatio)
            genConfig.put("imageConfig", imageConfig)
            val modalities = JSONArray().put("IMAGE")
            genConfig.put("responseModalities", modalities)
            root.put("generationConfig", genConfig)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val partsArr = jsonResponse.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")

            if (partsArr != null) {
                for (i in 0 until partsArr.length()) {
                    val p = partsArr.optJSONObject(i)
                    val data = p?.optJSONObject("inlineData")?.optString("data")
                    if (!data.isNullOrBlank()) {
                        val bytes = Base64.decode(data, Base64.DEFAULT)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) return@withContext Result.success(bmp)
                    }
                }
            }
            Result.failure(Exception("Image generation completed without visual payload"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Text to Speech using model gemini-3.8-flash-tts
     * Falls back to native Android TTS for instant zero-latency speech output
     */
    suspend fun speakText(text: String, voiceName: String = "Kore"): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            speakNativeTts(text)
            return@withContext Result.success(true)
        }

        try {
            // First attempt Gemini 3.8 Flash TTS
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject()
            val parts = JSONArray().put(JSONObject().put("text", text))
            content.put("parts", parts)
            contents.put(content)
            root.put("contents", contents)

            val genConfig = JSONObject()
            genConfig.put("responseModalities", JSONArray().put("AUDIO"))
            val speechConfig = JSONObject()
            val voiceConfig = JSONObject()
            voiceConfig.put("prebuiltVoiceConfig", JSONObject().put("voiceName", voiceName))
            speechConfig.put("voiceConfig", voiceConfig)
            genConfig.put("speechConfig", speechConfig)
            root.put("generationConfig", genConfig)

            val modelName = "gemini-3.8-flash-tts"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val partsArr = json.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                val audioData = partsArr?.optJSONObject(0)?.optJSONObject("inlineData")?.optString("data")

                if (!audioData.isNullOrBlank()) {
                    val pcmBytes = Base64.decode(audioData, Base64.DEFAULT)
                    playPcmAudio(pcmBytes)
                    return@withContext Result.success(true)
                }
            }

            // Fallback to Native Android TTS
            speakNativeTts(text)
            Result.success(true)
        } catch (_: Exception) {
            speakNativeTts(text)
            Result.success(true)
        }
    }

    private fun playPcmAudio(pcmData: ByteArray) {
        try {
            val sampleRate = 24000
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBufferSize, pcmData.size))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()
        } catch (_: Exception) {
            // Handled
        }
    }

    private fun speakNativeTts(text: String) {
        try {
            nativeTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_TTS")
        } catch (_: Exception) {}
    }

    fun stopSpeaking() {
        try {
            nativeTts?.stop()
        } catch (_: Exception) {}
    }

    fun destroy() {
        try {
            nativeTts?.shutdown()
            nativeTts = null
        } catch (_: Exception) {}
    }
}
