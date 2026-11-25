package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AgentStatus
import com.example.model.ComputeTarget
import com.example.model.GituImageUrls
import com.example.model.Teammate
import com.example.ui.components.AgentAvatar
import com.example.ui.components.GituBackButton
import com.example.ui.components.GituBadgePill
import com.example.ui.components.GituCircleIconButton
import com.example.ui.components.GituSurfaceCard
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors
import com.example.viewmodel.AgentGituUiState

// ==========================================
// Screen 2: AI Teammates Roster
// ==========================================
@Composable
fun TeammatesRosterScreen(
    uiState: AgentGituUiState,
    onBack: () -> Unit,
    onNewTeammate: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onEditTeammate: (String) -> Unit,
    onCallTeammate: (String) -> Unit,
    onMessageTeammate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    var selectedRole by remember { mutableStateOf("All") }
    val roles = listOf("All", "Coordinator", "Engineer", "Researcher")

    val filtered = remember(uiState.teammates, selectedRole) {
        if (selectedRole == "All") uiState.teammates
        else uiState.teammates.filter { it.roleFilter.equals(selectedRole, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Contextual Header
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
                GituBackButton(onClick = onBack, testTag = "roster_back_button")
                Column {
                    Text("AI Teammates", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "${uiState.teammates.size} active profiles",
                        style = GituMonoSm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onNewTeammate,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("roster_new_button")
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("New", style = MaterialTheme.typography.labelSmall)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role Filter Pills
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { role ->
                        val active = selectedRole == role
                        Surface(
                            color = if (active) MaterialTheme.colorScheme.primary else gituColors.surfaceContainerLow,
                            contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { selectedRole = role }
                                .testTag("roster_filter_${role.lowercase()}")
                        ) {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // 2-Column Teammate Grid
            val chunked = filtered.chunked(2)
            items(chunked.size) { rowIndex ->
                val pair = chunked[rowIndex]
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { teammate ->
                        TeammateRosterCard(
                            teammate = teammate,
                            onCardClick = { onOpenProfile(teammate.id) },
                            onActionClick = {
                                if (teammate.id == "chief") onCallTeammate(teammate.name)
                                else onMessageTeammate(teammate.id)
                            },
                            onEditClick = { onEditTeammate(teammate.id) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TeammateRosterCard(
    teammate: Teammate,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    GituSurfaceCard(
        modifier = modifier.testTag("roster_card_${teammate.id}"),
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AgentAvatar(
                    imageUrl = teammate.avatarUrl,
                    fallbackInitials = teammate.shortName,
                    contentDescription = teammate.name,
                    size = 48.dp,
                    status = teammate.status
                )
                GituBadgePill(
                    text = teammate.avatarTag,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = teammate.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = teammate.roleTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = gituColors.surfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = teammate.modelShort,
                        style = GituMonoSm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                    )
                }
                Surface(
                    color = gituColors.surfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (teammate.computeTarget == ComputeTarget.MY_PC) "Local" else teammate.computeTarget.label,
                        style = GituMonoSm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GituCircleIconButton(
                    icon = if (teammate.id == "chief") Icons.Filled.Call else Icons.Filled.ChatBubbleOutline,
                    contentDescription = if (teammate.id == "chief") "Voice call ${teammate.name}" else "Message ${teammate.name}",
                    onClick = onActionClick,
                    size = 36.dp,
                    testTag = "roster_action_${teammate.id}"
                )
                Surface(
                    color = gituColors.surfaceContainerLow,
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onEditClick)
                        .testTag("roster_edit_${teammate.id}")
                ) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// Screen 3: Agent Profile
// ==========================================
@Composable
fun AgentProfileScreen(
    teammate: Teammate,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenDm: () -> Unit,
    onVoiceCall: () -> Unit,
    onOpenDesktop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current

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
            GituBackButton(onClick = onBack, testTag = "profile_back_button")
            Text("Agent profile", style = MaterialTheme.typography.headlineSmall)
            Surface(
                color = gituColors.surfaceContainerLow,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onEdit)
                    .testTag("profile_header_edit_button")
            ) {
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Block
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AgentAvatar(
                        imageUrl = teammate.avatarUrl,
                        fallbackInitials = teammate.shortName,
                        contentDescription = teammate.name,
                        size = 96.dp,
                        ringColor = MaterialTheme.colorScheme.primary,
                        status = teammate.status
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(teammate.name, style = MaterialTheme.typography.headlineLarge)
                        Text(
                            text = "${teammate.roleTitle} • ${teammate.computeTarget.label} computer",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 4-Column Quick Action Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileQuickActionTile(
                            icon = Icons.Filled.ChatBubble,
                            label = "DM",
                            isPrimary = true,
                            onClick = onOpenDm,
                            testTag = "profile_dm_button",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileQuickActionTile(
                            icon = Icons.Filled.Call,
                            label = "Call",
                            isPrimary = false,
                            onClick = onVoiceCall,
                            testTag = "profile_call_button",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileQuickActionTile(
                            icon = Icons.Filled.Devices,
                            label = "Desktop",
                            isPrimary = false,
                            onClick = onOpenDesktop,
                            testTag = "profile_desktop_button",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileQuickActionTile(
                            icon = Icons.Filled.Tune,
                            label = "Edit",
                            isPrimary = false,
                            onClick = onEdit,
                            testTag = "profile_edit_button",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Stats 2-Column Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Model & effort", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${teammate.modelId} • ${teammate.effortLevel}", style = GituMonoSm)
                        }
                    }
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        val permText = buildList {
                            if (teammate.allowShell) add("Shell")
                            if (teammate.allowWriteFiles) add("Write")
                            if (isEmpty()) add("Read-only")
                        }.joinToString(" + ")
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Permissions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(permText, style = GituMonoSm)
                        }
                    }
                }
            }

            // Skills & Connected Apps
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Skills & apps", style = MaterialTheme.typography.titleMedium)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            teammate.skills.forEach { skill ->
                                GituBadgePill(
                                    text = skill,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    isMono = true
                                )
                            }
                            teammate.connectedApps.forEach { app ->
                                Surface(
                                    color = gituColors.surfaceContainerLow,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = app,
                                        style = GituMonoSm,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Persistent Memory
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Persistent memory", style = MaterialTheme.typography.titleMedium)
                            Text("${teammate.memoryFactsCount} facts", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = teammate.memorySummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileQuickActionTile(
    icon: ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isPrimary) MaterialTheme.colorScheme.primary else LocalGituColors.current.surfaceContainerLowest,
        contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp),
        border = if (isPrimary) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ==========================================
// Screen 4: Teammate Profile Editor
// ==========================================
@Composable
fun TeammateProfileEditorScreen(
    initialTeammate: Teammate?,
    onBack: () -> Unit,
    onSave: (Teammate) -> Unit,
    onDelete: (String) -> Unit,
    onOpenConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    val isNew = initialTeammate == null

    var name by remember(initialTeammate) { mutableStateOf(initialTeammate?.name ?: "") }
    var preset by remember(initialTeammate) { mutableStateOf(initialTeammate?.roleFilter ?: "Engineer") }
    var provider by remember(initialTeammate) { mutableStateOf(initialTeammate?.provider ?: "Anthropic") }
    var modelId by remember(initialTeammate) { mutableStateOf(initialTeammate?.modelId ?: "claude-3-7") }
    var effortLevel by remember(initialTeammate) { mutableStateOf(initialTeammate?.effortLevel ?: "High") }
    var computeTarget by remember(initialTeammate) { mutableStateOf(initialTeammate?.computeTarget ?: ComputeTarget.CLOUD) }
    var systemPrompt by remember(initialTeammate) {
        mutableStateOf(
            initialTeammate?.systemPrompt
                ?: "Verify tests before requesting git push approvals, and summarize diffs concisely for mobile review."
        )
    }
    var skillSearch by remember { mutableStateOf("") }
    var activeSkills by remember(initialTeammate) {
        mutableStateOf(initialTeammate?.skills?.toSet() ?: setOf("git-review", "android-fcm", "livekit-voice", "a2ui-cards"))
    }
    var allowShell by remember(initialTeammate) { mutableStateOf(initialTeammate?.allowShell ?: true) }
    var allowWrite by remember(initialTeammate) { mutableStateOf(initialTeammate?.allowWriteFiles ?: true) }
    var allowSetup by remember(initialTeammate) { mutableStateOf(initialTeammate?.allowToolSetup ?: false) }
    var allowProactive by remember(initialTeammate) { mutableStateOf(initialTeammate?.allowProactive ?: false) }

    val allSkills = listOf("git-review", "android-fcm", "livekit-voice", "a2ui-cards", "docker-ops", "web-verify")
    val providers = listOf("Anthropic", "OpenAI", "DeepSeek", "Gemini")
    val models = listOf("claude-3-7", "gpt-4o", "deepseek-r1", "gemini-2.5")

    var providerMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }

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
                GituBackButton(onClick = onBack, testTag = "editor_back_button")
                Text(
                    text = if (isNew) "New teammate" else "Edit teammate",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Button(
                onClick = {
                    val finalName = name.ifBlank { "New Agent" }
                    val short = finalName.split(" ").firstOrNull() ?: finalName
                    val modelShort = when (modelId) {
                        "gpt-4o" -> "GPT-4o"
                        "deepseek-r1" -> "R1"
                        "gemini-2.5" -> "Gemini"
                        else -> "Claude"
                    }
                    onSave(
                        Teammate(
                            id = initialTeammate?.id ?: "agent_${System.currentTimeMillis()}",
                            name = finalName,
                            shortName = short,
                            roleTitle = if (preset == "Engineer") "Staff Engineer" else preset,
                            roleFilter = preset,
                            avatarTag = initialTeammate?.avatarTag ?: "Voxel",
                            avatarUrl = initialTeammate?.avatarUrl ?: GituImageUrls.KAI_AVATAR,
                            status = initialTeammate?.status ?: AgentStatus.ONLINE,
                            provider = provider,
                            modelId = modelId,
                            modelShort = modelShort,
                            effortLevel = effortLevel,
                            computeTarget = computeTarget,
                            systemPrompt = systemPrompt,
                            skills = activeSkills.toList(),
                            connectedApps = initialTeammate?.connectedApps ?: listOf("GitHub", "Slack"),
                            allowShell = allowShell,
                            allowWriteFiles = allowWrite,
                            allowToolSetup = allowSetup,
                            allowProactive = allowProactive,
                            memoryFactsCount = initialTeammate?.memoryFactsCount ?: 14,
                            memorySummary = initialTeammate?.memorySummary ?: "Configured via mobile teammate editor."
                        )
                    )
                },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                modifier = Modifier.testTag("editor_save_button")
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity & Instruction Preset
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.size(64.dp)) {
                            AgentAvatar(
                                imageUrl = initialTeammate?.avatarUrl ?: GituImageUrls.KAI_AVATAR,
                                fallbackInitials = name.ifBlank { "AI" },
                                contentDescription = "Teammate avatar",
                                size = 64.dp
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Change avatar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Teammate name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("e.g. Kai") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("editor_name_input")
                            )
                        }
                    }

                    Text("Instruction preset", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val presets = listOf("Coordinator", "Engineer", "Researcher", "Writer")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.chunked(2).forEach { rowPresets ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowPresets.forEach { item ->
                                    val selected = preset == item
                                    Surface(
                                        color = if (selected) MaterialTheme.colorScheme.primary else gituColors.surfaceContainerLowest,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { preset = item }
                                            .testTag("editor_preset_${item.lowercase()}")
                                    ) {
                                        Text(
                                            text = item,
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

            // Model & Reasoning Card
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Model & reasoning", style = MaterialTheme.typography.titleMedium)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Provider", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(
                                        color = gituColors.surfaceContainerLow,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { providerMenuExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(provider, style = GituMonoSm)
                                            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                DropdownMenu(expanded = providerMenuExpanded, onDismissRequest = { providerMenuExpanded = false }) {
                                    providers.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p, style = GituMonoSm) },
                                            onClick = {
                                                provider = p
                                                providerMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Model", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(
                                        color = gituColors.surfaceContainerLow,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { modelMenuExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(modelId, style = GituMonoSm)
                                            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                DropdownMenu(expanded = modelMenuExpanded, onDismissRequest = { modelMenuExpanded = false }) {
                                    models.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m, style = GituMonoSm) },
                                            onClick = {
                                                modelId = m
                                                modelMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text("Effort level", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(color = gituColors.surfaceContainerLow, shape = CircleShape) {
                            Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                listOf("Low", "Medium", "High").forEach { level ->
                                    val selected = effortLevel == level
                                    Surface(
                                        color = if (selected) gituColors.surfaceContainerLowest else Color.Transparent,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                            .clickable { effortLevel = level }
                                    ) {
                                        Text(
                                            text = level,
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
                }
            }

            // Compute Target 3-Col Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Compute target", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ComputeTarget.entries.forEach { target ->
                            val selected = computeTarget == target
                            val icon = when (target) {
                                ComputeTarget.MY_PC -> Icons.Filled.LaptopMac
                                ComputeTarget.DESKTOP -> Icons.Filled.DesktopWindows
                                ComputeTarget.CLOUD -> Icons.Filled.CloudDone
                            }
                            GituSurfaceCard(
                                containerColor = if (selected) gituColors.surfaceContainerLow else gituColors.surfaceContainerLowest,
                                borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                borderWidth = if (selected) 2.dp else 1.dp,
                                modifier = Modifier.weight(1f),
                                onClick = { computeTarget = target }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = target.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        target.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(target.subtitle, style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // System Prompt
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("System prompt", style = MaterialTheme.typography.titleMedium)
                            Text("${initialTeammate?.memoryFactsCount ?: 0} facts • ${activeSkills.size} skills", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = systemPrompt,
                            onValueChange = { systemPrompt = it },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Skills Search & Grid
            item {
                GituSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Skills", style = MaterialTheme.typography.titleMedium)
                            Text("${activeSkills.size} active", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = skillSearch,
                            onValueChange = { skillSearch = it },
                            placeholder = { Text("Search skills (e.g. git, fcm, mcp)...", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        val visibleSkills = allSkills.filter { skillSearch.isBlank() || it.contains(skillSearch, ignoreCase = true) }
                        visibleSkills.chunked(2).forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { skill ->
                                    val active = activeSkills.contains(skill)
                                    Surface(
                                        color = if (active) MaterialTheme.colorScheme.secondaryContainer else gituColors.surfaceContainerLow,
                                        contentColor = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                activeSkills = if (active) activeSkills - skill else activeSkills + skill
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(skill, style = GituMonoSm, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Icon(
                                                imageVector = if (active) Icons.Filled.CheckCircle else Icons.Outlined.AddCircleOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Memory & Apps Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GituSurfaceCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Memory", style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Text("${initialTeammate?.memoryFactsCount ?: 14} facts", style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    GituSurfaceCard(modifier = Modifier.weight(1f), onClick = onOpenConnections) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Apps & MCP", style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Text((initialTeammate?.connectedApps ?: listOf("GitHub", "Slack")).joinToString(", "), style = GituMonoSm, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Permissions 2x2 Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Permissions", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionToggleTile("Shell", "Run CLI", allowShell, { allowShell = it }, Modifier.weight(1f))
                        PermissionToggleTile("Write files", "Edit code", allowWrite, { allowWrite = it }, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionToggleTile("Tool setup", "Install deps", allowSetup, { allowSetup = it }, Modifier.weight(1f))
                        PermissionToggleTile("Proactive", "Auto alerts", allowProactive, { allowProactive = it }, Modifier.weight(1f))
                    }
                }
            }

            // Delete Button
            if (initialTeammate != null) {
                item {
                    OutlinedButton(
                        onClick = { onDelete(initialTeammate.id) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("editor_delete_button")
                    ) {
                        Text("Delete teammate", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionToggleTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    GituSurfaceCard(
        modifier = modifier,
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
