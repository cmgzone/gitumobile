package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.GituBottomNavBar
import com.example.ui.components.GituNavRail
import com.example.ui.screens.*
import com.example.viewmodel.AgentGituViewModel

const val ROUTE_COWORK_HUB = "cowork_hub"
const val ROUTE_CODING_MODE = "coding_mode"
const val ROUTE_REMOTE_DESKTOP = "remote_desktop"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_TEAMMATES_ROSTER = "teammates_roster"
const val ROUTE_AGENT_PROFILE = "agent_profile/{agentId}"
const val ROUTE_TEAMMATE_EDITOR = "teammate_editor/{agentId}"
const val ROUTE_GROUP_CHAT = "group_chat/{threadId}"
const val ROUTE_PRIVATE_CHAT = "private_chat/{agentId}"
const val ROUTE_SHARED_INBOX = "shared_inbox/{roomId}"
const val ROUTE_LIVEKIT_VOICE = "livekit_voice/{contextName}"
const val ROUTE_GOVERNANCE = "governance"
const val ROUTE_LOCAL_PC_LINK = "local_pc_link"
const val ROUTE_CONNECTIONS = "connections"
const val ROUTE_SERVER_PAIRING = "server_pairing"

private val BottomBarRoutes = setOf(
    ROUTE_COWORK_HUB,
    ROUTE_TEAMMATES_ROSTER,
    ROUTE_CODING_MODE,
    ROUTE_REMOTE_DESKTOP,
    ROUTE_SETTINGS
)

