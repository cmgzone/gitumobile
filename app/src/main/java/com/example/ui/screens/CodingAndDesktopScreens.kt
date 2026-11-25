package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.model.GituImageUrls
import com.example.ui.components.GituCircleIconButton
import com.example.ui.components.GituSurfaceCard
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors
import com.example.viewmodel.AgentGituUiState

// ==========================================
// Screen 8: Coding Mode — Runs
// ==========================================
@Composable
fun CodingModeScreen(
    uiState: AgentGituUiState,
    onSetCodingMode: (Boolean) -> Unit,
    onSelectSession: (String) -> Unit,
    onCreateSession: () -> Unit,
    onToggleStopRun: () -> Unit,
    onSendPrompt: (String) -> Unit,
    onOpenDesktop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var showSidebar by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }
    var showCheckpointsSheet by remember { mutableStateOf(false) }
    var workspaceToolDialog by remember { mutableStateOf<String?>(null) }

    if (showSidebar) {
        BackHandler { showSidebar = false }
    }

    val activeSession = uiState.codingSessions.find { it.id == uiState.activeCodingSessionId }
        ?: uiState.codingSessions.first()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            // Top Header with Sidebar Trigger & Agent/Chat Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GituCircleIconButton(
                        icon = Icons.Filled.Menu,
                        contentDescription = "Open sessions and workspace sidebar",
                        onClick = { showSidebar = true },
                        testTag = "coding_sidebar_button"
                    )
                    Column {
                        Text(activeSession.repo, style = MaterialTheme.typography.titleMedium)
                        Text(activeSession.branch, style = GituMonoSm, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Agent / Chat Mode Pill
                Surface(color = gituColors.surfaceContainerLow, shape = CircleShape) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        listOf(true to "Agent", false to "Chat").forEach { (isAgent, label) ->
                            val active = uiState.codingModeIsAgent == isAgent
                            Surface(
                                color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onSetCodingMode(isAgent) }
                                    .testTag("coding_mode_${label.lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Live Run State Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gituColors.surfaceContainerLow)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = gituColors.surfaceContainerLowest,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isCodingRunActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        )
                        Text(
                            text = if (uiState.isCodingRunActive) "3/4 plan" else "Paused",
                            style = GituMonoSm,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                RunMetricChip("+142 / -28", gituColors.success)
                RunMetricChip("PASS 18/18", MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = gituColors.surfaceContainerLowest,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onOpenDesktop)
                ) {
                    Text("Browser", style = GituMonoSm, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Stream
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.codingMessages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp),
                                modifier = Modifier.widthIn(max = 310.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(msg.body, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        msg.timestamp,
                                        style = GituMonoSm,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    } else {
                        var expandedReasoning by remember(msg.id) { mutableStateOf(false) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Code, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(14.dp))
                                    }
                                    Text("Coding Agent", style = MaterialTheme.typography.labelLarge)
                                    Text(msg.modelTag, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(msg.costTag, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (msg.reasoningSummary != null) {
                                Surface(
                                    color = gituColors.surfaceContainerLow,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { expandedReasoning = !expandedReasoning }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Text(msg.reasoningSummary, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Icon(
                                            if (expandedReasoning) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                AnimatedVisibility(visible = expandedReasoning && msg.reasoningDetails != null) {
                                    Surface(
                                        color = gituColors.surfaceContainerLow,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(
                                            text = msg.reasoningDetails ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }

                            GituSurfaceCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(msg.body, style = MaterialTheme.typography.bodyMedium)

                                    if (msg.hasLiveBrowser) {
                                        Surface(
                                            color = gituColors.surfaceContainerLow,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(gituColors.success))
                                                        Text(msg.browserUrl, style = GituMonoSm)
                                                    }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                                                            Icon(Icons.Filled.Refresh, contentDescription = "Reload live browser", modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(onClick = onOpenDesktop, modifier = Modifier.size(28.dp)) {
                                                            Icon(Icons.Filled.OpenInFull, contentDescription = "Expand live browser", modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                                XfceDesktopPreviewImage(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(16f / 9f)
                                                )

                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(gituColors.surfaceContainerLowest)
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(msg.browserStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Button(
                                                        onClick = onOpenDesktop,
                                                        shape = CircleShape,
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        modifier = Modifier.defaultMinSize(minHeight = 32.dp).testTag("coding_take_control_button")
                                                    ) {
                                                        Text("Take control", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (msg.evidenceCommand != null) {
                                        Surface(
                                            color = gituColors.surfaceContainerLow,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Evidence log", style = MaterialTheme.typography.labelSmall)
                                                    Surface(color = gituColors.successContainer, shape = CircleShape) {
                                                        Text(
                                                            text = "PASS",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = gituColors.onSuccessContainer,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(msg.evidenceCommand, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedButton(
                                                        onClick = { showCheckpointsSheet = true },
                                                        shape = CircleShape,
                                                        modifier = Modifier.weight(1f).testTag("coding_checkpoints_button")
                                                    ) {
                                                        Text("Checkpoints (3)", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    Button(
                                                        onClick = onToggleStopRun,
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (uiState.isCodingRunActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                                            contentColor = if (uiState.isCodingRunActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                                        ),
                                                        shape = CircleShape,
                                                        modifier = Modifier.weight(1f).testTag("coding_stop_run_button")
                                                    ) {
                                                        Text(
                                                            text = if (uiState.isCodingRunActive) "Stop run" else "Resume run",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Docked Coding Composer
            Surface(color = gituColors.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Claude 3.7 • Effort: High", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Steer run",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { promptText = "Focus on bounded cursor polling" }
                        )
                    }
                    ComposerInputBar(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = "Steer run or start a task...",
                        showAttachAndMic = true,
                        onMicClick = {},
                        onSend = {
                            onSendPrompt(promptText)
                            promptText = ""
                        },
                        testTagPrefix = "coding_composer"
                    )
                }
            }
        }

        // Slide-Over Sidebar Drawer
        AnimatedVisibility(
            visible = showSidebar,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                    .clickable { showSidebar = false }
            )
        }
        AnimatedVisibility(
            visible = showSidebar,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            Surface(
                color = gituColors.surfaceContainerLowest,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 310.dp)
                    .fillMaxWidth(0.82f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Coding workspace", style = MaterialTheme.typography.headlineSmall)
                        GituCircleIconButton(
                            icon = Icons.Filled.Close,
                            contentDescription = "Close sidebar",
                            onClick = { showSidebar = false },
                            size = 36.dp,
                            testTag = "coding_close_sidebar_button"
                        )
                    }

                    // Workspace Features 2x2 Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SidebarToolTile(Icons.Filled.FolderOpen, "Files", { workspaceToolDialog = "Files"; showSidebar = false }, Modifier.weight(1f))
                            SidebarToolTile(Icons.Filled.Commit, "Git diff", { workspaceToolDialog = "Git diff"; showSidebar = false }, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SidebarToolTile(Icons.Filled.Language, "Browser", { showSidebar = false; onOpenDesktop() }, Modifier.weight(1f))
                            SidebarToolTile(Icons.Filled.Schedule, "Cron", { workspaceToolDialog = "Cron"; showSidebar = false }, Modifier.weight(1f))
                        }
                    }

                    // Sessions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "+ New",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                onCreateSession()
                                showSidebar = false
                            }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.codingSessions.forEach { session ->
                            val selected = session.id == uiState.activeCodingSessionId
                            Surface(
                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else gituColors.surfaceContainerLow,
                                contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectSession(session.id)
                                        showSidebar = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(session.title, style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        session.statusText,
                                        style = GituMonoSm,
                                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckpointsSheet) {
        AlertDialog(
            onDismissRequest = { showCheckpointsSheet = false },
            title = { Text("Run checkpoints (3)", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. Scanned FCM token service (clean)", style = GituMonoSm)
                    Text("2. Wired Cowork thread deep links (+64)", style = GituMonoSm)
                    Text("3. Verified 18/18 mobile-push tests (PASS)", style = GituMonoSm, color = gituColors.success)
                }
            },
            confirmButton = { Button(onClick = { showCheckpointsSheet = false }) { Text("Done") } }
        )
    }

    if (workspaceToolDialog != null) {
        AlertDialog(
            onDismissRequest = { workspaceToolDialog = null },
            title = { Text(workspaceToolDialog ?: "", style = MaterialTheme.typography.headlineSmall) },
            text = {
                val summary = when (workspaceToolDialog) {
                    "Files" -> "src/FcmTokenManager.kt\nsrc/CoworkRouter.kt\ntest/FcmRotationTest.kt\nworkspace-app.tsx"
                    "Git diff" -> "3 files changed, 142 insertions(+), 28 deletions(-) on feat/mobile-fcm"
                    else -> "Active Cron Jobs (3):\n• Every 15m: Healthz & FCM token check\n• Nightly 02:00: Playwright OAuth smoke\n• Daily 09:00: Partner digest sync"
                }
                Text(summary, style = GituMonoSm)
            },
            confirmButton = { Button(onClick = { workspaceToolDialog = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun RunMetricChip(text: String, color: Color) {
    Surface(
        color = LocalGituColors.current.surfaceContainerLowest,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = text,
            style = GituMonoSm,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SidebarToolTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LocalGituColors.current.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ==========================================
// Screen 9: Live Remote Desktop
// ==========================================
@Composable
fun LiveRemoteDesktopScreen(
    uiState: AgentGituUiState,
    onSelectTarget: (String) -> Unit,
    onToggleControl: () -> Unit,
    onToggleSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var showFullscreenDialog by remember { mutableStateOf(false) }
    var activeToolModal by remember { mutableStateOf<String?>(null) }

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
            Column {
                Text("Desktop", style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = if (uiState.desktopSleeping) "XFCE • Suspended" else "XFCE • Running (noVNC)",
                    style = GituMonoSm,
                    color = if (uiState.desktopSleeping) gituColors.warning else gituColors.success
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GituCircleIconButton(
                    icon = Icons.Filled.Bedtime,
                    contentDescription = "Sleep desktop",
                    onClick = onToggleSleep,
                    testTag = "desktop_sleep_button"
                )
                GituCircleIconButton(
                    icon = Icons.Filled.Fullscreen,
                    contentDescription = "Fullscreen desktop",
                    onClick = { showFullscreenDialog = true },
                    testTag = "desktop_fullscreen_button"
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compute Target Switcher
            item {
                Surface(color = gituColors.surfaceContainerLow, shape = CircleShape) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        listOf("My PC", "Private", "Cloud").forEach { target ->
                            val selected = uiState.desktopTarget == target
                            Surface(
                                color = if (selected) gituColors.surfaceContainerLowest else Color.Transparent,
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable { onSelectTarget(target) }
                                    .testTag("desktop_target_${target.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = target,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Live Desktop Stream Viewport
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    XfceDesktopPreviewImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.desktopHumanControl) "You control ${uiState.desktopTarget} desktop"
                                else "Kai is browsing ${uiState.desktopBrowserUrl}",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (uiState.desktopHumanControl) "Agent shell paused while interacting"
                                else "Tap to interact or take control",
                                style = GituMonoSm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onToggleControl,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("desktop_take_control_button")
                        ) {
                            Text(
                                text = if (uiState.desktopHumanControl) "Release" else "Take control",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Quick Remote Tools 3-Col Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteToolTile(Icons.Filled.Folder, "Files", { activeToolModal = "Files" }, Modifier.weight(1f).fillMaxHeight())
                    RemoteToolTile(Icons.Filled.Terminal, "Terminal", { activeToolModal = "Terminal" }, Modifier.weight(1f).fillMaxHeight())
                    RemoteToolTile(Icons.Filled.Public, "Browser", { activeToolModal = "Browser" }, Modifier.weight(1f).fillMaxHeight())
                }
            }

            // Handoff Status Card
            item {
                GituSurfaceCard(
                    containerColor = gituColors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.desktopHumanControl) Icons.Filled.PanTool else Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = if (uiState.desktopHumanControl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (uiState.desktopHumanControl) "Human control active" else "Agent input active",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = if (uiState.desktopHumanControl) "Tap Release to hand session back to Kai" else "Taking control pauses agent shell",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFullscreenDialog) {
        AlertDialog(
            onDismissRequest = { showFullscreenDialog = false },
            title = { Text("noVNC Fullscreen (${uiState.desktopTarget})", style = MaterialTheme.typography.headlineSmall) },
            text = {
                XfceDesktopPreviewImage(
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp))
                )
            },
            confirmButton = { Button(onClick = { showFullscreenDialog = false }) { Text("Exit") } }
        )
    }

    if (activeToolModal != null) {
        AlertDialog(
            onDismissRequest = { activeToolModal = null },
            title = { Text("Remote ${activeToolModal}", style = MaterialTheme.typography.headlineSmall) },
            text = {
                val msg = when (activeToolModal) {
                    "Files" -> "~/Projects/agent-gitu\n~/Downloads/android-fcm-spec.pdf\n/tmp/novnc-session.log"
                    "Terminal" -> "$ ps aux | grep chromium\nkai  1402  chromium --remote-debugging-port=9222\n$ npm test mobile-push -> PASS (18/18)"
                    else -> "Active Tab: http://${uiState.desktopBrowserUrl}\nStatus: SSO Redirect Verified"
                }
                Text(msg, style = GituMonoSm)
            },
            confirmButton = { Button(onClick = { activeToolModal = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun RemoteToolTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GituSurfaceCard(
        modifier = modifier.testTag("desktop_tool_${label.lowercase()}"),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun XfceDesktopPreviewImage(modifier: Modifier = Modifier) {
    val gituColors = LocalGituColors.current
    SubcomposeAsyncImage(
        model = GituImageUrls.XFCE_DESKTOP_PREVIEW,
        contentDescription = "Live XFCE Chromium desktop stream",
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize().background(gituColors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF1E2433)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.DesktopWindows, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Text("XFCE Desktop • localhost:3000/login", style = GituMonoSm, color = Color.White)
                }
            }
        }
    )
}
