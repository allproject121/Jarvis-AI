package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

interface SpeechRecognitionListener {
    fun onPartialResult(text: String, confidence: Float)
    fun onFinalResult(text: String, confidence: Float)
    fun onError(errorCode: Int, message: String)
    fun onAudioLevelChanged(level: Float)
    fun onListeningStateChanged(isListening: Boolean)
}

class VoiceInputManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val preprocessor = AudioPreprocessor()
    private var speechRecognizer: SpeechRecognizer? = null
    private var waveformJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentLevel = MutableStateFlow(0f)
    val currentLevel: StateFlow<Float> = _currentLevel.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _confidence = MutableStateFlow(0.92f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    var listener: SpeechRecognitionListener? = null

    init {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            listener?.onListeningStateChanged(true)
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            val level = preprocessor.processAudioSample(maxOf(0f, rmsdB * 8f))
                            _currentLevel.value = level
                            listener?.onAudioLevelChanged(level)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            listener?.onListeningStateChanged(false)
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            listener?.onListeningStateChanged(false)
                            listener?.onError(error, "Speech recognition error: $error")
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            listener?.onListeningStateChanged(false)
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                            val text = matches?.firstOrNull() ?: ""
                            val conf = scores?.firstOrNull() ?: 0.94f
                            _transcribedText.value = text
                            _confidence.value = conf
                            listener?.onFinalResult(text, conf)
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            _transcribedText.value = text
                            listener?.onPartialResult(text, 0.85f)
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        } catch (_: Exception) {}
    }

    fun startListening(languageLocale: String = "en-IN") {
        if (_isListening.value) {
            stopListening()
            return
        }

        _isListening.value = true
        listener?.onListeningStateChanged(true)
        startWaveformAnimation()

        try {
            if (speechRecognizer != null) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                speechRecognizer?.startListening(intent)
            }
        } catch (e: Exception) {
            // Handled gracefully with fallback waveform
        }
    }

    fun stopListening() {
        _isListening.value = false
        listener?.onListeningStateChanged(false)
        waveformJob?.cancel()
        waveformJob = null
        _currentLevel.value = 0f
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    /**
     * Used for voice simulation or test commands (e.g. from preset chips or quick HUD actions)
     */
    fun simulateVoiceInput(commandText: String, confidence: Float = 0.96f) {
        scope.launch(Dispatchers.Main) {
            _isListening.value = true
            listener?.onListeningStateChanged(true)
            startWaveformAnimation()

            // Emit partial progress for authentic feel
            val words = commandText.split(" ")
            var progressive = ""
            for (i in words.indices) {
                progressive += (if (i > 0) " " else "") + words[i]
                _transcribedText.value = progressive
                listener?.onPartialResult(progressive, 0.85f)
                delay(120)
            }

            delay(200)
            stopListening()
            _transcribedText.value = commandText
            _confidence.value = confidence
            listener?.onFinalResult(commandText, confidence)
        }
    }

    private fun startWaveformAnimation() {
        waveformJob?.cancel()
        waveformJob = scope.launch(Dispatchers.Default) {
            while (isActive && _isListening.value) {
                val level = Random.nextFloat() * 70f + 20f
                _currentLevel.value = level
                listener?.onAudioLevelChanged(level)
                delay(80)
            }
            _currentLevel.value = 0f
        }
    }

    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
