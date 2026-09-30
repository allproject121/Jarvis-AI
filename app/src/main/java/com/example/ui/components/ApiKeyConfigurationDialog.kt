package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
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
fun ApiKeyConfigurationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val isTesting by viewModel.isKeyTesting.collectAsStateWithLifecycle()
    val testStatus by viewModel.keyTestStatus.collectAsStateWithLifecycle()
    val isConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()

    var inputKey by remember(currentKey) { mutableStateOf(currentKey) }
    var keyVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("api_key_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = BorderStroke(1.dp, JarvisCyan)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEURAL CORE LINK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = JarvisCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isConfigured) JarvisSuccess.copy(alpha = 0.2f) else JarvisOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isConfigured) "ACTIVE" else "UNLINKED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfigured) JarvisSuccess else JarvisOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Configure your Google Gemini API key to activate live reasoning models (gemini-3.5-flash, gemini-3.1-pro-preview) and neural image generation.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = {
                        Text("Paste AIzaSy... key", color = TextMuted, fontSize = 13.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle key visibility",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
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

                if (testStatus != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val isSuccess = testStatus?.contains("verified", ignoreCase = true) == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSuccess) JarvisSuccess.copy(alpha = 0.15f) else JarvisOrange.copy(alpha = 0.15f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccess) JarvisSuccess else JarvisOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = testStatus.orEmpty(),
                            fontSize = 11.sp,
                            color = if (isSuccess) JarvisSuccess else JarvisOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_dismiss_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, JarvisCardBorder)
                    ) {
                        Text("Close", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.testAndSaveApiKey(inputKey)
                        },
                        enabled = !isTesting && inputKey.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("dialog_save_api_key_btn")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Verify & Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (inputKey.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            inputKey = ""
                            viewModel.setCustomApiKey("")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_clear_key_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Clear API Key", color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
