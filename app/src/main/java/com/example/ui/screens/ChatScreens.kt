package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors
import com.example.viewmodel.AgentGituUiState

// ==========================================
// Screen 5: Cowork Group Chat
// ==========================================
@Composable
fun CoworkGroupChatScreen(
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onSelectTopic: (String) -> Unit,
    onAddTopic: (String) -> Unit,
    onApprovePush: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var composerText by remember { mutableStateOf("") }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var newTopicInput by remember { mutableStateOf("") }
    var showDiffSheet by remember { mutableStateOf(false) }
    var showArtifactSheet by remember { mutableStateOf<String?>(null) }
    var showMissionsSheet by remember { mutableStateOf(false) }

    val topicMessages = remember(uiState.groupMessages, uiState.activeGroupTopic) {
        uiState.groupMessages.filter { it.topic == uiState.activeGroupTopic }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        // Contextual Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GituBackButton(onClick = onBack, testTag = "group_chat_back_button")

                // Overlapping Avatars
                Box(modifier = Modifier.width(52.dp).height(32.dp)) {
                    AgentAvatar(
                        imageUrl = GituImageUrls.KAI_AVATAR,
                        fallbackInitials = "KA",
                        contentDescription = "Kai",
                        size = 32.dp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    AgentAvatar(
                        imageUrl = GituImageUrls.MAYA_AVATAR,
                        fallbackInitials = "MA",
                        contentDescription = "Maya",
                        size = 32.dp,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Core Product Team",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(gituColors.success)
                        )
                        Text("2 active • Cloud", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GituCircleIconButton(
                    icon = Icons.Filled.AccountTree,
                    contentDescription = "View delegated missions",
                    onClick = { showMissionsSheet = true },
                    testTag = "group_missions_button"
                )
                GituCircleIconButton(
                    icon = Icons.Filled.Call,
                    contentDescription = "Start LiveKit voice call",
                    onClick = onStartVoiceCall,
                    isPrimary = true,
                    testTag = "group_call_button"
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Topic Threads Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gituColors.surfaceContainerLow)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.groupTopics.forEach { topic ->
                val active = uiState.activeGroupTopic == topic
                Surface(
                    color = gituColors.surfaceContainerLowest,
                    contentColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSelectTopic(topic) }
                        .testTag("topic_chip_${topic.removePrefix("#")}")
                ) {
                    Text(
                        text = topic,
                        style = GituMonoSm,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
            Surface(
                color = gituColors.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { showAddTopicDialog = true }
                    .testTag("add_topic_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = "Add topic thread", modifier = Modifier.size(16.dp))
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Conversation Stream
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = gituColors.surfaceContainerLow,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Today • 14:01",
                            style = GituMonoSm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(topicMessages, key = { it.id }) { msg ->
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
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(msg.timestamp, style = GituMonoSm, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                                    Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                } else {
                    var expandedReasoning by remember(msg.id) { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AgentAvatar(
                            imageUrl = msg.avatarUrl,
                            fallbackInitials = msg.senderName,
                            contentDescription = msg.senderName,
                            size = 36.dp
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(msg.senderName, style = MaterialTheme.typography.labelLarge)
                                if (msg.senderRole.isNotEmpty()) {
                                    val isResearcher = msg.senderRole == "Researcher"
                                    GituBadgePill(
                                        text = msg.senderRole,
                                        containerColor = if (isResearcher) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = if (isResearcher) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text(msg.timestamp, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (msg.reasoningPill != null) {
                                Surface(
                                    color = gituColors.surfaceContainerLow,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { expandedReasoning = !expandedReasoning }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Text(msg.reasoningPill, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                            GituSurfaceCard(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(msg.body, style = MaterialTheme.typography.bodyMedium)

                                    if (msg.hasApprovalGate) {
                                        Surface(
                                            color = gituColors.surfaceContainerLow,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(
                                                            Icons.Filled.VerifiedUser,
                                                            contentDescription = null,
                                                            tint = if (uiState.pendingApproval.isApproved) gituColors.success else MaterialTheme.colorScheme.tertiary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text(
                                                            text = if (uiState.pendingApproval.isApproved) "Approved • git push" else "Approval gate • git push",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (uiState.pendingApproval.isApproved) gituColors.success else MaterialTheme.colorScheme.tertiary
                                                        )
                                                    }
                                                    Text(uiState.pendingApproval.diffStats, style = GituMonoSm, color = gituColors.success)
                                                }

                                                Surface(
                                                    color = gituColors.surfaceContainerLowest,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Branch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text(uiState.pendingApproval.branch, style = GituMonoSm)
                                                        }
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Commits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text(uiState.pendingApproval.commitsLabel, style = GituMonoSm)
                                                        }
                                                    }
                                                }

                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = onApprovePush,
                                                        enabled = !uiState.pendingApproval.isApproved,
                                                        shape = CircleShape,
                                                        modifier = Modifier.weight(1f).testTag("group_approve_push_button")
                                                    ) {
                                                        Text(
                                                            text = if (uiState.pendingApproval.isApproved) "Pushed" else "Approve push",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                    OutlinedButton(
                                                        onClick = { showDiffSheet = true },
                                                        shape = CircleShape,
                                                        modifier = Modifier.weight(1f).testTag("group_inspect_diff_button")
                                                    ) {
                                                        Text("Inspect diff", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (msg.artifactTitle != null) {
                                        Surface(
                                            color = gituColors.surfaceContainerLow,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { showArtifactSheet = msg.artifactTitle }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                                                    }
                                                    Column {
                                                        Text(msg.artifactTitle, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text(msg.artifactMeta ?: "", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                                Surface(
                                                    color = gituColors.surfaceContainerLowest,
                                                    shape = CircleShape,
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Text(
                                                        text = "Preview",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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

            // Live Chief Routing Status
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgentAvatar(
                        imageUrl = GituImageUrls.CHIEF_AVATAR,
                        fallbackInitials = "CH",
                        contentDescription = "Chief of Staff",
                        size = 28.dp
                    )
                    Surface(
                        color = gituColors.surfaceContainerLow,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = if (uiState.pendingApproval.isApproved) "Chief verified push on feat/mobile-fcm."
                            else "Chief is watching for your push decision...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Docked Composer
        Surface(
            color = gituColors.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("@Kai", "@Maya", "@Chief").forEach { mention ->
                        Surface(
                            color = gituColors.surfaceContainerLow,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { composerText = "$composerText$mention ".trimStart() }
                        ) {
                            Text(
                                text = mention,
                                style = GituMonoSm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onStartVoiceCall)
                            .testTag("group_livekit_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("LiveKit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                ComposerInputBar(
                    value = composerText,
                    onValueChange = { composerText = it },
                    placeholder = "Steer or message teammates...",
                    showAttachAndMic = true,
                    onMicClick = onStartVoiceCall,
                    onSend = {
                        onSendMessage(composerText)
                        composerText = ""
                    },
                    testTagPrefix = "group_composer"
                )
            }
        }
    }

    if (showAddTopicDialog) {
        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("New topic thread", style = MaterialTheme.typography.headlineSmall) },
            text = {
                OutlinedTextField(
                    value = newTopicInput,
                    onValueChange = { newTopicInput = it },
                    label = { Text("#topic-name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onAddTopic(newTopicInput)
                    newTopicInput = ""
                    showAddTopicDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDiffSheet) {
        AlertDialog(
            onDismissRequest = { showDiffSheet = false },
            title = { Text("Git diff • feat/mobile-fcm", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("+48  FcmTokenManager.kt (foreground rotation)", style = GituMonoSm, color = gituColors.success)
                    Text("+64  CoworkDeepLinkRouter.kt (thread routes)", style = GituMonoSm, color = gituColors.success)
                    Text("+30 / -28  FcmRotationTest.kt (18/18 passed)", style = GituMonoSm)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onApprovePush()
                    showDiffSheet = false
                }) { Text("Approve push") }
            },
            dismissButton = {
                TextButton(onClick = { showDiffSheet = false }) { Text("Close") }
            }
        )
    }

    if (showArtifactSheet != null) {
        AlertDialog(
            onDismissRequest = { showArtifactSheet = null },
            title = { Text(showArtifactSheet ?: "", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Android FCM Payload Contract v2.1\n\n• Topic routing: agentgitu://cowork/:threadId\n• Bounded cursor polling fallback: 3,000ms cap\n• Redacted workspace secrets for Shared Inbox rooms.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(onClick = { showArtifactSheet = null }) { Text("Done") }
            }
        )
    }

    if (showMissionsSheet) {
        AlertDialog(
            onDismissRequest = { showMissionsSheet = false },
            title = { Text("Delegated missions", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Kai: FCM rotation + 18 unit tests (awaiting push gate)", style = MaterialTheme.typography.bodySmall)
                    Text("• Maya: Android FCM payload contract (delivered)", style = MaterialTheme.typography.bodySmall)
                    Text("• Chief: Routing & standing rule enforcement", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showMissionsSheet = false }) { Text("Close") }
            }
        )
    }
}

// ==========================================
// Screen 6: Private Agent Chat (1-on-1)
// ==========================================
@Composable
fun PrivateAgentChatScreen(
    teammate: Teammate,
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onViewProfile: () -> Unit,
    onVoiceCall: () -> Unit,
    onOpenDesktop: () -> Unit,
    onSendMessage: (String) -> Unit,
    onDraftReplyFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var composerText by remember { mutableStateOf("") }
    val messages = remember(uiState.privateMessages, teammate.id) {
        uiState.privateMessages.filter { it.agentId == teammate.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GituBackButton(onClick = onBack, testTag = "dm_back_button")
                AgentAvatar(
                    imageUrl = teammate.avatarUrl,
                    fallbackInitials = teammate.shortName,
                    contentDescription = teammate.name,
                    size = 40.dp,
                    status = teammate.status
                )
                Column {
                    Text(teammate.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${teammate.roleTitle} • ${teammate.computeTarget.label}",
                        style = GituMonoSm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GituCircleIconButton(
                    icon = Icons.Filled.Person,
                    contentDescription = "View ${teammate.name} profile",
                    onClick = onViewProfile,
                    testTag = "dm_profile_button"
                )
                GituCircleIconButton(
                    icon = Icons.Filled.Call,
                    contentDescription = "Voice call ${teammate.name}",
                    onClick = onVoiceCall,
                    isPrimary = true,
                    testTag = "dm_call_button"
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Private Scope Pill
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = gituColors.surfaceContainerLow,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Private DM • ${teammate.connectedApps.joinToString(" & ").ifEmpty { "Workspace" }}",
                                style = GituMonoSm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
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
                } else if (msg.hasHandoffCard) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AgentAvatar(
                            imageUrl = teammate.avatarUrl,
                            fallbackInitials = teammate.shortName,
                            contentDescription = teammate.name,
                            size = 36.dp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Browser sign-in handoff", style = MaterialTheme.typography.labelSmall)
                                    Text("Figma", style = GituMonoSm)
                                }
                                Text(msg.body, style = MaterialTheme.typography.bodySmall)
                                Button(
                                    onClick = onOpenDesktop,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("dm_open_desktop_button")
                                ) {
                                    Text("Open desktop", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AgentAvatar(
                            imageUrl = teammate.avatarUrl,
                            fallbackInitials = teammate.shortName,
                            contentDescription = teammate.name,
                            size = 36.dp
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(teammate.name, style = MaterialTheme.typography.labelLarge)
                                Text(msg.timestamp, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            GituSurfaceCard(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(msg.body, style = MaterialTheme.typography.bodyMedium)

                                    if (msg.hasDigestWidget) {
                                        Surface(
                                            color = gituColors.surfaceContainerLow,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Partner Digest Widget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                    Text("Gmail • Live", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Surface(
                                                    color = gituColors.surfaceContainerLowest,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Acme Mobile", style = MaterialTheme.typography.labelSmall)
                                                            Text("Ready • Thu", style = GituMonoSm, color = gituColors.success)
                                                        }
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Northstar", style = MaterialTheme.typography.labelSmall)
                                                            Text("Needs key", style = GituMonoSm, color = gituColors.warning)
                                                        }
                                                    }
                                                }
                                                Button(
                                                    onClick = onDraftReplyFeedback,
                                                    shape = CircleShape,
                                                    modifier = Modifier.fillMaxWidth().testTag("dm_draft_gmail_button")
                                                ) {
                                                    Text("Draft reply in Gmail", style = MaterialTheme.typography.labelSmall)
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

        // Docked Private Composer
        Surface(color = gituColors.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ComposerInputBar(
                    value = composerText,
                    onValueChange = { composerText = it },
                    placeholder = "Message ${teammate.shortName} privately...",
                    showAttachAndMic = true,
                    onMicClick = onVoiceCall,
                    onSend = {
                        onSendMessage(composerText)
                        composerText = ""
                    },
                    testTagPrefix = "dm_composer"
                )
            }
        }
    }
}

// ==========================================
// Screen 7: Shared Inbox Room & Invites
// ==========================================
@Composable
fun SharedInboxRoomScreen(
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onToggleInviteRevoked: () -> Unit,
    onSendMessage: (String) -> Unit,
    onCopyFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    val clipboard = LocalClipboardManager.current
    var composerText by remember { mutableStateOf("") }
    var showInviteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
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
                GituBackButton(onClick = onBack, testTag = "inbox_back_button")
                Column {
                    Text("Design Partner Room", style = MaterialTheme.typography.titleMedium)
                    Text("2 external • @Maya", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            GituCircleIconButton(
                icon = Icons.Filled.PersonAdd,
                contentDescription = "Invite external collaborator",
                onClick = { showInviteDialog = true },
                isPrimary = true,
                testTag = "inbox_invite_button"
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Room Policy 2-Column Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Agent trigger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("@Maya • mention", style = GituMonoSm)
                        }
                    }
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tool policy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("read_only", style = GituMonoSm, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Invite Link Card
            item {
                GituSurfaceCard(
                    containerColor = gituColors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(uiState.sharedInboxInviteCode, style = GituMonoSm)
                            Text(
                                text = if (uiState.sharedInboxInviteRevoked) "Link revoked • Tap Renew" else "Expires in 23h • 1 seat left",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = gituColors.surfaceContainerLowest,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        clipboard.setText(AnnotatedString(uiState.sharedInboxInviteCode))
                                        onCopyFeedback("Copied ${uiState.sharedInboxInviteCode}")
                                    }
                                    .testTag("inbox_copy_invite_button")
                            ) {
                                Text("Copy", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                            Surface(
                                color = if (uiState.sharedInboxInviteRevoked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                                contentColor = if (uiState.sharedInboxInviteRevoked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                shape = CircleShape,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onToggleInviteRevoked)
                                    .testTag("inbox_revoke_invite_button")
                            ) {
                                Text(
                                    text = if (uiState.sharedInboxInviteRevoked) "Renew" else "Revoke",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Room Messages
            items(uiState.sharedInboxMessages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (msg.avatarUrl != null) {
                        AgentAvatar(
                            imageUrl = msg.avatarUrl,
                            fallbackInitials = msg.senderName,
                            contentDescription = msg.senderName,
                            size = 36.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(gituColors.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(msg.initials ?: "GU", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(msg.senderName, style = MaterialTheme.typography.labelLarge)
                            if (msg.badge != null) {
                                if (msg.badge == "Guest") {
                                    Surface(
                                        color = gituColors.surfaceContainerLow,
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(msg.badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp))
                                    }
                                } else {
                                    GituBadgePill(
                                        text = msg.badge,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Text(msg.timestamp, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        GituSurfaceCard(
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(msg.body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }
        }

        // Docked Room Composer
        Surface(color = gituColors.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ComposerInputBar(
                    value = composerText,
                    onValueChange = { composerText = it },
                    placeholder = "Message room or @Maya...",
                    showAttachAndMic = false,
                    onMicClick = {},
                    onSend = {
                        onSendMessage(composerText)
                        composerText = ""
                    },
                    testTagPrefix = "inbox_composer"
                )
            }
        }
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite collaborator", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Share invite link ${uiState.sharedInboxInviteCode}. External guests can mention @Maya with read_only tool permissions.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(onClick = {
                    clipboard.setText(AnnotatedString(uiState.sharedInboxInviteCode))
                    onCopyFeedback("Invite link copied")
                    showInviteDialog = false
                }) { Text("Copy link") }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun ComposerInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showAttachAndMic: Boolean,
    onMicClick: () -> Unit,
    onSend: () -> Unit,
    testTagPrefix: String
) {
    val gituColors = LocalGituColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showAttachAndMic) {
            GituCircleIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Attach files or images",
                onClick = {},
                testTag = "${testTagPrefix}_attach"
            )
        }

        Surface(
            color = gituColors.surfaceContainerLow,
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("${testTagPrefix}_input")
                    )
                }
                if (showAttachAndMic) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice message",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onMicClick)
                    )
                }
            }
        }

        GituCircleIconButton(
            icon = Icons.Filled.ArrowUpward,
            contentDescription = "Send message",
            onClick = onSend,
            isPrimary = true,
            testTag = "${testTagPrefix}_send"
        )
    }
}