@Composable
fun AgentGituApp(
    viewModel: AgentGituViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_COWORK_HUB
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    val showPrimaryNav = currentRoute in BottomBarRoutes

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen && showPrimaryNav) {
                GituNavRail(
                    currentRoute = currentRoute,
                    onNavigate = { navigateTopTab(it) }
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surface,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (!isWideScreen && showPrimaryNav) {
                            GituBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { navigateTopTab(it) }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                ) { innerPadding ->
                    val statusTopPadding = if (currentRoute != ROUTE_LIVEKIT_VOICE && currentRoute != ROUTE_SERVER_PAIRING) {
                        Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    } else {
                        Modifier
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_COWORK_HUB,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .then(statusTopPadding)
                    ) {
                        // 1. Cowork Hub
                        composable(ROUTE_COWORK_HUB) {
                            CoworkHubScreen(
                                uiState = uiState,
                                onSwitchServerClick = { navController.navigate(ROUTE_SERVER_PAIRING) },
                                onToggleSearch = { viewModel.toggleHubSearch() },
                                onSearchQueryChange = { viewModel.updateHubSearchQuery(it) },
                                onSelectCategory = { viewModel.selectThreadCategory(it) },
                                onApproveGate = { viewModel.approvePushGate() },
                                onCreateThread = { title ->
                                    viewModel.createNewCoworkThread(title)
                                },
                                onOpenAgentProfile = { agentId ->
                                    navController.navigate("agent_profile/$agentId")
                                },
                                onOpenRoster = {
                                    navController.navigate(ROUTE_TEAMMATES_ROSTER)
                                },
                                onOpenThread = { targetRoute ->
                                    navController.navigate(targetRoute)
                                },
                                onOpenVoiceCall = {
                                    navController.navigate("livekit_voice/Chief of Staff")
                                }
                            )
                        }

                        // 2. AI Teammates Roster
                        composable(ROUTE_TEAMMATES_ROSTER) {
                            TeammatesRosterScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onNewTeammate = { navController.navigate("teammate_editor/new") },
                                onOpenProfile = { agentId -> navController.navigate("agent_profile/$agentId") },
                                onEditTeammate = { agentId -> navController.navigate("teammate_editor/$agentId") },
                                onCallTeammate = { name -> navController.navigate("livekit_voice/$name") },
                                onMessageTeammate = { agentId -> navController.navigate("private_chat/$agentId") }
                            )
                        }

                        // 3. Agent Profile
                        composable(
                            route = ROUTE_AGENT_PROFILE,
                            arguments = listOf(navArgument("agentId") { type = NavType.StringType })
                        ) { entry ->
                            val agentId = entry.arguments?.getString("agentId") ?: "kai"
                            val teammate = uiState.teammates.find { it.id == agentId } ?: uiState.teammates.first()
                            AgentProfileScreen(
                                teammate = teammate,
                                onBack = { navController.popBackStack() },
                                onEdit = { navController.navigate("teammate_editor/${teammate.id}") },
                                onOpenDm = { navController.navigate("private_chat/${teammate.id}") },
                                onVoiceCall = { navController.navigate("livekit_voice/${teammate.name}") },
                                onOpenDesktop = { navigateTopTab(ROUTE_REMOTE_DESKTOP) }
                            )
                        }

                        // 4. Teammate Profile Editor
                        composable(
                            route = ROUTE_TEAMMATE_EDITOR,
                            arguments = listOf(navArgument("agentId") { type = NavType.StringType })
                        ) { entry ->
                            val agentId = entry.arguments?.getString("agentId") ?: "new"
                            val teammate = if (agentId == "new") null else uiState.teammates.find { it.id == agentId }
                            TeammateProfileEditorScreen(
                                initialTeammate = teammate,
                                onBack = { navController.popBackStack() },
                                onSave = { updated ->
                                    viewModel.saveTeammate(updated)
                                    navController.popBackStack()
                                },
                                onDelete = { id ->
                                    viewModel.deleteTeammate(id)
                                    navController.popBackStack(ROUTE_TEAMMATES_ROSTER, inclusive = false)
                                },
                                onOpenConnections = { navController.navigate(ROUTE_CONNECTIONS) }
                            )
                        }

                        // 5. Cowork Group Chat
                        composable(
                            route = ROUTE_GROUP_CHAT,
                            arguments = listOf(navArgument("threadId") { type = NavType.StringType })
                        ) {
                            CoworkGroupChatScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onStartVoiceCall = { navController.navigate("livekit_voice/Core Product Team") },
                                onSelectTopic = { viewModel.selectGroupTopic(it) },
                                onAddTopic = { viewModel.addGroupTopic(it) },
                                onApprovePush = { viewModel.approvePushGate() },
                                onSendMessage = { viewModel.sendGroupMessage(it) }
                            )
                        }

                        // 6. Private Agent Chat (1-on-1)
                        composable(
                            route = ROUTE_PRIVATE_CHAT,
                            arguments = listOf(navArgument("agentId") { type = NavType.StringType })
                        ) { entry ->
                            val agentId = entry.arguments?.getString("agentId") ?: "maya"
                            val teammate = uiState.teammates.find { it.id == agentId } ?: uiState.teammates.first()
                            PrivateAgentChatScreen(
                                teammate = teammate,
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onViewProfile = { navController.navigate("agent_profile/${teammate.id}") },
                                onVoiceCall = { navController.navigate("livekit_voice/${teammate.name}") },
                                onOpenDesktop = { navigateTopTab(ROUTE_REMOTE_DESKTOP) },
                                onSendMessage = { viewModel.sendPrivateMessage(teammate.id, it) },
                                onDraftReplyFeedback = { viewModel.showMessage("Drafted Gmail reply for Acme Mobile & Northstar") }
                            )
                        }

                        // 7. Shared Inbox Room & Invites
                        composable(
                            route = ROUTE_SHARED_INBOX,
                            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
                        ) {
                            SharedInboxRoomScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onToggleInviteRevoked = { viewModel.toggleInviteRevoked() },
                                onSendMessage = { viewModel.sendSharedInboxMessage(it) },
                                onCopyFeedback = { viewModel.showMessage(it) }
                            )
                        }

                        // 8. Coding Mode — Runs
                        composable(ROUTE_CODING_MODE) {
                            CodingModeScreen(
                                uiState = uiState,
                                onSetCodingMode = { viewModel.setCodingMode(it) },
                                onSelectSession = { viewModel.selectCodingSession(it) },
                                onCreateSession = { viewModel.createCodingSession() },
                                onToggleStopRun = { viewModel.toggleStopCodingRun() },
                                onSendPrompt = { viewModel.sendCodingPrompt(it) },
                                onOpenDesktop = { navigateTopTab(ROUTE_REMOTE_DESKTOP) }
                            )
                        }

                        // 9. Live Remote Desktop
                        composable(ROUTE_REMOTE_DESKTOP) {
                            LiveRemoteDesktopScreen(
                                uiState = uiState,
                                onSelectTarget = { viewModel.setDesktopTarget(it) },
                                onToggleControl = { viewModel.toggleDesktopControl() },
                                onToggleSleep = { viewModel.toggleDesktopSleep() }
                            )
                        }

                        // 10. LiveKit Voice Call
                        composable(
                            route = ROUTE_LIVEKIT_VOICE,
                            arguments = listOf(navArgument("contextName") { type = NavType.StringType })
                        ) { entry ->
                            val contextName = entry.arguments?.getString("contextName") ?: "Chief of Staff"
                            LiveKitVoiceCallScreen(
                                contextName = contextName,
                                uiState = uiState,
                                onStartTimer = { viewModel.startVoiceTimer() },
                                onStopTimer = { viewModel.stopVoiceTimer() },
                                onMinimize = { navController.popBackStack() },
                                onEndCall = {
                                    viewModel.stopVoiceTimer()
                                    viewModel.showMessage("Voice transcript saved to Cowork")
                                    navController.popBackStack()
                                },
                                onSelectMode = { viewModel.setVoiceMode(it) },
                                onToggleMute = { viewModel.toggleVoiceMute() },
                                onToggleSpeaker = { viewModel.toggleVoiceSpeaker() }
                            )
                        }

                        // 11. Chief of Staff Governance
                        composable(ROUTE_GOVERNANCE) {
                            GovernanceScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onToggleRule = { viewModel.toggleGovernanceRule(it) },
                                onAddRule = { p, d -> viewModel.addGovernanceRule(p, d) },
                                onRevokeAudit = { viewModel.revokeAuditEntry(it) },
                                onApproveGate = { viewModel.approvePushGate() }
                            )
                        }

                        // 12. Settings & Server Pairing
                        composable(ROUTE_SETTINGS) {
                            SettingsScreen(
                                uiState = uiState,
                                onLockApp = { navController.navigate(ROUTE_SERVER_PAIRING) },
                                onOpenServerPairing = { navController.navigate(ROUTE_SERVER_PAIRING) },
                                onOpenGovernance = { navController.navigate(ROUTE_GOVERNANCE) },
                                onOpenLocalPcLink = { navController.navigate(ROUTE_LOCAL_PC_LINK) },
                                onOpenConnections = { navController.navigate(ROUTE_CONNECTIONS) },
                                onToggleFcm = { viewModel.toggleFcmPush() },
                                onUpdateSpendCeiling = { viewModel.updateSpendCeiling(it) }
                            )
                        }

                        // 13. Local PC Link
                        composable(ROUTE_LOCAL_PC_LINK) {
                            LocalPcLinkScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onToggleConnection = { viewModel.toggleLocalPcConnection() },
                                onPingLink = { viewModel.pingLocalPc() },
                                onTogglePolicy = { viewModel.toggleLocalPolicy(it) },
                                onAddFolder = { p, r -> viewModel.addSharedFolder(p, r) },
                                onRemoveFolder = { viewModel.removeSharedFolder(it) },
                                onCopyCodeFeedback = { viewModel.showMessage(it) }
                            )
                        }

                        // 14. Connections & Integrations
                        composable(ROUTE_CONNECTIONS) {
                            ConnectionsScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onSearchChange = { viewModel.updateIntegrationSearch(it) },
                                onSelectCategory = { viewModel.selectIntegrationCategory(it) },
                                onToggleApp = { viewModel.toggleIntegrationApp(it) },
                                onAddCustomApi = { n, e -> viewModel.addCustomIntegration(n, e) },
                                onShowMessage = { viewModel.showMessage(it) }
                            )
                        }

                        // 15. Server Pairing & Auth
                        composable(ROUTE_SERVER_PAIRING) {
                            ServerPairingAuthScreen(
                                uiState = uiState,
                                onSwitchMode = { viewModel.switchServerMode(it) },
                                onUpdateUrl = { viewModel.updateServerUrl(it) },
                                onUpdateEmail = { viewModel.updateAuthEmail(it) },
                                onTestHealth = { viewModel.testServerHealth() },
                                onSignIn = {
                                    viewModel.showMessage("Connected to ${uiState.serverName}")
                                    navController.navigate(ROUTE_COWORK_HUB) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onOAuthFeedback = { viewModel.showMessage(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
