package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AgentStatus
import com.example.model.ConversationThread
import com.example.model.ThreadCategory
import com.example.ui.components.AgentAvatar
import com.example.ui.components.GituCircleIconButton
import com.example.ui.components.GituSurfaceCard
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors
import com.example.viewmodel.AgentGituUiState

@Composable
fun CoworkHubScreen(
    uiState: AgentGituUiState,
    onSwitchServerClick: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (ThreadCategory) -> Unit,
    onApproveGate: () -> Unit,
    onCreateThread: (String) -> Unit,
    onOpenAgentProfile: (String) -> Unit,
    onOpenRoster: () -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenVoiceCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var showNewThreadDialog by remember { mutableStateOf(false) }
    var newThreadTitle by remember { mutableStateOf("") }

    val filteredThreads = remember(uiState.threads, uiState.selectedThreadCategory, uiState.hubSearchQuery) {
        uiState.threads.filter { thread ->
            val catMatch = uiState.selectedThreadCategory == ThreadCategory.ALL ||
                thread.category == uiState.selectedThreadCategory
            val queryMatch = uiState.hubSearchQuery.isBlank() ||
                thread.title.contains(uiState.hubSearchQuery, ignoreCase = true) ||
                thread.preview.contains(uiState.hubSearchQuery, ignoreCase = true)
            catMatch && queryMatch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sticky-style Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cowork",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onSwitchServerClick)
                                .padding(vertical = 2.dp)
                                .testTag("switch_server_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(gituColors.success)
                            )
                            Text(
                                text = uiState.serverName,
                                style = GituMonoSm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = "Switch connected server",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GituCircleIconButton(
                            icon = Icons.Filled.Search,
                            contentDescription = "Search teammates and threads",
                            onClick = onToggleSearch,
                            testTag = "hub_search_button"
                        )
                        GituCircleIconButton(
                            icon = Icons.Filled.EditNote,
                            contentDescription = "Start new Cowork thread",
                            onClick = { showNewThreadDialog = true },
                            isPrimary = true,
                            testTag = "hub_new_thread_button"
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.isHubSearchVisible) {
                    OutlinedTextField(
                        value = uiState.hubSearchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search threads or teammates...", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.hubSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .testTag("hub_search_input")
                    )
                }
            }
        }

        // Teammate Presence Strip
        item {
            val presenceTeammates = uiState.teammates.take(3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                presenceTeammates.forEach { teammate ->
                    val ringColor = when (teammate.status) {
                        AgentStatus.NEEDS_APPROVAL -> MaterialTheme.colorScheme.tertiary
                        AgentStatus.ONLINE, AgentStatus.RUNNING ->
                            if (teammate.id == "chief") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        AgentStatus.SLEEPING -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenAgentProfile(teammate.id) }
                            .padding(vertical = 4.dp)
                            .testTag("presence_${teammate.id}")
                    ) {
                        AgentAvatar(
                            imageUrl = teammate.avatarUrl,
                            fallbackInitials = teammate.shortName,
                            contentDescription = teammate.name,
                            size = 48.dp,
                            ringColor = ringColor,
                            status = teammate.status
                        )
                        Text(
                            text = teammate.shortName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Hire / Roster Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenRoster)
                        .padding(vertical = 4.dp)
                        .testTag("presence_hire_button")
                ) {
                    val outlineColor = MaterialTheme.colorScheme.outline
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(gituColors.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = outlineColor,
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "View AI teammates roster and hire",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Hire",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Compact Inline Escalation Strip
        if (!uiState.pendingApproval.isApproved) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenThread("group_chat/core_product") }
                        .testTag("escalation_strip_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = "Approval gate required",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = uiState.pendingApproval.actionTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = uiState.pendingApproval.detailSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = onApproveGate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 36.dp)
                                .testTag("escalation_approve_button")
                        ) {
                            Text("Approve", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Filter Tabs
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val tabs = listOf(
                        ThreadCategory.ALL to "All",
                        ThreadCategory.TEAM to "Teams",
                        ThreadCategory.DM to "DMs",
                        ThreadCategory.INBOX to "Inbox"
                    )
                    tabs.forEach { (category, label) ->
                        val selected = uiState.selectedThreadCategory == category
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onSelectCategory(category) }
                                .testTag("filter_tab_${label.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            }
        }

        // Thread Cards
        items(filteredThreads, key = { it.id }) { thread ->
            CoworkThreadCard(
                thread = thread,
                onClick = { onOpenThread(thread.targetRoute) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Quick LiveKit Voice Bar
        item {
            Surface(
                color = gituColors.surfaceContainerLow,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenVoiceCall)
                    .testTag("quick_voice_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Voice call Chief",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Voice check-in with Chief",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ask, steer, or queue without stopping runs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showNewThreadDialog) {
        AlertDialog(
            onDismissRequest = { showNewThreadDialog = false },
            title = { Text("Start Cowork thread", style = MaterialTheme.typography.headlineSmall) },
            text = {
                OutlinedTextField(
                    value = newThreadTitle,
                    onValueChange = { newThreadTitle = it },
                    label = { Text("Thread topic or team name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateThread(newThreadTitle)
                        newThreadTitle = ""
                        showNewThreadDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewThreadDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CoworkThreadCard(
    thread: ConversationThread,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    GituSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("thread_card_${thread.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (thread.avatarUrl != null) {
                AgentAvatar(
                    imageUrl = thread.avatarUrl,
                    fallbackInitials = thread.title,
                    contentDescription = thread.title,
                    size = 44.dp
                )
            } else {
                val isVoice = thread.iconType == "voice"
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVoice) MaterialTheme.colorScheme.secondaryContainer
                            else gituColors.surfaceContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVoice) Icons.Filled.GraphicEq else Icons.Filled.Group,
                        contentDescription = thread.title,
                        tint = if (isVoice) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = thread.timestamp,
                        style = GituMonoSm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (thread.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thread.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
