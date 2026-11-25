package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.model.AgentStatus
import com.example.ui.theme.GituMonoSm
import com.example.ui.theme.LocalGituColors

@Composable
fun AgentAvatar(
    imageUrl: String,
    fallbackInitials: String,
    contentDescription: String,
    size: Dp = 44.dp,
    ringColor: Color? = null,
    status: AgentStatus? = null,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    val outerSize = if (ringColor != null) size + 8.dp else size

    Box(
        modifier = modifier.size(outerSize),
        contentAlignment = Alignment.Center
    ) {
        val ringModifier = if (ringColor != null) {
            Modifier
                .size(outerSize)
                .clip(CircleShape)
                .border(2.dp, ringColor, CircleShape)
                .padding(4.dp)
        } else {
            Modifier.size(size)
        }

        Box(
            modifier = ringModifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fallbackInitials.take(2).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fallbackInitials.take(2).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                )
            } else {
                Text(
                    text = fallbackInitials.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (status != null) {
            val dotColor = when (status) {
                AgentStatus.ONLINE -> gituColors.success
                AgentStatus.RUNNING -> MaterialTheme.colorScheme.primary
                AgentStatus.NEEDS_APPROVAL -> MaterialTheme.colorScheme.tertiary
                AgentStatus.SLEEPING -> MaterialTheme.colorScheme.outline
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun GituCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String,
    isPrimary: Boolean = false,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPrimary) MaterialTheme.colorScheme.primary else LocalGituColors.current.surfaceContainerLow
    val tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun GituBackButton(
    onClick: () -> Unit,
    testTag: String = "back_button",
    modifier: Modifier = Modifier
) {
    GituCircleIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        onClick = onClick,
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun GituBadgePill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    isMono: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = if (isMono) GituMonoSm else MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun GituSurfaceCard(
    modifier: Modifier = Modifier,
    containerColor: Color = LocalGituColors.current.surfaceContainerLowest,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderWidth: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = containerColor,
        shape = shape,
        border = BorderStroke(borderWidth, borderColor),
        modifier = if (onClick != null) modifier.clip(shape).clickable(onClick = onClick) else modifier
    ) {
        Column(content = content)
    }
}

data class GituBottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val PrimaryGituTabs = listOf(
    GituBottomTab("cowork_hub", "Cowork", Icons.Filled.Forum, Icons.Outlined.Forum),
    GituBottomTab("coding_mode", "Coding", Icons.Filled.Code, Icons.Outlined.Code),
    GituBottomTab("remote_desktop", "Desktop", Icons.Filled.Devices, Icons.Outlined.Devices),
    GituBottomTab("settings", "Settings", Icons.Filled.Tune, Icons.Outlined.Tune)
)

@Composable
fun GituBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    Surface(
        color = gituColors.surfaceContainerLowest,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PrimaryGituTabs.forEach { tab ->
                    val selected = currentRoute == tab.route ||
                        (tab.route == "cowork_hub" && currentRoute == "teammates_roster")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigate(tab.route) }
                            .padding(vertical = 4.dp)
                            .testTag("nav_tab_${tab.label.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent
                                )
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GituNavRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gituColors = LocalGituColors.current
    NavigationRail(
        containerColor = gituColors.surfaceContainerLowest,
        modifier = modifier
    ) {
        Spacer(Modifier.height(16.dp))
        PrimaryGituTabs.forEach { tab ->
            val selected = currentRoute == tab.route ||
                (tab.route == "cowork_hub" && currentRoute == "teammates_roster")
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("rail_tab_${tab.label.lowercase()}")
            )
        }
    }
}
