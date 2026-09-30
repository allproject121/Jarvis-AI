package com.example.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.execution.JarvisAccessibilityService
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SystemHudScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userContext by viewModel.userContext.collectAsStateWithLifecycle()
    val entities by viewModel.entityRecords.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
    val isKeyTesting by viewModel.isKeyTesting.collectAsStateWithLifecycle()
    val keyTestStatus by viewModel.keyTestStatus.collectAsStateWithLifecycle()

    var inputKey by remember(customApiKey) { mutableStateOf(customApiKey) }
    var keyVisible by remember { mutableStateOf(false) }

    var brightnessSlider by remember { mutableFloatStateOf(75f) }
    var volumeSlider by remember { mutableFloatStateOf(65f) }

    val isAccessibilityActive = JarvisAccessibilityService.isServiceRunning

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "SYSTEM HUD & TELEMETRY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = JarvisCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "Real-time hardware sensors, accessibility bridges & entity memory",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Gemini Neural Core API Key Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_key_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isApiKeyConfigured) JarvisSuccess else JarvisOrange
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isApiKeyConfigured) JarvisSuccess else JarvisOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI NEURAL API KEY",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isApiKeyConfigured) JarvisSuccess.copy(alpha = 0.2f) else JarvisOrange.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isApiKeyConfigured) "CONFIGURED" else "NOT SET",
                                color = if (isApiKeyConfigured) JarvisSuccess else JarvisOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your Google Gemini API key below to link live models (gemini-3.5-flash, gemini-3.1-pro-preview, gemini-3-pro-image-preview).",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        placeholder = { Text("Paste AIzaSy... Gemini API key", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle key visibility",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = JarvisSurfaceVariant,
                            unfocusedContainerColor = JarvisSurfaceVariant
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (keyTestStatus != null) {
                        val isSuccess = keyTestStatus?.contains("verified", ignoreCase = true) == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSuccess) JarvisSuccess.copy(alpha = 0.15f) else JarvisOrange.copy(alpha = 0.15f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = keyTestStatus.orEmpty(),
                                fontSize = 11.sp,
                                color = if (isSuccess) JarvisSuccess else JarvisOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputKey.isNotBlank()) {
                                    viewModel.testAndSaveApiKey(inputKey)
                                } else {
                                    viewModel.setCustomApiKey("")
                                    Toast.makeText(context, "API Key cleared", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isKeyTesting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_api_key_btn")
                        ) {
                            if (isKeyTesting) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verifying...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Verify & Activate Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (inputKey.isNotBlank()) {
                            Button(
                                onClick = {
                                    inputKey = ""
                                    viewModel.setCustomApiKey("")
                                    Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisSurfaceVariant,
                                    contentColor = JarvisOrange
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Tip: You can also configure GEMINI_API_KEY in the Secrets panel in Google AI Studio.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Accessibility Service Bridge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accessibility_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAccessibilityActive) JarvisSuccess else JarvisOrange
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Accessibility,
                                contentDescription = null,
                                tint = if (isAccessibilityActive) JarvisSuccess else JarvisOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACCESSIBILITY AUTOMATION",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isAccessibilityActive) JarvisSuccess.copy(alpha = 0.2f) else JarvisOrange.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAccessibilityActive) "ACTIVE" else "STANDBY",
                                color = if (isAccessibilityActive) JarvisSuccess else JarvisOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Allows JARVIS to read on-screen elements, navigate between apps, and simulate taps/keystrokes for complex multi-step workflows.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisSurfaceVariant,
                            contentColor = JarvisCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_accessibility_btn")
                    ) {
                        Text("Configure Accessibility Bridge in Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hardware Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HARDWARE EXECUTION CONTROLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Brightness slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BrightnessMedium, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Screen Brightness", fontSize = 13.sp, color = TextPrimary)
                        }
                        Text("${brightnessSlider.toInt()}%", fontSize = 12.sp, color = JarvisCyan, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = brightnessSlider,
                        onValueChange = {
                            brightnessSlider = it
                            viewModel.handleVoiceCommand("Brightness ${it.toInt()} कर")
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceVariant
                        ),
                        modifier = Modifier.testTag("brightness_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Volume slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sound Volume", fontSize = 13.sp, color = TextPrimary)
                        }
                        Text("${volumeSlider.toInt()}%", fontSize = 12.sp, color = JarvisCyan, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = volumeSlider,
                        onValueChange = {
                            volumeSlider = it
                            viewModel.handleVoiceCommand("Volume ${it.toInt()} कर")
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceVariant
                        ),
                        modifier = Modifier.testTag("volume_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick buttons for WiFi and Ringer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.handleVoiceCommand("WiFi toggle") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant, contentColor = TextPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WiFi Panel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.handleVoiceCommand("Silent mode enable") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant, contentColor = TextPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Silent Mode", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Entity Resolution Database Inspector (Layer 3)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STORED ENTITY MAPPINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${entities.size} ENTITIES",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        items(entities) { entity ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entity.entityName,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = entity.entityType,
                                fontSize = 10.sp,
                                color = JarvisCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Aliases: ${entity.aliases}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Resolved: ${entity.resolvedValue}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = "${entity.accessCount} uses",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
