package com.example.ui.screens

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Notification
import com.example.model.NotificationType
import com.example.ui.utils.animateEnter
import com.example.viewmodel.NotificationFilter
import com.example.viewmodel.NotificationsViewModel

private val VioraNeonLime = Color(0xFFB4FF00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    taskViewModel: com.example.viewmodel.VioraTaskViewModel? = null,
    onBack: () -> Unit,
    onNotificationClick: (Notification) -> Unit = {}
) {
    var selectedTeamInviteNotification by remember { mutableStateOf<Notification?>(null) }
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "notificationsSpringBack"
    )

    PredictiveBackHandler { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                predictiveBackProgress = backEvent.progress * 0.75f
            }
            onBack()
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            predictiveBackProgress = 0f
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val systemAlert by viewModel.systemAlert.collectAsStateWithLifecycle()

    // Show system alert modal if maintenance or update
    systemAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSystemAlert() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (alert.type == "maintenance_mode") Icons.Rounded.Warning else Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = VioraNeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = alert.title, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = alert.message,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSystemAlert() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioraNeonLime,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Got it", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(24.dp)
        )
    }

    val groupedNotifications = remember(notifications) {
        notifications.groupBy { it.timeGroup }
    }

    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refresh()
            taskViewModel?.refreshDashboard()
        },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color(0xFF1C1C1E),
                color = VioraNeonLime
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val progress = animatedBackProgress
                    if (progress > 0f) {
                        val scale = 1f - (progress * 0.12f)
                        scaleX = scale
                        scaleY = scale
                        translationY = progress * 70.dp.toPx()
                        alpha = 1f - (progress * 0.35f)
                        shape = RoundedCornerShape((progress * 32).dp)
                        clip = true
                    }
                }
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable(onClick = onBack)
                    .testTag("notifications_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Refresh button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { viewModel.refresh() }
                        .testTag("notifications_refresh_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isRefreshing) VioraNeonLime else Color.White
                    )
                }

                // Mark all read button
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(VioraNeonLime)
                            .clickable { viewModel.markAllAsRead() }
                            .padding(horizontal = 14.dp)
                            .testTag("notifications_mark_all_read_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = "Mark all read",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Mark all",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title and unread counter badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Notifications",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VioraNeonLime)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$unreadCount new",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotificationFilter.values().forEach { filter ->
                val isSelected = currentFilter == filter
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) VioraNeonLime else Color(0xFF1C1C1E),
                    label = "filterChipBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                    label = "filterChipText"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { viewModel.setFilter(filter) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("filter_${filter.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content / List
        if (isLoading && notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = VioraNeonLime,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
            }
        } else if (notifications.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "All caught up!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "No notifications in this filter right now.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            var globalIndex = 0
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("notifications_list"),
                contentPadding = PaddingValues(start = 7.dp, end = 7.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                groupedNotifications.forEach { (timeGroup, notifs) ->
                    item {
                        val idx = globalIndex++
                        Text(
                            text = timeGroup,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .animateEnter(delayMillis = idx * 30)
                                .padding(
                                    start = 13.dp,
                                    bottom = 8.dp,
                                    top = if (timeGroup == "Today") 4.dp else 16.dp
                                )
                        )
                    }

                    itemsIndexed(notifs, key = { _, item -> item.id }) { _, notification ->
                        val idx = globalIndex++
                        Box(modifier = Modifier.animateEnter(delayMillis = idx * 30)) {
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification.id)
                                    if (notification.type == NotificationType.TEAM_INVITE) {
                                        selectedTeamInviteNotification = notification
                                    } else {
                                        onNotificationClick(notification)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Interactive Team Invite BottomSheet / Dialog when an invite notification is tapped
        selectedTeamInviteNotification?.let { notif ->
            val teamId = notif.teamId ?: notif.data?.get("teamId") ?: ""
            val inviteId = notif.data?.get("inviteId") ?: notif.id
            val inviter = notif.data?.get("invitedBy") ?: notif.data?.get("inviter")
            val teamName = notif.data?.get("teamName") ?: notif.title.removePrefix("Invited to ").trim()

            com.example.ui.components.TeamInviteDialog(
                teamId = teamId,
                teamName = teamName,
                inviterName = inviter,
                onAccept = {
                    if (taskViewModel != null) {
                        taskViewModel.acceptTeamInvitation(inviteId) { success ->
                            if (success) {
                                viewModel.markAsRead(notif.id)
                            }
                        }
                    }
                    selectedTeamInviteNotification = null
                },
                onDecline = {
                    if (taskViewModel != null) {
                        taskViewModel.declineTeamInvitation(inviteId) { success ->
                            if (success) {
                                viewModel.markAsRead(notif.id)
                            }
                        }
                    }
                    selectedTeamInviteNotification = null
                },
                onDismiss = { selectedTeamInviteNotification = null }
            )
        }
    }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(if (notification.isRead) Color.White.copy(alpha = 0.88f) else Color.White)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, top = 14.dp, end = 16.dp, bottom = 14.dp)
            .testTag("notification_card_${notification.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Icon or Avatar
        val (iconVector, iconBgColor) = getNotificationIconAndBg(notification.type)

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            if (notification.avatarRes != null) {
                Image(
                    painter = painterResource(id = notification.avatarRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    fontSize = 17.sp,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (!notification.isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(VioraNeonLime)
                            .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = notification.description,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black.copy(alpha = 0.65f),
                lineHeight = 21.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getNotificationIconAndBg(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.TASK_ASSIGNED -> Pair(Icons.Rounded.Assignment, Color(0xFFE3F2FD)) // Soft blue
        NotificationType.TASK_DONE -> Pair(Icons.Rounded.CheckCircle, Color(0xFFD4EFA5)) // Lime/Green
        NotificationType.TEAM_INVITE -> Pair(Icons.Rounded.Group, Color(0xFFEDE7F6)) // Soft purple
        NotificationType.DEADLINE_FOCUS_TODAY -> Pair(Icons.Rounded.Alarm, Color(0xFFD4EFA5)) // Lime
        NotificationType.DEADLINE_OVERDUE -> Pair(Icons.Rounded.AccessTimeFilled, Color(0xFFFFE0D6)) // Soft coral
        NotificationType.REMINDER -> Pair(Icons.Rounded.Alarm, Color(0xFFD4EFA5))
        NotificationType.DEADLINE -> Pair(Icons.Rounded.AccessTimeFilled, Color(0xFFFFE0D6))
        NotificationType.SOFT_UPDATE -> Pair(Icons.Rounded.SystemUpdate, Color(0xFFE0F7FA))
        NotificationType.MAINTENANCE_MODE -> Pair(Icons.Rounded.Warning, Color(0xFFFFF3E0))
        NotificationType.CONNECTION -> Pair(Icons.Rounded.Group, Color(0xFFEDE7F6))
        NotificationType.OTHER -> Pair(Icons.Rounded.Assignment, Color(0xFFF5F5F5))
    }
}
