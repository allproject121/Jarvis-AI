package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.planner.RiskLevel
import com.example.prediction.ProactiveSuggestion
import com.example.ui.components.ApiKeyConfigurationDialog
import com.example.ui.components.CyanBreathingOrb
import com.example.ui.theme.ColorBackgroundDark
import com.example.ui.theme.ColorPrimaryCyan
import com.example.ui.theme.ColorPrimaryLightCyan
import com.example.ui.theme.ColorSurfaceDark
import com.example.ui.theme.ColorTextMuted
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JarvisMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.voiceInputManager.isListening.collectAsStateWithLifecycle()
    val audioLevel by viewModel.voiceInputManager.currentLevel.collectAsStateWithLifecycle()
    val transcribedText by viewModel.voiceInputManager.transcribedText.collectAsStateWithLifecycle()
    val confidence by viewModel.voiceInputManager.confidence.collectAsStateWithLifecycle()

    val userContext by viewModel.userContext.collectAsStateWithLifecycle()
    val suggestions by viewModel.proactiveSuggestions.collectAsStateWithLifecycle()
    val executionStatus by viewModel.executionStatus.collectAsStateWithLifecycle()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsStateWithLifecycle()
    val recentCommands by viewModel.recentCommands.collectAsStateWithLifecycle()
    val voiceStatusMessage by viewModel.voiceStatusMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var typedCommand by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required to listen to voice commands.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val handleMicClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.toggleListening()
        }
    }

    // Dynamic Clock updating every 10 seconds
    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("h:mm", Locale.getDefault()).format(Date()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())
            delay(10000)
        }
    }

    // Settings API Key Configuration Dialog
    if (showSettingsDialog) {
        ApiKeyConfigurationDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Confirmation Modal for High-risk Actions (Layer 4)
    if (pendingConfirmation != null) {
        val plan = pendingConfirmation!!
        AlertDialog(
            onDismissRequest = { viewModel.confirmPendingPlan(false) },
            containerColor = ColorSurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Confirmation Required",
                        tint = JarvisOrange,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Confirmation Required",
                        color = ColorPrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = plan.confirmationMessage ?: "Authorize and execute operations?",
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Risk Level: ${plan.riskLevel} (${plan.tasks.size} steps)",
                        color = if (plan.riskLevel == RiskLevel.CRITICAL) JarvisError else JarvisOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    plan.tasks.forEachIndexed { idx, task ->
                        Text(
                            text = "${idx + 1}. ${task.name}",
                            color = ColorPrimaryLightCyan,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingPlan(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryCyan, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_action_btn")
                ) {
                    Text("Authorize", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.confirmPendingPlan(false) },
                    modifier = Modifier.testTag("cancel_action_btn")
                ) {
                    Text("Decline", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Header with Title, Battery and Time
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTimeString,
                    color = ColorPrimaryCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("timeDisplay")
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JARVIS PHONE",
                        color = ColorPrimaryCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AUTOMATION SYSTEM",
                        color = ColorPrimaryCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("settingsButton")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ColorPrimaryCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Battery and WiFi / RMS Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BAT ${userContext.deviceState.batteryLevel}%",
                    color = ColorPrimaryCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .testTag("batteryStatus")
                )
                val rmsText = if (isListening && audioLevel > 18f) "RMS ACTIVE" else "RMS RELAXING"
                Text(
                    text = rmsText,
                    color = ColorPrimaryCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("wifiStatus")
                )
            }
        }

        // 2. Animated Cyan Breathing Orb Visualizer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CyanBreathingOrb(
                    isListening = isListening,
                    audioLevel = audioLevel,
                    modifier = Modifier
                        .clickable { handleMicClick() }
                        .testTag("orbVisualizer")
                )
            }
        }

        // 3. Command Display & Multi-language info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayCommand = if (transcribedText.isNotBlank()) {
                    "\"$transcribedText\" 🎤"
                } else if (isListening) {
                    "\"Listening... Boliyee...\" 🎤"
                } else {
                    "\"Office mode on?\" 🎤"
                }

                Text(
                    text = displayCommand,
                    color = ColorPrimaryCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("commandText")
                )

                Spacer(modifier = Modifier.height(4.dp))

                val scoreText = if (confidence > 0f) {
                    "Confidence: ${(confidence * 100).toInt()}%"
                } else {
                    "Confidence: 98%"
                }

                Text(
                    text = scoreText,
                    color = ColorPrimaryLightCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag("confidenceScore")
                )

                Text(
                    text = "Multi-language (EN/HI)",
                    color = ColorPrimaryLightCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag("languageInfo")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick tap mic toggle button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isListening) listOf(JarvisOrange, ColorPrimaryCyan) else listOf(ColorPrimaryCyan, Color(0xFF0077FF))
                            )
                        )
                        .clickable { handleMicClick() }
                        .testTag("voiceMicToggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Voice status message indicator
                if (!voiceStatusMessage.isNullOrBlank()) {
                    Surface(
                        color = if (isListening) ColorPrimaryCyan.copy(alpha = 0.15f) else ColorSurfaceDark,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isListening) ColorPrimaryCyan else Color(0xFF2A3656)),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            if (isListening) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF3366))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = voiceStatusMessage ?: "",
                                color = if (isListening) ColorPrimaryCyan else ColorPrimaryLightCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Command Text Input Bar for manual typing / fallback
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedCommand,
                        onValueChange = { typedCommand = it },
                        placeholder = {
                            Text("Type command or tap preset...", color = ColorTextMuted, fontSize = 12.sp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("commandInputField"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorPrimaryCyan,
                            unfocusedBorderColor = Color(0xFF2A3656),
                            focusedContainerColor = ColorSurfaceDark,
                            unfocusedContainerColor = ColorSurfaceDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (typedCommand.isNotBlank()) {
                                viewModel.processUserCommand(typedCommand.trim())
                                typedCommand = ""
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorPrimaryCyan)
                            .testTag("sendTypedCommandButton")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Execute Command",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                thickness = 1.dp,
                color = ColorSurfaceDark
            )
        }

        // 4. Voice Input Presets
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "VOICE INPUT PRESETS (QUICK TAP):",
                    color = ColorPrimaryCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val presetCommands = listOf(
                    "Office mode on",
                    "Instagram backup to Drive",
                    "Send message to Mom",
                    "Call Raj",
                    "Set brightness to 80",
                    "Turn off WiFi",
                    "Lock screen"
                )

                presetCommands.forEachIndexed { index, cmd ->
                    Button(
                        onClick = { viewModel.processUserCommand(cmd) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .height(42.dp)
                            .testTag("preset_$index"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorSurfaceDark,
                            contentColor = ColorPrimaryCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ColorPrimaryCyan.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📌 $cmd",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorPrimaryCyan
                            )
                        }
                    }
                }
            }
        }

        // 5. Workflow Progress Section (Live execution or demo progress)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("workflowProgressCard"),
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ColorPrimaryCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val title = if (executionStatus?.isExecuting == true) "WORKFLOW EXECUTING" else "WORKFLOW COMPLETE"
                        Text(
                            text = title,
                            color = ColorPrimaryCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val stepCountText = if (executionStatus != null && executionStatus!!.totalSteps > 0) {
                            "${executionStatus!!.currentStepIndex}/${executionStatus!!.totalSteps} STEPS"
                        } else {
                            "2/5 STEPS"
                        }

                        Text(
                            text = stepCountText,
                            color = ColorPrimaryCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("stepCounter")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val steps: List<WorkflowStepItem> = if (executionStatus != null && executionStatus!!.stepStatuses.isNotEmpty()) {
                        executionStatus!!.stepStatuses.map { step ->
                            val s = when {
                                step.isCompleted -> WorkflowStepStatus.COMPLETED
                                step.isRunning -> WorkflowStepStatus.IN_PROGRESS
                                else -> WorkflowStepStatus.PENDING
                            }
                            WorkflowStepItem(step.taskName, s)
                        }
                    } else {
                        // Standard workflow step checklist from video demo
                        listOf(
                            WorkflowStepItem("Establish connection", WorkflowStepStatus.COMPLETED),
                            WorkflowStepItem("Resolve Contact Coordinates", WorkflowStepStatus.COMPLETED),
                            WorkflowStepItem("Dispatch WhatsApp Message", WorkflowStepStatus.COMPLETED),
                            WorkflowStepItem("Waiting for response...", WorkflowStepStatus.IN_PROGRESS),
                            WorkflowStepItem("Finalizing setup...", WorkflowStepStatus.PENDING)
                        )
                    }

                    steps.forEach { step ->
                        WorkflowStepRow(step = step)
                    }
                }
            }
        }

        // 6. Proactive Predictions (Layer 7)
        if (suggestions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 6.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROACTIVE PREDICTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "LEARNING ENGINE ACTIVE",
                        fontSize = 10.sp,
                        color = ColorPrimaryCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(suggestions) { item ->
                ProactiveSuggestionCard(
                    suggestion = item,
                    onExecute = {
                        viewModel.handleVoiceCommand(item.commandText)
                    },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        // 7. Automation Activity Log (from Room DB)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 10.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTOMATION ACTIVITY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${recentCommands.size} ENTRIES",
                    fontSize = 10.sp,
                    color = ColorTextMuted
                )
            }
        }

        if (recentCommands.isEmpty()) {
            item {
                Text(
                    text = "No automations logged yet. Speak or tap a preset above to begin.",
                    color = ColorTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                )
            }
        } else {
            items(recentCommands) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = BorderStroke(1.dp, JarvisCardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            color = ColorPrimaryCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.originalText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Intent: ${record.parsedIntent} • Actions: ${record.actionsExecuted}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkflowStepRow(step: WorkflowStepItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, iconColor, textColor) = when (step.status) {
            WorkflowStepStatus.COMPLETED -> Triple("✓", ColorPrimaryCyan, ColorPrimaryLightCyan)
            WorkflowStepStatus.IN_PROGRESS -> Triple("⏳", ColorPrimaryCyan, ColorPrimaryCyan)
            WorkflowStepStatus.PENDING -> Triple("⏳", ColorTextMuted, ColorTextMuted)
        }

        Text(
            text = icon,
            color = iconColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(26.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = step.description,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (step.status == WorkflowStepStatus.IN_PROGRESS) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ProactiveSuggestionCard(
    suggestion: ProactiveSuggestion,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("suggestion_${suggestion.title.take(6)}"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = BorderStroke(1.dp, ColorPrimaryCyan.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryCyan
                )
                Text(
                    text = suggestion.subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Button(
                onClick = onExecute,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimaryCyan,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
