package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.components.ApiKeyConfigurationDialog
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Key

@Composable
fun ImageStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val generatedImages by viewModel.generatedImages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingImage.collectAsStateWithLifecycle()
    val selectedResolution by viewModel.selectedImageResolution.collectAsStateWithLifecycle()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsStateWithLifecycle()

    var promptText by remember { mutableStateOf("Futuristic JARVIS holographic interface with glowing cyber circuitry") }
    var selectedRatio by remember { mutableStateOf("1:1") }

    if (showApiKeyDialog) {
        ApiKeyConfigurationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeApiKeyDialog() }
        )
    }

    val resolutionOptions = listOf("1K", "2K", "4K")
    val ratioOptions = listOf("1:1", "16:9", "4:3")

    val samplePrompts = listOf(
        "Futuristic JARVIS holographic interface with glowing cyber circuitry",
        "Tony Stark AI Arc Reactor core pulsing neon cyan energy in dark armor",
        "Sleek autonomous cybernetic device HUD with data telemetry",
        "JARVIS orbital defense satellite matrix in high-tech sci-fi style"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEURAL VISUAL STUDIO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isApiKeyConfigured) JarvisSuccess.copy(alpha = 0.15f) else JarvisOrange.copy(alpha = 0.2f))
                        .clickable { viewModel.openApiKeyDialog() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("image_studio_api_key_pill")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Key",
                        tint = if (isApiKeyConfigured) JarvisSuccess else JarvisOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isApiKeyConfigured) "LINKED" else "SET KEY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isApiKeyConfigured) JarvisSuccess else JarvisOrange
                    )
                }
            }
            Text(
                text = "Model: gemini-3-pro-image-preview • Ultra high resolution rendering",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (!isApiKeyConfigured) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("image_studio_key_prompt"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisOrange)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GEMINI API KEY REQUIRED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisOrange
                            )
                            Text(
                                text = "Image generation models require an active Gemini API key.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(
                            onClick = { viewModel.openApiKeyDialog() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Configure", fontSize = 11.sp, color = JarvisCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Generator Studio Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IMAGE RESOLUTION AFFORDANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1K, 2K, 4K Affordance selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        resolutionOptions.forEach { res ->
                            val isSelected = selectedResolution == res
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setImageResolution(res) }
                                    .testTag("res_option_$res"),
                                color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) JarvisCyan else JarvisCardBorder
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$res Resolution",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) JarvisCyan else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ASPECT RATIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ratioOptions.forEach { ratio ->
                            val isSelected = selectedRatio == ratio
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedRatio = ratio },
                                color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) JarvisCyan else JarvisCardBorder
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ratio,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) JarvisCyan else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text("Prompt description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = JarvisSurfaceVariant,
                            unfocusedContainerColor = JarvisSurfaceVariant
                        ),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset prompt chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(samplePrompts) { sample ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { promptText = sample },
                                color = JarvisSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                            ) {
                                Text(
                                    text = sample.take(28) + "…",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.generateImage(prompt = promptText, aspectRatio = selectedRatio)
                        },
                        enabled = !isGenerating && promptText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("render_image_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = Color.Black,
                            disabledContainerColor = JarvisSurfaceVariant,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Rendering $selectedResolution Asset via Gemini...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Render Image ($selectedResolution)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Rendered Visual Gallery
        item {
            Text(
                text = "GENERATED ASSETS GALLERY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        if (generatedImages.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No rendered assets yet", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "Select resolution (1K, 2K, 4K) above and tap Render Image.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(generatedImages) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("generated_image_card"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Image(
                            bitmap = item.bitmap.asImageBitmap(),
                            contentDescription = item.prompt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.prompt,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${item.resolution} HD",
                                    color = JarvisCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
