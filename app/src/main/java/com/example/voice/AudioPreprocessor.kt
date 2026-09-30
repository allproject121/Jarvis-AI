package com.example.voice

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class AudioPreprocessor {

    private var noiseFloor = 10f
    private val smoothingFactor = 0.2f

    fun processAudioSample(rawAmplitude: Float): Float {
        // Noise suppression
        val clean = max(0f, rawAmplitude - noiseFloor)
        // Adaptive noise floor tracking
        if (rawAmplitude < noiseFloor * 1.5f) {
            noiseFloor = noiseFloor * (1 - smoothingFactor) + rawAmplitude * smoothingFactor
        }
        // Normalize between 0 and 100
        return min(100f, clean * 1.2f)
    }

    fun isVoiceActive(amplitude: Float): Boolean {
        return amplitude > (noiseFloor + 15f)
    }
}
