package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.GituImageUrls
import com.example.ui.components.AgentAvatar
import com.example.ui.components.GituBackButton
import com.example.ui.components.GituBadgePill
import com.example.ui.components.GituCircleIconButton
import com.example.ui.components.GituSurfaceCard
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors
import com.example.viewmodel.AgentGituUiState

// ==========================================
// Screen 10: LiveKit Voice Call
// ==========================================
@Composable
fun LiveKitVoiceCallScreen(
    contextName: String,
    uiState: AgentGituUiState,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onMinimize: () -> Unit,
    onEndCall: () -> Unit,
    onSelectMode: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current

    LaunchedEffect(Unit) {
        onStartTimer()
    }
    DisposableEffect(Unit) {
        onDispose { onStopTimer() }
    }

    val minutes = uiState.voiceCallSeconds / 60
    val seconds = uiState.voiceCallSeconds % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)

    val avatarUrl = when {
        contextName.contains("Kai", ignoreCase = true) -> GituImageUrls.KAI_AVATAR
        contextName.contains("Maya", ignoreCase = true) -> GituImageUrls.MAYA_AVATAR
        contextName.contains("Sora", ignoreCase = true) -> GituImageUrls.SORA_AVATAR
        else -> GituImageUrls.CHIEF_AVATAR
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Call Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GituCircleIconButton(
                icon = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Minimize voice call",
                onClick = onMinimize,
                testTag = "voice_minimize_button"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LiveKit WebRTC", style = GituMonoSm, color = gituColors.success)
                Text(formattedTime, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(gituColors.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.GraphicEq,
                    contentDescription = "Noise suppression active",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bound Teammate & Topic
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(8.dp)
            ) {
                AgentAvatar(
                    imageUrl = avatarUrl,
                    fallbackInitials = contextName,
                    contentDescription = contextName,
                    size = 96.dp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(contextName, style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "Bound to Core Product Team • ${uiState.activeGroupTopic}",
                    style = GituMonoSm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Ask / Steer / Queue Switcher + Transcript Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(color = gituColors.surfaceContainerLow, shape = CircleShape) {
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    listOf("Ask", "Steer", "Queue").forEach { mode ->
                        val selected = uiState.voiceCallMode == mode
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable { onSelectMode(mode) }
                                .testTag("voice_mode_${mode.lowercase()}")
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Live transcript (saved to chat)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.voiceTranscript,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Call Controls Footer
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (uiState.voiceCallMuted) MaterialTheme.colorScheme.secondaryContainer else gituColors.surfaceContainerLow,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleMute)
                    .testTag("voice_mute_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (uiState.voiceCallMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Mute microphone",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEndCall)
                    .testTag("voice_end_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End voice call",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Surface(
                color = if (uiState.voiceCallSpeaker) MaterialTheme.colorScheme.secondaryContainer else gituColors.surfaceContainerLow,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleSpeaker)
                    .testTag("voice_speaker_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (uiState.voiceCallSpeaker) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = "Toggle speaker",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// Screen 11: Chief of Staff Governance
// ==========================================
@Composable
fun GovernanceScreen(
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onToggleRule: (String) -> Unit,
    onAddRule: (String, String) -> Unit,
    onRevokeAudit: (String) -> Unit,
    onApproveGate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var rulePattern by remember { mutableStateOf("") }
    var ruleDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GituBackButton(onClick = onBack, testTag = "governance_back_button")
                AgentAvatar(
                    imageUrl = GituImageUrls.CHIEF_AVATAR,
                    fallbackInitials = "CH",
                    contentDescription = "Chief of Staff",
                    size = 36.dp
                )
                Column {
                    Text("Chief of Staff", style = MaterialTheme.typography.titleMedium)
                    Text("Governance & rules", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = { showAddRuleDialog = true },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("governance_add_rule_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Rule", style = MaterialTheme.typography.labelSmall)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2-Column Summary Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Auto-approvals", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${uiState.autoApprovalsToday} today", style = MaterialTheme.typography.headlineSmall, color = gituColors.success)
                        }
                    }
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        val waitingCount = if (uiState.pendingApproval.isApproved) 0 else 1
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Escalations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$waitingCount waiting",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (waitingCount > 0) MaterialTheme.colorScheme.tertiary else gituColors.success
                            )
                        }
                    }
                }
            }

            // Standing Rules
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Standing rules", style = MaterialTheme.typography.titleMedium)
                    GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        uiState.governanceRules.forEachIndexed { idx, rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleRule(rule.id) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(rule.pattern, style = GituMonoSm)
                                    Text(rule.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Checkbox(
                                    checked = rule.enabled,
                                    onCheckedChange = { onToggleRule(rule.id) }
                                )
                            }
                            if (idx < uiState.governanceRules.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }

            // Decision Audit Log
            item {
                Text("Decision audit log", style = MaterialTheme.typography.titleMedium)
            }

            items(uiState.auditLogs, key = { it.id }) { entry ->
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.labelLarge)
                            Text(entry.actorAndTime, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        if (entry.isGate) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                shape = CircleShape,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onApproveGate)
                                    .testTag("audit_gate_button")
                            ) {
                                Text("Gate", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        } else if (entry.isRevoked) {
                            GituBadgePill(
                                text = "Revoked",
                                containerColor = gituColors.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                shape = CircleShape,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onRevokeAudit(entry.id) }
                            ) {
                                Text("Revoke", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Add standing rule", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rulePattern,
                        onValueChange = { rulePattern = it },
                        label = { Text("Command or prompt pattern") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ruleDesc,
                        onValueChange = { ruleDesc = it },
                        label = { Text("Policy action") },
                        placeholder = { Text("Auto-approve • Read-only") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddRule(rulePattern, ruleDesc)
                    rulePattern = ""
                    ruleDesc = ""
                    showAddRuleDialog = false
                }) { Text("Save rule") }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) { Text("Cancel") }
            }
        )
    }
}
