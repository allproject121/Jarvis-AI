package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF002029),
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0A2E5C),
    onSecondaryContainer = Color(0xFFCBE3FF),
    tertiary = JarvisOrange,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF572C00),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = JarvisBackground,
    onBackground = TextPrimary,
    surface = JarvisSurface,
    onSurface = TextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = JarvisCardBorder,
    error = JarvisError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // JARVIS is fundamentally an immersive futuristic sci-fi dark HUD interface
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
