package com.example.model

object GituImageUrls {
    const val CHIEF_AVATAR =
        "https://lh3.googleusercontent.com/aida/ALJJyrBbMQ7D6q2ltHv5P0DqSQJLAANfOpLK6aMsnt97Ab_JeB7xHY2jrvu4Pi_T8MZDgFju9OshXuQv9uR6U-VaZv2pXx3vwwRK7P9-O_eTiQXccjBo81lEDQM5dgpJnePjDq2yFYTzr7nz67z9zWW8Bk4iQKE2EMvRk6nK2n0E6c0ophUARm63JZ84isFGGP2JgNIYONjgabeAzE8FyqNQCWZOXW58mQacH8bq2_IKFYttetog7x0ZNg19xlc6"
    const val KAI_AVATAR =
        "https://lh3.googleusercontent.com/aida/ALJJyrCXTa5InLEkSvDCb94OjJCbtgs-pzECbJAvIk5mKozSzKjGoHLpgyGbsQswU9r9DDY-mk4J1i_wRZFklqxlrO6rNcM55rm4a84U8paHp8J_ySw2LvsGXDqfI_muS0Y-eyOR0XMAs1HD2wO1BQFmp_eJGSLPl4SZedGgS4MfowLXQMgxLCzCtPn5vhP_YbLJPp3THcnhACFqAchtka4b8kTmKX0YHgNjIsxTFC0EJqDUZebr_wm_yyeSCHM"
    const val MAYA_AVATAR =
        "https://lh3.googleusercontent.com/aida/ALJJyrDKpqr6obTJL9UCsfvo-3R3tC4rxX2tNEZ1EUOhi8SbHYu50tODVP243fyHgoZAYQbwLvclFNUfX8hQHZfBqv20MXHb5aw_Q0bnXWpGtXjEdCjP37JGSC3nOfNg_AslGQgCMl_KKmqFIDfsmHF0uQh9DoWBKzMLxMAn3k8SPO3BYjjWKJGNllHy0EhoJMGelVxTgCYrtLkVIdwDVaXiiPWnB2xB9xnOeO4mJgexVMD3Fak8OiGhm3fDp0SNbGrjMQ"
    const val SORA_AVATAR =
        "https://lh3.googleusercontent.com/aida/ALJJyrC8dPXVcZKjoDfPe1t3PMQJfnNneLeJoH5UK9zTz9Vkjx5spYEZlsqg4KxBCpRjh0V8IQjHTOGvF9sPa9aYWlo-XIY64NdWVMVNoIy5u-x6Jy0RGDQdOoxxz2FGFuwlUpSPq8iuf7v7H_bmBDlQykIqNO-xzzVPyxCOeoYW-ZGynR4PiDZZJK5vKZn6hlU3JF9uizXZ9psfndomKzQEcQJvya856Kx-DnRYRtz1ye0PGZj6VadSMksgsSZazcM"
    const val XFCE_DESKTOP_PREVIEW =
        "https://lh3.googleusercontent.com/aida/ALJJyrDxKsvjm6WDtIbAHjAcTiTMuDa0xlwQkfeOI19VR4yCO304Yigmm86-KjjvJYRI0FRD0IYcSqHwdzJWTLRmq_4g2oWm99n_ycp-V9mKTjUbyNonFaF4COEIj4j2eh-HSrlnbE-P8pOqeGyyljWzJCfSkBXOc3oD_s6_0kukeljhvqO2XUkPoEiLRa_hLRdJByFXjBXswe46wVf22SJFJHXLPytQaj4PSv4Urm8T6ogPOUNFc3N1T8PiAOx7TkpcXw"
}

enum class AgentStatus { ONLINE, RUNNING, NEEDS_APPROVAL, SLEEPING }
enum class ThreadCategory { ALL, TEAM, DM, INBOX }
enum class ComputeTarget(val label: String, val subtitle: String) {
    MY_PC("My PC", "LAN"),
    DESKTOP("Desktop", "XFCE"),
    CLOUD("Cloud", "SSH")
}

