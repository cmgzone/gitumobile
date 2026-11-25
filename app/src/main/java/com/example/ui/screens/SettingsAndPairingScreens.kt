package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.GituBackButton
import com.example.ui.components.GituBadgePill
import com.example.ui.components.GituSurfaceCard
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors
import com.example.viewmodel.AgentGituUiState

// ==========================================
// Screen 12: Settings & Server Pairing
// ==========================================
@Composable
fun SettingsScreen(
    uiState: AgentGituUiState,
    onLockApp: () -> Unit,
    onOpenServerPairing: () -> Unit,
    onOpenGovernance: () -> Unit,
    onOpenLocalPcLink: () -> Unit,
    onOpenConnections: () -> Unit,
    onToggleFcm: () -> Unit,
    onUpdateSpendCeiling: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var showSchedulesDialog by remember { mutableStateOf(false) }
    var showModelsDialog by remember { mutableStateOf(false) }
    var showSpendDialog by remember { mutableStateOf(false) }
    var spendInput by remember(uiState.spendCeilingUsd) { mutableStateOf(uiState.spendCeilingUsd) }

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
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
            Surface(
                color = gituColors.surfaceContainerLow,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onLockApp)
                    .testTag("settings_lock_app_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Lock app", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Server Card
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(gituColors.success))
                                    Text(
                                        text = if (uiState.serverModeLan) "Local Network (LAN)" else "Remote HTTPS Host",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Text(uiState.serverUrl, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            GituBadgePill(
                                text = "Online",
                                containerColor = gituColors.successContainer,
                                contentColor = gituColors.onSuccessContainer
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onOpenServerPairing,
                                shape = CircleShape,
                                modifier = Modifier.weight(1f).testTag("settings_scan_qr_button")
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Scan QR", style = MaterialTheme.typography.labelSmall)
                            }
                            Surface(
                                color = gituColors.surfaceContainerLow,
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable(onClick = onOpenServerPairing)
                                    .testTag("settings_switch_host_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Switch host", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Governance & Bridges 2x2 Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsBridgeTile(
                            icon = Icons.Filled.Shield,
                            title = "Chief of Staff",
                            subtitle = "${uiState.governanceRules.count { it.enabled }} standing rules",
                            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onOpenGovernance,
                            testTag = "settings_tile_governance",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        SettingsBridgeTile(
                            icon = Icons.Filled.Link,
                            title = "Local PC Link",
                            subtitle = if (uiState.localPcConnected) "Paired • Read/Write" else "Unpaired",
                            subtitleColor = if (uiState.localPcConnected) gituColors.success else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onOpenLocalPcLink,
                            testTag = "settings_tile_local_pc",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsBridgeTile(
                            icon = Icons.Filled.Extension,
                            title = "Apps & Gateways",
                            subtitle = "Telegram, Mail, MCP",
                            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onOpenConnections,
                            testTag = "settings_tile_connections",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        SettingsBridgeTile(
                            icon = Icons.Filled.Schedule,
                            title = "Schedules",
                            subtitle = "${uiState.activeSchedulesCount} active jobs",
                            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { showSchedulesDialog = true },
                            testTag = "settings_tile_schedules",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }

            // Models, Autonomy & Mobile Push
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showModelsDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Models & fallbacks", style = MaterialTheme.typography.labelLarge)
                            Text(uiState.modelFallbackChain, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSpendDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Spend ceiling", style = MaterialTheme.typography.labelLarge)
                            Text("${uiState.spendCurrentUsd} / ${uiState.spendCeilingUsd} USD", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(color = gituColors.surfaceContainerLow, shape = CircleShape) {
                            Text("Edit", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleFcm)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Push alerts (FCM)", style = MaterialTheme.typography.labelLarge)
                            Text("Gates, completions & Cowork replies", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Checkbox(checked = uiState.fcmPushEnabled, onCheckedChange = { onToggleFcm() })
                    }
                }
            }
        }
    }

    if (showSchedulesDialog) {
        AlertDialog(
            onDismissRequest = { showSchedulesDialog = false },
            title = { Text("Active schedules (${uiState.activeSchedulesCount})", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "• 09:00 Daily — Partner Gmail digest (Maya)\n• Every 15m — Server /healthz & FCM heartbeat\n• 02:00 Nightly — Regression & Playwright suite",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = { Button(onClick = { showSchedulesDialog = false }) { Text("Done") } }
        )
    }

    if (showModelsDialog) {
        AlertDialog(
            onDismissRequest = { showModelsDialog = false },
            title = { Text("Model fallback chain", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Primary: Anthropic (claude-3-7)\nFallback 1: OpenAI (gpt-4o)\nFallback 2: DeepSeek (deepseek-r1)",
                    style = GituMonoSm
                )
            },
            confirmButton = { Button(onClick = { showModelsDialog = false }) { Text("Close") } }
        )
    }

    if (showSpendDialog) {
        AlertDialog(
            onDismissRequest = { showSpendDialog = false },
            title = { Text("Daily spend ceiling", style = MaterialTheme.typography.headlineSmall) },
            text = {
                OutlinedTextField(
                    value = spendInput,
                    onValueChange = { spendInput = it },
                    label = { Text("Max daily USD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateSpendCeiling(spendInput)
                    showSpendDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSpendDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsBridgeTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    GituSurfaceCard(modifier = modifier.testTag(testTag), onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = GituMonoSm, color = subtitleColor)
        }
    }
}

// ==========================================
// Screen 13: Local PC Link
// ==========================================
@Composable
fun LocalPcLinkScreen(
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onToggleConnection: () -> Unit,
    onPingLink: () -> Unit,
    onTogglePolicy: (String) -> Unit,
    onAddFolder: (String, Boolean) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onCopyCodeFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    val clipboard = LocalClipboardManager.current
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var folderPath by remember { mutableStateOf("~/Projects/") }
    var folderReadOnly by remember { mutableStateOf(false) }

    val activeCount = listOf(
        uiState.allowReadFiles,
        uiState.allowWriteFiles,
        uiState.allowRunCommands,
        uiState.allowOpenUrls,
        uiState.allowAgentChat
    ).count { it }

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
                GituBackButton(onClick = onBack, testTag = "pc_link_back_button")
                Column {
                    Text("Local PC Link", style = MaterialTheme.typography.titleMedium)
                    Text("Workstation bridge", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = onToggleConnection,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("pc_link_pair_button")
            ) {
                Icon(Icons.Filled.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pair PC", style = MaterialTheme.typography.labelSmall)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Paired Peer Status Card
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LaptopMac, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(uiState.localPcName, style = MaterialTheme.typography.titleMedium)
                                    Text("WebSocket • ${uiState.localPcLatencyMs}ms", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            GituBadgePill(
                                text = if (uiState.localPcConnected) "Connected" else "Revoked",
                                containerColor = if (uiState.localPcConnected) gituColors.successContainer else MaterialTheme.colorScheme.errorContainer,
                                contentColor = if (uiState.localPcConnected) gituColors.onSuccessContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = gituColors.surfaceContainerLow,
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable(onClick = onPingLink)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Test link", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable(onClick = onToggleConnection)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (uiState.localPcConnected) "Revoke" else "Reconnect", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // One-time pairing code
                        Surface(
                            color = gituColors.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("One-time pairing code", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(uiState.localPcPairingCode, style = GituMonoSm)
                                }
                                Surface(
                                    color = gituColors.surfaceContainerLowest,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            clipboard.setText(AnnotatedString(uiState.localPcPairingCode))
                                            onCopyCodeFeedback("Copied ${uiState.localPcPairingCode}")
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Copy", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Granular Local Policy Controls
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Local policy controls", style = MaterialTheme.typography.titleMedium)
                        Text("$activeCount of 5 active", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LocalPolicyTile(Icons.Filled.FolderOpen, "Read files", "Inspect shared dirs", uiState.allowReadFiles, { onTogglePolicy("read") }, Modifier.weight(1f))
                        LocalPolicyTile(Icons.Filled.Edit, "Write files", "Apply local patches", uiState.allowWriteFiles, { onTogglePolicy("write") }, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LocalPolicyTile(Icons.Filled.Terminal, "Run commands", "Ask before shell", uiState.allowRunCommands, { onTogglePolicy("run") }, Modifier.weight(1f))
                        LocalPolicyTile(Icons.Filled.OpenInBrowser, "Open URLs", "Launch local tabs", uiState.allowOpenUrls, { onTogglePolicy("url") }, Modifier.weight(1f))
                    }

                    GituSurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = { onTogglePolicy("chat") }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Agent chat", style = MaterialTheme.typography.labelLarge)
                                    Text("Relay messages from local CLI session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Checkbox(checked = uiState.allowAgentChat, onCheckedChange = { onTogglePolicy("chat") })
                        }
                    }
                }
            }

            // Shared Local Folders Allowlist
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shared local folders", style = MaterialTheme.typography.titleMedium)
                        Surface(
                            color = gituColors.surfaceContainerLow,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showAddFolderDialog = true }
                                .testTag("pc_link_add_folder_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Add folder", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    uiState.sharedFolders.forEach { folder ->
                        GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(folder.path, style = GituMonoSm)
                                        Text(folder.accessSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { onRemoveFolder(folder.id) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove folder", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFolderDialog) {
        AlertDialog(
            onDismissRequest = { showAddFolderDialog = false },
            title = { Text("Share local folder", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = folderPath,
                        onValueChange = { folderPath = it },
                        label = { Text("Folder path") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = folderReadOnly, onCheckedChange = { folderReadOnly = it })
                        Text("Read-only access", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddFolder(folderPath, folderReadOnly)
                    showAddFolderDialog = false
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { showAddFolderDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LocalPolicyTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    GituSurfaceCard(modifier = modifier, onClick = onToggle) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Checkbox(checked = checked, onCheckedChange = { onToggle() })
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ==========================================
// Screen 14: Connections & Integrations
// ==========================================
@Composable
fun ConnectionsScreen(
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    onAddCustomApi: (String, String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var showCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }

    val categories = listOf("All", "OAuth Apps", "Mailboxes", "Gateways")
    val visibleApps = remember(uiState.integrationApps, uiState.integrationSearch, uiState.integrationCategory) {
        uiState.integrationApps.filter { app ->
            val catOk = uiState.integrationCategory == "All" || app.category == uiState.integrationCategory
            val qOk = uiState.integrationSearch.isBlank() ||
                app.name.contains(uiState.integrationSearch, ignoreCase = true) ||
                app.subtitle.contains(uiState.integrationSearch, ignoreCase = true)
            catOk && qOk
        }
    }

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
                GituBackButton(onClick = onBack, testTag = "connections_back_button")
                Text("Connections", style = MaterialTheme.typography.headlineSmall)
            }
            Button(
                onClick = { showCustomDialog = true },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("connections_add_custom_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Custom API", style = MaterialTheme.typography.labelSmall)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search & Category Filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.integrationSearch,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search Gmail, GitHub, Slack, MCP...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("connections_search_input")
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val selected = uiState.integrationCategory == cat
                            Surface(
                                color = if (selected) MaterialTheme.colorScheme.primary else gituColors.surfaceContainerLow,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape,
                                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onSelectCategory(cat) }
                            ) {
                                Text(cat, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
                            }
                        }
                    }
                }
            }

            // OAuth & SaaS Apps 2-Col Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("OAuth & SaaS apps", style = MaterialTheme.typography.titleMedium)
                    visibleApps.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { app ->
                                val icon = when (app.iconKey) {
                                    "mail" -> Icons.Filled.Mail
                                    "code" -> Icons.Filled.Code
                                    "tag" -> Icons.Filled.Tag
                                    "article" -> Icons.Filled.Article
                                    else -> Icons.Filled.Api
                                }
                                GituSurfaceCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (app.isConnected) MaterialTheme.colorScheme.secondaryContainer
                                                        else gituColors.surfaceContainerLow
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    icon,
                                                    contentDescription = app.name,
                                                    tint = if (app.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            if (app.isConnected) {
                                                GituBadgePill("Active", gituColors.successContainer, gituColors.onSuccessContainer)
                                            } else {
                                                Text("OAuth", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(app.name, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = app.subtitle,
                                                style = if (app.isConnected) GituMonoSm else MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(Modifier.weight(1f))

                                        if (app.isConnected) {
                                            Surface(
                                                color = gituColors.surfaceContainerLow,
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(CircleShape)
                                                    .clickable { onToggleApp(app.id) }
                                                    .testTag("conn_manage_${app.id}")
                                            ) {
                                                Text(
                                                    "Manage",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = { onToggleApp(app.id) },
                                                shape = CircleShape,
                                                contentPadding = PaddingValues(vertical = 6.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("conn_connect_${app.id}")
                                            ) {
                                                Text("Connect", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Gateways & MCP
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gateways & MCP", style = MaterialTheme.typography.titleMedium)
                    GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        uiState.gateways.forEachIndexed { idx, gw ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(gw.name, style = MaterialTheme.typography.labelLarge)
                                    Text(gw.subtitle, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (gw.isLiveBadge) {
                                    GituBadgePill(gw.badgeOrAction, gituColors.successContainer, gituColors.onSuccessContainer)
                                } else {
                                    Surface(
                                        color = gituColors.surfaceContainerLow,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { onShowMessage("${gw.name}: ${gw.badgeOrAction} synced") }
                                    ) {
                                        Text(gw.badgeOrAction, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                    }
                                }
                            }
                            if (idx < uiState.gateways.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Add Custom API / MCP", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Integration name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("Endpoint URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddCustomApi(customName, customUrl)
                    customName = ""
                    customUrl = ""
                    showCustomDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// Screen 15: Server Pairing & Auth
// ==========================================
@Composable
fun ServerPairingAuthScreen(
    uiState: AgentGituUiState,
    onSwitchMode: (Boolean) -> Unit,
    onUpdateUrl: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onTestHealth: () -> Unit,
    onSignIn: () -> Unit,
    onOAuthFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var passphrase by remember { mutableStateOf("•••••••••••••••") }
    var showQrDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Brand & Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AGENT GITU MOBILE", style = GituMonoSm, color = MaterialTheme.colorScheme.primary)
                Text("Connect to your server", style = MaterialTheme.typography.displayLarge)
                Text(
                    text = "Pair via desktop QR code or sign in to a local LAN or remote HTTPS host.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // QR Code Quick Pair CTA
        item {
            Button(
                onClick = { showQrDialog = true },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().testTag("pairing_scan_qr_button")
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan desktop QR code", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Server & Auth Card
        item {
            GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // LAN vs Remote Switcher
                    Surface(color = gituColors.surfaceContainerLow, shape = CircleShape) {
                        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            listOf(true to "Local LAN", false to "Remote HTTPS").forEach { (isLan, label) ->
                                val selected = uiState.serverModeLan == isLan
                                Surface(
                                    color = if (selected) gituColors.surfaceContainerLowest else Color.Transparent,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .clickable { onSwitchMode(isLan) }
                                        .testTag("pairing_mode_${if (isLan) "lan" else "https"}")
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Server Address + Healthz Pill
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Server address", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                color = gituColors.successContainer,
                                shape = CircleShape,
                                modifier = Modifier.clip(CircleShape).clickable(onClick = onTestHealth)
                            ) {
                                Text(
                                    text = "/healthz OK",
                                    style = GituMonoSm,
                                    color = gituColors.onSuccessContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = uiState.serverUrl,
                            onValueChange = onUpdateUrl,
                            textStyle = GituMonoSm,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pairing_url_input")
                        )
                    }

                    // Email
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = uiState.authEmail,
                            onValueChange = onUpdateEmail,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pairing_email_input")
                        )
                    }

                    // Passphrase
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Passphrase or access key", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = passphrase,
                            onValueChange = { passphrase = it },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pairing_passphrase_input")
                        )
                    }

                    Button(
                        onClick = onSignIn,
                        shape = CircleShape,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pairing_signin_button")
                    ) {
                        Text("Sign in to Cowork", style = MaterialTheme.typography.labelLarge)
                    }

                    // External OAuth Flows
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Google", "ChatGPT", "PikRouter").forEach { provider ->
                            Surface(
                                color = gituColors.surfaceContainerLow,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onOAuthFeedback("Authenticated via $provider")
                                        onSignIn()
                                    }
                            ) {
                                Text(
                                    text = provider,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Desktop QR Pairing", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(gituColors.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.QrCode2, contentDescription = "Desktop pairing QR", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(88.dp))
                    }
                    Text("Detected ${uiState.localPcName} (${uiState.localPcPairingCode})", style = GituMonoSm)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showQrDialog = false
                    onSignIn()
                }) { Text("Pair & Connect") }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Cancel") }
            }
        )
    }
}
