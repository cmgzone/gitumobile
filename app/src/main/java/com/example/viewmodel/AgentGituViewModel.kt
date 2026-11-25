package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgentGituUiState(
    val serverName: String = "LAN • 192.168.1.42",
    val serverModeLan: Boolean = true,
    val serverUrl: String = "http://192.168.1.42:8321",
    val serverHealthOk: Boolean = true,
    val authEmail: String = "owner@agentgitu.local",
    val teammates: List<Teammate> = defaultTeammates(),
    val threads: List<ConversationThread> = defaultThreads(),
    val selectedThreadCategory: ThreadCategory = ThreadCategory.ALL,
    val hubSearchQuery: String = "",
    val isHubSearchVisible: Boolean = false,
    val pendingApproval: PendingApproval = PendingApproval(),
    val groupTopics: List<String> = listOf("#mobile-fcm", "#voice-polling", "#qa-checks"),
    val activeGroupTopic: String = "#mobile-fcm",
    val groupMessages: List<GroupMessage> = defaultGroupMessages(),
    val privateMessages: List<PrivateMessage> = defaultPrivateMessages(),
    val sharedInboxMessages: List<SharedInboxMessage> = defaultSharedInboxMessages(),
    val sharedInboxInviteCode: String = "join/DP-84K9",
    val sharedInboxInviteRevoked: Boolean = false,
    val codingModeIsAgent: Boolean = true,
    val codingSessions: List<CodingRunSession> = defaultCodingSessions(),
    val activeCodingSessionId: String = "s1",
    val codingMessages: List<CodingRunMessage> = defaultCodingMessages(),
    val isCodingRunActive: Boolean = true,
    val desktopTarget: String = "Private", // "My PC", "Private", "Cloud"
    val desktopHumanControl: Boolean = false,
    val desktopSleeping: Boolean = false,
    val desktopBrowserUrl: String = "localhost:3000/login",
    val voiceCallMode: String = "Ask", // "Ask", "Steer", "Queue"
    val voiceCallMuted: Boolean = false,
    val voiceCallSpeaker: Boolean = true,
    val voiceCallSeconds: Int = 134, // 02:14
    val voiceTranscript: String = "\"Kai finished all 18 FCM rotation tests on feat/mobile-fcm. Want me to approve the push gate?\"",
    val autoApprovalsToday: Int = 12,
    val governanceRules: List<GovernanceRule> = defaultGovernanceRules(),
    val auditLogs: List<AuditLogEntry> = defaultAuditLogs(),
    val localPcConnected: Boolean = true,
    val localPcName: String = "MacBook-Pro-Local",
    val localPcLatencyMs: Int = 12,
    val localPcPairingCode: String = "PAIR-7F92",
    val allowReadFiles: Boolean = true,
    val allowWriteFiles: Boolean = true,
    val allowRunCommands: Boolean = false,
    val allowOpenUrls: Boolean = true,
    val allowAgentChat: Boolean = true,
    val sharedFolders: List<SharedFolderEntry> = defaultSharedFolders(),
    val integrationSearch: String = "",
    val integrationCategory: String = "All",
    val integrationApps: List<IntegrationApp> = defaultIntegrationApps(),
    val gateways: List<GatewayItem> = defaultGateways(),
    val fcmPushEnabled: Boolean = true,
    val spendCurrentUsd: String = "$12.40",
    val spendCeilingUsd: String = "$25.00",
    val modelFallbackChain: String = "Anthropic → OpenAI → DeepSeek",
    val activeSchedulesCount: Int = 3,
    val snackbarMessage: String? = null
)

class AgentGituViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AgentGituUiState())
    val uiState: StateFlow<AgentGituUiState> = _uiState.asStateFlow()

    private var voiceTimerJob: Job? = null

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    // --- Cowork Hub ---
    fun selectThreadCategory(category: ThreadCategory) {
        _uiState.update { it.copy(selectedThreadCategory = category) }
    }

    fun toggleHubSearch() {
        _uiState.update {
            it.copy(
                isHubSearchVisible = !it.isHubSearchVisible,
                hubSearchQuery = if (it.isHubSearchVisible) "" else it.hubSearchQuery
            )
        }
    }

    fun updateHubSearchQuery(query: String) {
        _uiState.update { it.copy(hubSearchQuery = query) }
    }

    fun approvePushGate() {
        _uiState.update { state ->
            if (state.pendingApproval.isApproved) return@update state
            val updatedLogs = state.auditLogs.map {
                if (it.isGate) it.copy(title = "Approved git push feat/mobile-fcm", isGate = false)
                else it
            }
            val updatedTeammates = state.teammates.map {
                if (it.id == "kai") it.copy(status = AgentStatus.ONLINE) else it
            }
            val newGroupMsg = GroupMessage(
                id = "gm_${System.currentTimeMillis()}",
                topic = state.activeGroupTopic,
                isUser = false,
                senderName = "Chief of Staff",
                senderRole = "Coordinator",
                avatarUrl = GituImageUrls.CHIEF_AVATAR,
                timestamp = "14:06",
                body = "Push gate approved. Kai pushed 3 commits to feat/mobile-fcm and cleared the mobile-push check."
            )
            state.copy(
                pendingApproval = state.pendingApproval.copy(isApproved = true),
                autoApprovalsToday = state.autoApprovalsToday + 1,
                auditLogs = updatedLogs,
                teammates = updatedTeammates,
                groupMessages = state.groupMessages + newGroupMsg,
                snackbarMessage = "Approved git push on feat/mobile-fcm"
            )
        }
    }

    fun createNewCoworkThread(title: String) {
        if (title.isBlank()) return
        val newThread = ConversationThread(
            id = "thread_${System.currentTimeMillis()}",
            title = title.trim(),
            category = ThreadCategory.TEAM,
            preview = "@Chief: Workspace initialized and ready for teammates.",
            timestamp = "Just now",
            unreadCount = 0,
            avatarUrl = GituImageUrls.CHIEF_AVATAR,
            targetRoute = "group_chat/core_product"
        )
        _uiState.update {
            it.copy(
                threads = listOf(newThread) + it.threads,
                snackbarMessage = "Started thread \"${title.trim()}\""
            )
        }
    }

    // --- Group Chat ---
    fun selectGroupTopic(topic: String) {
        _uiState.update { it.copy(activeGroupTopic = topic) }
    }

    fun addGroupTopic(topic: String) {
        val formatted = if (topic.startsWith("#")) topic.trim() else "#${topic.trim()}"
        if (formatted.length <= 1) return
        _uiState.update {
            if (it.groupTopics.contains(formatted)) it.copy(activeGroupTopic = formatted)
            else it.copy(groupTopics = it.groupTopics + formatted, activeGroupTopic = formatted)
        }
    }

    fun sendGroupMessage(text: String) {
        if (text.isBlank()) return
        val trimmed = text.trim()
        val userMsg = GroupMessage(
            id = "gm_u_${System.currentTimeMillis()}",
            topic = _uiState.value.activeGroupTopic,
            isUser = true,
            senderName = "You",
            timestamp = "Now",
            body = trimmed
        )
        val replyAgent = when {
            trimmed.contains("@Maya", ignoreCase = true) -> Triple("Maya", "Researcher", GituImageUrls.MAYA_AVATAR)
            trimmed.contains("@Chief", ignoreCase = true) -> Triple("Chief of Staff", "Coordinator", GituImageUrls.CHIEF_AVATAR)
            else -> Triple("Kai", "Engineer", GituImageUrls.KAI_AVATAR)
        }
        val agentReply = GroupMessage(
            id = "gm_a_${System.currentTimeMillis() + 1}",
            topic = _uiState.value.activeGroupTopic,
            isUser = false,
            senderName = replyAgent.first,
            senderRole = replyAgent.second,
            avatarUrl = replyAgent.third,
            timestamp = "Now",
            body = "Acknowledged on ${_uiState.value.activeGroupTopic}: queued \"${trimmed.take(48)}\" for execution."
        )
        _uiState.update {
            it.copy(groupMessages = it.groupMessages + userMsg + agentReply)
        }
    }

    // --- Private 1-on-1 Chat ---
    fun sendPrivateMessage(agentId: String, text: String) {
        if (text.isBlank()) return
        val teammate = _uiState.value.teammates.find { it.id == agentId } ?: _uiState.value.teammates.first()
        val userMsg = PrivateMessage(
            id = "pm_u_${System.currentTimeMillis()}",
            agentId = agentId,
            isUser = true,
            senderName = "You",
            avatarUrl = "",
            timestamp = "Now",
            body = text.trim()
        )
        val replyMsg = PrivateMessage(
            id = "pm_a_${System.currentTimeMillis() + 1}",
            agentId = agentId,
            isUser = false,
            senderName = teammate.name,
            avatarUrl = teammate.avatarUrl,
            timestamp = "Now",
            body = "Got it. Running ${teammate.modelShort} on ${teammate.computeTarget.label} — updating private context now."
        )
        _uiState.update {
            it.copy(privateMessages = it.privateMessages + userMsg + replyMsg)
        }
    }

    // --- Shared Inbox Room ---
    fun sendSharedInboxMessage(text: String) {
        if (text.isBlank()) return
        val trimmed = text.trim()
        val userMsg = SharedInboxMessage(
            id = "sim_u_${System.currentTimeMillis()}",
            isExternalGuest = false,
            isUser = true,
            senderName = "You (Owner)",
            initials = "YO",
            timestamp = "Now",
            body = trimmed
        )
        val mayaMsg = if (trimmed.contains("@Maya", ignoreCase = true)) {
            listOf(
                SharedInboxMessage(
                    id = "sim_m_${System.currentTimeMillis() + 1}",
                    isExternalGuest = false,
                    isUser = false,
                    senderName = "Maya",
                    badge = "read_only",
                    avatarUrl = GituImageUrls.MAYA_AVATAR,
                    timestamp = "Now",
                    body = "Shared read-only summary attached. No workspace credentials or local files exposed."
                )
            )
        } else emptyList()

        _uiState.update {
            it.copy(sharedInboxMessages = it.sharedInboxMessages + userMsg + mayaMsg)
        }
    }

    fun toggleInviteRevoked() {
        _uiState.update {
            val nextRevoked = !it.sharedInboxInviteRevoked
            it.copy(
                sharedInboxInviteRevoked = nextRevoked,
                sharedInboxInviteCode = if (nextRevoked) "Revoked" else "join/DP-92M4",
                snackbarMessage = if (nextRevoked) "Invite link revoked" else "Generated new invite code"
            )
        }
    }

    // --- Teammates Roster & Editor ---
    fun saveTeammate(updated: Teammate) {
        _uiState.update { state ->
            val exists = state.teammates.any { it.id == updated.id }
            val nextList = if (exists) {
                state.teammates.map { if (it.id == updated.id) updated else it }
            } else {
                state.teammates + updated
            }
            state.copy(
                teammates = nextList,
                snackbarMessage = "Saved ${updated.name}'s profile"
            )
        }
    }

    fun deleteTeammate(agentId: String) {
        _uiState.update { state ->
            val target = state.teammates.find { it.id == agentId }
            state.copy(
                teammates = state.teammates.filterNot { it.id == agentId },
                snackbarMessage = "Removed ${target?.name ?: "teammate"}"
            )
        }
    }

    // --- Coding Mode ---
    fun setCodingMode(isAgent: Boolean) {
        _uiState.update { it.copy(codingModeIsAgent = isAgent) }
    }

    fun selectCodingSession(sessionId: String) {
        _uiState.update { it.copy(activeCodingSessionId = sessionId) }
    }

    fun createCodingSession() {
        val nextIndex = _uiState.value.codingSessions.size + 1
        val newSession = CodingRunSession(
            id = "s_$nextIndex",
            title = "New coding task #$nextIndex",
            repo = "agent-gitu",
            branch = "feat/task-$nextIndex",
            statusText = "Ready • 0/4",
            isRunning = false
        )
        _uiState.update {
            it.copy(
                codingSessions = listOf(newSession) + it.codingSessions,
                activeCodingSessionId = newSession.id,
                snackbarMessage = "Created session ${newSession.branch}"
            )
        }
    }

    fun toggleStopCodingRun() {
        _uiState.update {
            val nextActive = !it.isCodingRunActive
            it.copy(
                isCodingRunActive = nextActive,
                snackbarMessage = if (nextActive) "Resumed coding run" else "Stopped active run at checkpoint"
            )
        }
    }

    fun sendCodingPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = CodingRunMessage(
            id = "cm_u_${System.currentTimeMillis()}",
            isUser = true,
            timestamp = "Now",
            body = prompt.trim()
        )
        val agentMsg = CodingRunMessage(
            id = "cm_a_${System.currentTimeMillis() + 1}",
            isUser = false,
            timestamp = "Now",
            body = if (_uiState.value.codingModeIsAgent) {
                "Steered active run: applying changes and re-running test suite."
            } else {
                "Chat mode reply: inspected workspace context without modifying files."
            },
            reasoningSummary = "Reasoned 2s • 2 files checked",
            reasoningDetails = "Checked diff status on feat/mobile-fcm and verified existing test assertions."
        )
        _uiState.update {
            it.copy(
                codingMessages = it.codingMessages + userMsg + agentMsg,
                isCodingRunActive = true
            )
        }
    }

    // --- Live Remote Desktop ---
    fun setDesktopTarget(target: String) {
        _uiState.update { it.copy(desktopTarget = target) }
    }

    fun toggleDesktopControl() {
        _uiState.update {
            val next = !it.desktopHumanControl
            it.copy(
                desktopHumanControl = next,
                snackbarMessage = if (next) "Human control active — agent shell paused" else "Returned control to Kai"
            )
        }
    }

    fun toggleDesktopSleep() {
        _uiState.update {
            val next = !it.desktopSleeping
            it.copy(
                desktopSleeping = next,
                snackbarMessage = if (next) "XFCE container suspended" else "XFCE container resumed"
            )
        }
    }

    // --- LiveKit Voice Call ---
    fun startVoiceTimer() {
        if (voiceTimerJob?.isActive == true) return
        voiceTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(voiceCallSeconds = it.voiceCallSeconds + 1) }
            }
        }
    }

    fun stopVoiceTimer() {
        voiceTimerJob?.cancel()
        voiceTimerJob = null
    }

    fun setVoiceMode(mode: String) {
        val transcript = when (mode) {
            "Steer" -> "\"Steering Kai: keep bounded cursor polling at 3s backoff and skip legacy retry queue.\""
            "Queue" -> "\"Queued follow-up after push gate: notify Design Partner Room once APK builds.\""
            else -> "\"Kai finished all 18 FCM rotation tests on feat/mobile-fcm. Want me to approve the push gate?\""
        }
        _uiState.update { it.copy(voiceCallMode = mode, voiceTranscript = transcript) }
    }

    fun toggleVoiceMute() {
        _uiState.update { it.copy(voiceCallMuted = !it.voiceCallMuted) }
    }

    fun toggleVoiceSpeaker() {
        _uiState.update { it.copy(voiceCallSpeaker = !it.voiceCallSpeaker) }
    }

    // --- Governance ---
    fun toggleGovernanceRule(ruleId: String) {
        _uiState.update { state ->
            state.copy(
                governanceRules = state.governanceRules.map {
                    if (it.id == ruleId) it.copy(enabled = !it.enabled) else it
                }
            )
        }
    }

    fun addGovernanceRule(pattern: String, description: String) {
        if (pattern.isBlank()) return
        val newRule = GovernanceRule(
            id = "rule_${System.currentTimeMillis()}",
            pattern = pattern.trim(),
            description = description.ifBlank { "Auto-approve • Custom policy" },
            enabled = true
        )
        _uiState.update {
            it.copy(
                governanceRules = it.governanceRules + newRule,
                snackbarMessage = "Added standing rule: ${newRule.pattern}"
            )
        }
    }

    fun revokeAuditEntry(entryId: String) {
        _uiState.update { state ->
            state.copy(
                auditLogs = state.auditLogs.map {
                    if (it.id == entryId) it.copy(isRevoked = true) else it
                },
                snackbarMessage = "Permission revoked from audit log"
            )
        }
    }

    // --- Local PC Link ---
    fun toggleLocalPcConnection() {
        _uiState.update {
            val next = !it.localPcConnected
            it.copy(
                localPcConnected = next,
                snackbarMessage = if (next) "Paired MacBook-Pro-Local" else "Workstation link revoked"
            )
        }
    }

    fun pingLocalPc() {
        val ms = (8..19).random()
        _uiState.update {
            it.copy(
                localPcLatencyMs = ms,
                snackbarMessage = "WebSocket health check OK (${ms}ms)"
            )
        }
    }

    fun toggleLocalPolicy(key: String) {
        _uiState.update { state ->
            when (key) {
                "read" -> state.copy(allowReadFiles = !state.allowReadFiles)
                "write" -> state.copy(allowWriteFiles = !state.allowWriteFiles)
                "run" -> state.copy(allowRunCommands = !state.allowRunCommands)
                "url" -> state.copy(allowOpenUrls = !state.allowOpenUrls)
                "chat" -> state.copy(allowAgentChat = !state.allowAgentChat)
                else -> state
            }
        }
    }

    fun addSharedFolder(path: String, readOnly: Boolean) {
        if (path.isBlank()) return
        val entry = SharedFolderEntry(
            id = "folder_${System.currentTimeMillis()}",
            path = path.trim(),
            accessSummary = if (readOnly) "Read-only • 0 files" else "Read & Write • 0 files"
        )
        _uiState.update {
            it.copy(
                sharedFolders = it.sharedFolders + entry,
                snackbarMessage = "Added folder ${entry.path}"
            )
        }
    }

    fun removeSharedFolder(folderId: String) {
        _uiState.update { state ->
            state.copy(sharedFolders = state.sharedFolders.filterNot { it.id == folderId })
        }
    }

    // --- Connections & Integrations ---
    fun updateIntegrationSearch(q: String) {
        _uiState.update { it.copy(integrationSearch = q) }
    }

    fun selectIntegrationCategory(category: String) {
        _uiState.update { it.copy(integrationCategory = category) }
    }

    fun toggleIntegrationApp(appId: String) {
        _uiState.update { state ->
            val updated = state.integrationApps.map {
                if (it.id == appId) it.copy(isConnected = !it.isConnected) else it
            }
            val app = updated.find { it.id == appId }
            state.copy(
                integrationApps = updated,
                snackbarMessage = if (app?.isConnected == true) "Connected ${app.name}" else "Disconnected ${app?.name}"
            )
        }
    }

    fun addCustomIntegration(name: String, endpoint: String) {
        if (name.isBlank()) return
        val item = IntegrationApp(
            id = "custom_${System.currentTimeMillis()}",
            name = name.trim(),
            category = "Gateways",
            iconKey = "api",
            subtitle = endpoint.ifBlank { "Custom REST / MCP" },
            isConnected = true
        )
        _uiState.update {
            it.copy(
                integrationApps = it.integrationApps + item,
                snackbarMessage = "Added custom integration ${item.name}"
            )
        }
    }

    // --- Settings & Server Pairing ---
    fun toggleFcmPush() {
        _uiState.update { it.copy(fcmPushEnabled = !it.fcmPushEnabled) }
    }

    fun updateSpendCeiling(ceiling: String) {
        if (ceiling.isNotBlank()) {
            _uiState.update {
                it.copy(
                    spendCeilingUsd = if (ceiling.startsWith("$")) ceiling else "$$ceiling",
                    snackbarMessage = "Updated spend ceiling"
                )
            }
        }
    }

    fun switchServerMode(isLan: Boolean) {
        val url = if (isLan) "http://192.168.1.42:8321" else "https://gitu.tailnet.ts.net"
        val label = if (isLan) "LAN • 192.168.1.42" else "HTTPS • tailnet.ts.net"
        _uiState.update {
            it.copy(
                serverModeLan = isLan,
                serverUrl = url,
                serverName = label,
                serverHealthOk = true
            )
        }
    }

    fun updateServerUrl(url: String) {
        val host = url.removePrefix("http://").removePrefix("https://").substringBefore(":")
        val prefix = if (_uiState.value.serverModeLan) "LAN" else "HTTPS"
        _uiState.update {
            it.copy(
                serverUrl = url,
                serverName = "$prefix • $host"
            )
        }
    }

    fun updateAuthEmail(email: String) {
        _uiState.update { it.copy(authEmail = email) }
    }

    fun testServerHealth() {
        _uiState.update {
            it.copy(
                serverHealthOk = true,
                snackbarMessage = "/healthz 200 OK — Server reachable"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopVoiceTimer()
    }
}

private fun defaultTeammates() = listOf(
    Teammate(
        id = "chief",
        name = "Chief of Staff",
        shortName = "Chief",
        roleTitle = "Coordinator",
        roleFilter = "Coordinator",
        avatarTag = "Orb",
        avatarUrl = GituImageUrls.CHIEF_AVATAR,
        status = AgentStatus.ONLINE,
        provider = "Anthropic",
        modelId = "claude-3-7",
        modelShort = "Claude",
        effortLevel = "High",
        computeTarget = ComputeTarget.MY_PC,
        systemPrompt = "You are Chief of Staff, coordinating teammates, enforcing standing rules, and escalating high-risk git/shell gates to the user.",
        skills = listOf("git-review", "a2ui-cards", "livekit-voice"),
        connectedApps = listOf("Gmail", "Slack"),
        allowShell = false,
        allowWriteFiles = false,
        allowToolSetup = false,
        allowProactive = true,
        memoryFactsCount = 19,
        memorySummary = "Auto-approves read-only git checks and npm test suites; always routes push gates to user."
    ),
    Teammate(
        id = "kai",
        name = "Kai",
        shortName = "Kai",
        roleTitle = "Staff Engineer",
        roleFilter = "Engineer",
        avatarTag = "Voxel",
        avatarUrl = GituImageUrls.KAI_AVATAR,
        status = AgentStatus.NEEDS_APPROVAL,
        provider = "Anthropic",
        modelId = "claude-3-7",
        modelShort = "Claude",
        effortLevel = "High",
        computeTarget = ComputeTarget.CLOUD,
        systemPrompt = "You are Kai, a staff mobile and backend engineer. Verify tests before requesting git push approvals, and summarize diffs concisely for mobile review.",
        skills = listOf("git-review", "android-fcm", "livekit-voice", "a2ui-cards"),
        connectedApps = listOf("GitHub", "Slack"),
        allowShell = true,
        allowWriteFiles = true,
        allowToolSetup = false,
        allowProactive = false,
        memoryFactsCount = 14,
        memorySummary = "Prefers bounded cursor polling for mobile reconnects; always runs mobile-push suite before requesting push approval."
    ),
    Teammate(
        id = "maya",
        name = "Maya",
        shortName = "Maya",
        roleTitle = "Researcher",
        roleFilter = "Researcher",
        avatarTag = "Plush",
        avatarUrl = GituImageUrls.MAYA_AVATAR,
        status = AgentStatus.ONLINE,
        provider = "OpenAI",
        modelId = "gpt-4o",
        modelShort = "GPT-4o",
        effortLevel = "Medium",
        computeTarget = ComputeTarget.DESKTOP,
        systemPrompt = "You are Maya, a product researcher. Synthesize Gmail partner threads, draft payload contracts, and keep Shared Inbox responses strictly read-only.",
        skills = listOf("a2ui-cards", "web-verify"),
        connectedApps = listOf("Gmail", "Notion"),
        allowShell = false,
        allowWriteFiles = true,
        allowToolSetup = false,
        allowProactive = true,
        memoryFactsCount = 11,
        memorySummary = "Maintains partner digest for Acme Mobile & Northstar; strips workspace secrets from external shared rooms."
    ),
    Teammate(
        id = "sora",
        name = "Sora",
        shortName = "Sora",
        roleTitle = "DevOps",
        roleFilter = "Engineer",
        avatarTag = "Plush",
        avatarUrl = GituImageUrls.SORA_AVATAR,
        status = AgentStatus.SLEEPING,
        provider = "DeepSeek",
        modelId = "deepseek-r1",
        modelShort = "R1",
        effortLevel = "High",
        computeTarget = ComputeTarget.CLOUD,
        systemPrompt = "You are Sora, managing Docker containers, XFCE noVNC sandboxes, and Postgres MCP migrations.",
        skills = listOf("docker-ops", "git-review"),
        connectedApps = listOf("GitHub"),
        allowShell = true,
        allowWriteFiles = true,
        allowToolSetup = true,
        allowProactive = false,
        memoryFactsCount = 8,
        memorySummary = "Keeps XFCE desktop warm during active runs; suspends idle cloud containers after 20 minutes."
    )
)

private fun defaultThreads() = listOf(
    ConversationThread(
        id = "core_product",
        title = "Core Product Team",
        category = ThreadCategory.TEAM,
        preview = "@Maya: Drafted the Android FCM payload spec with 2 test artifacts.",
        timestamp = "14:05",
        unreadCount = 3,
        avatarUrl = GituImageUrls.KAI_AVATAR,
        targetRoute = "group_chat/core_product"
    ),
    ConversationThread(
        id = "chief_dm",
        title = "Chief of Staff",
        category = ThreadCategory.DM,
        preview = "Auto-approved 2 read-only git checks. 1 push gate awaits your tap.",
        timestamp = "13:48",
        unreadCount = 0,
        avatarUrl = GituImageUrls.CHIEF_AVATAR,
        targetRoute = "private_chat/chief"
    ),
    ConversationThread(
        id = "voice_livekit",
        title = "Mobile Voice & LiveKit",
        category = ThreadCategory.TEAM,
        preview = "Voice transcript (04:12): Steered Kai to bounded cursor polling.",
        timestamp = "11:20",
        unreadCount = 0,
        iconType = "voice",
        targetRoute = "group_chat/voice_livekit"
    ),
    ConversationThread(
        id = "design_partner",
        title = "Design Partner Room",
        category = ThreadCategory.INBOX,
        preview = "Shared Inbox • 2 collaborators • Tool policy: read_only",
        timestamp = "Yesterday",
        unreadCount = 0,
        iconType = "inbox",
        targetRoute = "shared_inbox/design_partner"
    )
)

private fun defaultGroupMessages() = listOf(
    GroupMessage(
        id = "gm_1",
        topic = "#mobile-fcm",
        isUser = true,
        senderName = "You",
        timestamp = "14:01",
        body = "@Kai verify the FCM token rotation tests and push to feat/mobile-fcm. @Maya share the payload spec."
    ),
    GroupMessage(
        id = "gm_2",
        topic = "#mobile-fcm",
        isUser = false,
        senderName = "Kai",
        senderRole = "Engineer",
        avatarUrl = GituImageUrls.KAI_AVATAR,
        timestamp = "14:03",
        body = "All 18 FCM rotation tests passed. I staged 3 commits on feat/mobile-fcm and paused at the push gate.",
        reasoningPill = "Reasoned 4s • 18 tests passed",
        reasoningDetails = "Inspected TokenRotator.kt, FcmReconnectService.kt, and mobile-push.test.ts. Verified bounded cursor polling backoff and foreground refresh.",
        hasApprovalGate = true
    ),
    GroupMessage(
        id = "gm_3",
        topic = "#mobile-fcm",
        isUser = false,
        senderName = "Maya",
        senderRole = "Researcher",
        avatarUrl = GituImageUrls.MAYA_AVATAR,
        timestamp = "14:05",
        body = "Drafted the Android FCM payload contract with deep-link routing rules.",
        artifactTitle = "android-fcm-spec.pdf",
        artifactMeta = "12 pages • 248 KB"
    ),
    GroupMessage(
        id = "gm_4",
        topic = "#voice-polling",
        isUser = false,
        senderName = "Kai",
        senderRole = "Engineer",
        avatarUrl = GituImageUrls.KAI_AVATAR,
        timestamp = "11:22",
        body = "Applied bounded cursor polling with 3s max jitter after LiveKit voice steering."
    ),
    GroupMessage(
        id = "gm_5",
        topic = "#qa-checks",
        isUser = false,
        senderName = "Sora",
        senderRole = "DevOps",
        avatarUrl = GituImageUrls.SORA_AVATAR,
        timestamp = "09:40",
        body = "Nightly Android & XFCE Playwright smoke checks passed (24/24)."
    )
)

private fun defaultPrivateMessages() = listOf(
    PrivateMessage(
        id = "pm_1",
        agentId = "maya",
        isUser = true,
        senderName = "You",
        avatarUrl = "",
        timestamp = "10:14",
        body = "Summarize today's partner emails from Gmail and build a quick comparison table."
    ),
    PrivateMessage(
        id = "pm_2",
        agentId = "maya",
        isUser = false,
        senderName = "Maya",
        avatarUrl = GituImageUrls.MAYA_AVATAR,
        timestamp = "10:15",
        body = "Reviewed 6 threads in Gmail. Two design partners confirmed Android FCM testing for Thursday.",
        hasDigestWidget = true
    ),
    PrivateMessage(
        id = "pm_3",
        agentId = "maya",
        isUser = false,
        senderName = "Maya",
        avatarUrl = GituImageUrls.MAYA_AVATAR,
        timestamp = "10:16",
        body = "Maya hit an SSO prompt on Northstar's shared spec link. Take control to sign in.",
        hasHandoffCard = true
    ),
    PrivateMessage(
        id = "pm_c1",
        agentId = "chief",
        isUser = false,
        senderName = "Chief of Staff",
        avatarUrl = GituImageUrls.CHIEF_AVATAR,
        timestamp = "13:48",
        body = "Auto-approved 2 read-only git checks today. Kai's push to feat/mobile-fcm is waiting on your approval."
    ),
    PrivateMessage(
        id = "pm_k1",
        agentId = "kai",
        isUser = false,
        senderName = "Kai",
        avatarUrl = GituImageUrls.KAI_AVATAR,
        timestamp = "14:02",
        body = "Staged 3 verified commits on feat/mobile-fcm (+142 / -28). Ready whenever you clear the gate."
    ),
    PrivateMessage(
        id = "pm_s1",
        agentId = "sora",
        isUser = false,
        senderName = "Sora",
        avatarUrl = GituImageUrls.SORA_AVATAR,
        timestamp = "08:55",
        body = "XFCE container is running on private bridge. Cloud SSH nodes are standing by."
    )
)

private fun defaultSharedInboxMessages() = listOf(
    SharedInboxMessage(
        id = "sim_1",
        isExternalGuest = true,
        isUser = false,
        senderName = "Elena (Acme)",
        initials = "EL",
        badge = "Guest",
        timestamp = "16:12",
        body = "Can @Maya share the read-only summary of the FCM deep-link format?"
    ),
    SharedInboxMessage(
        id = "sim_2",
        isExternalGuest = false,
        isUser = false,
        senderName = "Maya",
        badge = "read_only",
        avatarUrl = GituImageUrls.MAYA_AVATAR,
        timestamp = "16:13",
        body = "Shared Inbox payloads route to agentgitu://inbox/:roomId with no workspace secrets included."
    )
)

private fun defaultCodingSessions() = listOf(
    CodingRunSession("s1", "FCM token rotation", "agent-gitu", "feat/mobile-fcm", "Running • 3/4", true),
    CodingRunSession("s2", "Cursor events API", "agent-gitu", "feat/cursor-api", "Done • 12m ago", false),
    CodingRunSession("s3", "QR pairing handler", "agent-gitu", "fix/qr-pairing", "Done • 1h ago", false)
)

private fun defaultCodingMessages() = listOf(
    CodingRunMessage(
        id = "cm_1",
        isUser = true,
        timestamp = "13:58",
        body = "Wire FCM token rotation on foreground and add deep-link routing for Cowork threads."
    ),
    CodingRunMessage(
        id = "cm_2",
        isUser = false,
        timestamp = "14:01",
        body = "Updated workspace-app.tsx and opened the live browser to verify the OAuth login redirect.",
        reasoningSummary = "Reasoned 6s • 4 files inspected",
        reasoningDetails = "Inspected workspace-app.tsx, push-router.ts, deep-link.kt, and mobile-push.test.ts. Verified token rotation hook runs once per foreground resume.",
        hasLiveBrowser = true,
        evidenceCommand = "$ npm test mobile-push (18/18)",
        evidencePassed = true
    )
)

private fun defaultGovernanceRules() = listOf(
    GovernanceRule("r1", "npm test *", "Auto-approve • Shell tests", true),
    GovernanceRule("r2", "git status / diff", "Auto-approve • Read-only", true),
    GovernanceRule("r3", "\"Use cursor polling?\"", "Auto-answer • Mobile API", true)
)

private fun defaultAuditLogs() = listOf(
    AuditLogEntry("a1", "Approved npm test mobile-push", "Kai • 14:02", isGate = false),
    AuditLogEntry("a2", "Escalated git push feat/mobile-fcm", "Kai • 14:03", isGate = true),
    AuditLogEntry("a3", "Routed FCM spec to @Maya", "Core Team • 13:45", isGate = false)
)

private fun defaultSharedFolders() = listOf(
    SharedFolderEntry("f1", "~/Projects/agent-gitu", "Read & Write • 142 files"),
    SharedFolderEntry("f2", "~/Documents/specs", "Read-only • 18 files")
)

private fun defaultIntegrationApps() = listOf(
    IntegrationApp("gmail", "Gmail", "OAuth Apps", "mail", "Maya, Chief", true),
    IntegrationApp("github", "GitHub", "OAuth Apps", "code", "Kai, Sora", true),
    IntegrationApp("slack", "Slack", "OAuth Apps", "tag", "Team channels", false),
    IntegrationApp("notion", "Notion", "OAuth Apps", "article", "Docs & wikis", false)
)

private fun defaultGateways() = listOf(
    GatewayItem("gw1", "Telegram Bot", "Linked • Core Product Team", "Live", true),
    GatewayItem("gw2", "IMAP / SMTP Mailbox", "Watching inbox • Auto-TLS", "Folders", false),
    GatewayItem("gw3", "MCP Servers", "Playwright, Postgres", "Configure", false)
)
