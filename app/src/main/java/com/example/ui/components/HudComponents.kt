package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSurfaceVariant
import kotlin.math.sin

@Composable
fun ArcReactorCore(
    isListening: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 500 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.85f * pulseScale

            // Outer glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(JarvisCyanGlow, Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.25f
                ),
                radius = baseRadius * 1.25f,
                center = center
            )

            // Outer cyan border
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.6f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Middle segmented automation ring
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.3f),
                radius = baseRadius * 0.72f,
                center = center,
                style = Stroke(width = 2f)
            )

            // Dynamic audio responsive ring
            val audioBoost = (audioLevel / 100f).coerceIn(0f, 1f) * 15f
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.8f),
                radius = (baseRadius * 0.5f) + audioBoost,
                center = center,
                style = Stroke(width = 3.5f)
            )

            // Inner Core Arc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, JarvisCyan),
                    center = center,
                    radius = baseRadius * 0.3f
                ),
                radius = baseRadius * 0.28f,
                center = center
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isListening: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 24.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = 36
        val barWidth = width / (barCount * 1.6f)
        val amplitude = if (isListening) (audioLevel.coerceIn(15f, 100f) / 100f) * (height / 2f) else 4f

        for (i in 0 until barCount) {
            val x = i * (barWidth * 1.6f) + barWidth / 2f
            val waveOffset = sin(phase + (i * 0.25f))
            val barHeight = (amplitude * (0.3f + 0.7f * waveOffset.toFloat())).coerceAtLeast(3f)

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(JarvisCyan, Color(0xFF0077FF))
                ),
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HudStatusChip(
    label: String,
    value: String,
    color: Color = JarvisCyan,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(JarvisSurfaceVariant.copy(alpha = 0.8f))
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Animated Cyan Breathing Orb as demonstrated in the automation system video.
 * Features concentric pulsing rings with alpha falloff and a radiant cyan radial core.
 */
@Composable
fun CyanBreathingOrb(
    isListening: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyan_breathing_orb")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 1200 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseScale = size.minDimension / 400f
            val audioBoost = if (isListening) (audioLevel.coerceIn(0f, 100f) / 100f) * 16f else 0f

            // 1. Draw concentric outer breathing rings
            for (i in 3 downTo 1) {
                val sinWave = kotlin.math.sin(time + (i * 0.4f)).toFloat()
                val radius = (50f + i * 25f + (sinWave * 5f) + (audioBoost * i * 0.5f)) * baseScale * 2f
                val alpha = (0.35f / i).coerceIn(0.08f, 0.5f)
                drawCircle(
                    color = Color(0xFF00D4FF).copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )
            }

            // 2. Draw center radiant orb with radial gradient
            val pulse = kotlin.math.sin(time * 2f).toFloat() * 3f + (audioBoost * 0.8f)
            val orbRadius = (32f + pulse) * baseScale * 2f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00D4FF),
                        Color(0x4D0096C8),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius * 1.6f
                ),
                radius = orbRadius * 1.6f,
                center = center
            )

            // Inner glowing core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF00D4FF)),
                    center = center,
                    radius = orbRadius
                ),
                radius = orbRadius,
                center = center
            )
        }
    }
}

