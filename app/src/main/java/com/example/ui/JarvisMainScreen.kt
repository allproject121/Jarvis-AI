package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.planner.RiskLevel
import com.example.prediction.ProactiveSuggestion
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.HudStatusChip
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    val presetPrompts = listOf(
        "Mom को message भेज",
        "WiFi on कर",
        "Brightness 80 कर",
        "कल 9 बजे alarm set कर",
        "Instagram backup to Drive",
        "Office mode on कर"
    )

    // Confirmation Modal for High-risk Actions
    if (pendingConfirmation != null) {
        val plan = pendingConfirmation!!
        AlertDialog(
            onDismissRequest = { viewModel.confirmPendingPlan(false) },
            containerColor = JarvisSurface,
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
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = plan.confirmationMessage ?: "Execute planned operations?",
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
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingPlan(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
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
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Top HUD System Diagnostics Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "J.A.R.V.I.S.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = JarvisCyan,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "PHONE AUTOMATION SYSTEM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HudStatusChip(
                        label = "BAT",
                        value = "${userContext.deviceState.batteryLevel}%",
                        color = if (userContext.deviceState.batteryLevel <= 20) JarvisError else JarvisSuccess
                    )
                    HudStatusChip(
                        label = "SYS",
                        value = userContext.detectedActivity.name,
                        color = JarvisCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Central Arc Reactor & Voice Visualizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArcReactorCore(
                        isListening = isListening,
                        audioLevel = audioLevel,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    AudioWaveformVisualizer(
                        isListening = isListening,
                        audioLevel = audioLevel
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isListening) "LISTENING TO VOICE INPUT..." else if (transcribedText.isNotBlank()) "\"$transcribedText\"" else "SYSTEM STANDING BY",
                        color = if (isListening) JarvisCyan else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (transcribedText.isNotBlank()) {
                        Text(
                            text = "Confidence: ${(confidence * 100).toInt()}% • Multi-language (EN/HI)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Floating Mic Button with Glow
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isListening) listOf(JarvisOrange, JarvisCyan) else listOf(JarvisCyan, Color(0xFF0077FF))
                                )
                            )
                            .clickable {
                                if (isListening) {
                                    viewModel.voiceInputManager.stopListening()
                                } else {
                                    viewModel.voiceInputManager.startListening()
                                }
                            }
                            .testTag("jarvis_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Quick Command Simulation Presets
        item {
            Text(
                text = "VOICE INPUT PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(presetPrompts) { prompt ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.voiceInputManager.simulateVoiceInput(prompt)
                            }
                            .testTag("preset_${prompt.take(6)}"),
                        color = JarvisSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prompt,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. Live Workflow Execution Status
        if (executionStatus != null) {
            item {
                val status = executionStatus!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("execution_status_card"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (status.isExecuting) JarvisCyan else if (status.success) JarvisSuccess else JarvisError
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (status.isExecuting) Icons.Default.Bolt else if (status.success) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (status.isExecuting) JarvisCyan else if (status.success) JarvisSuccess else JarvisError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (status.isExecuting) "EXECUTING WORKFLOW" else "WORKFLOW COMPLETE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${status.currentStepIndex}/${status.totalSteps} STEPS",
                                fontSize = 12.sp,
                                color = JarvisCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = status.statusMessage,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (status.totalSteps > 0) status.currentStepIndex.toFloat() / status.totalSteps.toFloat() else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JarvisCyan,
                            trackColor = JarvisSurfaceVariant
                        )

                        // Render Step checklist
                        Spacer(modifier = Modifier.height(10.dp))
                        status.stepStatuses.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (step.isCompleted) JarvisSuccess else if (step.isRunning) JarvisCyan else Color.Gray
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.taskName,
                                    fontSize = 12.sp,
                                    color = if (step.isRunning) JarvisCyan else if (step.isCompleted) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Proactive Suggestions Section (Layer 7)
        if (suggestions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp),
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
                        color = JarvisCyan,
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
            item {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 6. Recent Activity Log (from Room DB)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp),
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
                    color = TextMuted
                )
            }
        }

        if (recentCommands.isEmpty()) {
            item {
                Text(
                    text = "No automations logged yet. Speak or tap a preset above to begin.",
                    color = TextMuted,
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.originalText,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = record.actionsExecuted.ifEmpty { record.parsedIntent },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (record.success) JarvisSuccess.copy(alpha = 0.15f) else JarvisError.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (record.success) "SUCCESS" else "FAILED",
                                    color = if (record.success) JarvisSuccess else JarvisError,
                                    fontSize = 10.sp,
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

@Composable
fun ProactiveSuggestionCard(
    suggestion: ProactiveSuggestion,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("suggestion_${suggestion.id}"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (suggestion.isCritical) JarvisOrange else JarvisCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = suggestion.title,
                        fontWeight = FontWeight.Bold,
                        color = if (suggestion.isCritical) JarvisOrange else TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(suggestion.confidence * 100).toInt()}% match",
                        fontSize = 10.sp,
                        color = JarvisCyan
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = suggestion.subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onExecute,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (suggestion.isCritical) JarvisOrange else JarvisCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("exec_${suggestion.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Execute",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Run",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