data class Teammate(
    val id: String,
    val name: String,
    val shortName: String,
    val roleTitle: String,
    val roleFilter: String, // "Coordinator", "Engineer", "Researcher", "Writer"
    val avatarTag: String,  // "Orb", "Voxel", "Plush"
    val avatarUrl: String,
    val status: AgentStatus,
    val provider: String,
    val modelId: String,
    val modelShort: String,
    val effortLevel: String, // "Low", "Medium", "High"
    val computeTarget: ComputeTarget,
    val systemPrompt: String,
    val skills: List<String>,
    val connectedApps: List<String>,
    val allowShell: Boolean,
    val allowWriteFiles: Boolean,
    val allowToolSetup: Boolean,
    val allowProactive: Boolean,
    val memoryFactsCount: Int,
    val memorySummary: String
)

data class ConversationThread(
    val id: String,
    val title: String,
    val category: ThreadCategory,
    val preview: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null,
    val iconType: String? = null, // "voice", "inbox"
    val targetRoute: String
)

data class PendingApproval(
    val id: String = "gate_push_fcm",
    val agentId: String = "kai",
    val agentName: String = "Kai",
    val actionTitle: String = "Kai: push feat/mobile-fcm",
    val detailSummary: String = "3 commits • 18 tests passed",
    val branch: String = "feat/mobile-fcm",
    val commitsLabel: String = "3 verified",
    val diffStats: String = "+142 / -28",
    val isApproved: Boolean = false
)

data class GroupMessage(
    val id: String,
    val topic: String,
    val isUser: Boolean,
    val senderName: String,
    val senderRole: String = "",
    val avatarUrl: String = "",
    val timestamp: String,
    val body: String,
    val reasoningPill: String? = null,
    val reasoningDetails: String? = null,
    val hasApprovalGate: Boolean = false,
    val artifactTitle: String? = null,
    val artifactMeta: String? = null
)

data class PrivateMessage(
    val id: String,
    val agentId: String,
    val isUser: Boolean,
    val senderName: String,
    val avatarUrl: String,
    val timestamp: String,
    val body: String,
    val hasDigestWidget: Boolean = false,
    val hasHandoffCard: Boolean = false
)

data class SharedInboxMessage(
    val id: String,
    val isExternalGuest: Boolean,
    val isUser: Boolean,
    val senderName: String,
    val initials: String? = null,
    val badge: String? = null,
    val avatarUrl: String? = null,
    val timestamp: String,
    val body: String
)

data class CodingRunSession(
    val id: String,
    val title: String,
    val repo: String,
    val branch: String,
    val statusText: String,
    val isRunning: Boolean
)

data class CodingRunMessage(
    val id: String,
    val isUser: Boolean,
    val timestamp: String,
    val body: String,
    val modelTag: String = "claude-3-7",
    val costTag: String = "$0.42",
    val reasoningSummary: String? = null,
    val reasoningDetails: String? = null,
    val hasLiveBrowser: Boolean = false,
    val browserUrl: String = "localhost:3000/login",
    val browserStatus: String = "Testing OAuth callback...",
    val evidenceCommand: String? = null,
    val evidencePassed: Boolean = true
)

data class GovernanceRule(
    val id: String,
    val pattern: String,
    val description: String,
    val enabled: Boolean
)

data class AuditLogEntry(
    val id: String,
    val title: String,
    val actorAndTime: String,
    val isGate: Boolean = false,
    val isRevoked: Boolean = false
)

data class SharedFolderEntry(
    val id: String,
    val path: String,
    val accessSummary: String
)

data class IntegrationApp(
    val id: String,
    val name: String,
    val category: String, // "OAuth Apps", "Mailboxes", "Gateways"
    val iconKey: String,
    val subtitle: String,
    val isConnected: Boolean
)

data class GatewayItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val badgeOrAction: String,
    val isLiveBadge: Boolean
)
