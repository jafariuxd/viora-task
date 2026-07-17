package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CalendarEvent
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.model.WeatherInfo
import com.example.ui.theme.*
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VioraTaskViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val weatherInfo by viewModel.weatherInfo.collectAsState()
    val upcomingEvent by viewModel.upcomingEvent.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreToLoad by viewModel.hasMoreToLoad.collectAsState()
    val unplannedCount by viewModel.unplannedCount.collectAsState()
    val nextTask by viewModel.nextTask.collectAsState()
    val viewingTeam by viewModel.viewingTeam.collectAsState()
    val viewingList by viewModel.viewingList.collectAsState()

    // Status picker state
    var selectedTaskForStatus by remember { mutableStateOf<Task?>(null) }

    // Task Detail screen state
    var activeDetailTask by remember { mutableStateOf<Task?>(null) }
    
    val listState = rememberLazyListState()

    // Detect scroll end for Pagination
    val shouldLoadMore = remember(tasks.size, hasMoreToLoad) {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= tasks.size - 2 && hasMoreToLoad
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreTasks()
        }
    }

    val subsequentTasks = remember(tasks, nextTask?.id) {
        tasks.filter { it.id != nextTask?.id }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VioraBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Content Area (Scrollable Feed)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = currentTab, 
                    label = "tab_fade",
                    animationSpec = tween(300, easing = LinearOutSlowInEasing)
                ) { tab ->
                    when (tab) {
                        "home" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                HeaderSection(
                                    userName = "Mohammad",
                                    onSearchClick = onNavigateToSearch,
                                    onNotificationClick = onNavigateToNotifications,
                                    onProfileClick = onNavigateToProfile
                                )
                                 LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("home_feed_list"),
                                    contentPadding = PaddingValues(start = 2.dp, end = 2.dp, top = 0.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // 2. Weather & Time Card
                                item {
                                    WeatherTimeCard(
                                        weatherInfo = weatherInfo.copy(
                                            dateText = if (currentDate.isNotEmpty()) currentDate else weatherInfo.dateText
                                        ),
                                        timeString = if (currentTime.isNotEmpty()) currentTime else "22:13"
                                    )
                                }

                                // 3. Upcoming Event Card (Green)
                                item {
                                    UpcomingEventCard(
                                        event = upcomingEvent,
                                        onClick = { onNavigateToAgenda() }
                                    )
                                }

                                // 4. Next Task Card (White)
                                if (nextTask != null) {
                                    item {
                                        NextTaskCard(
                                            task = nextTask!!,
                                            onStatusClick = { selectedTaskForStatus = nextTask },
                                            onCardClick = {
                                                activeDetailTask = nextTask
                                            }
                                        )
                                    }
                                }

                                // 5. Unplanned Tasks Card (Yellow)
                                item {
                                    UnplannedTasksCard(
                                        count = unplannedCount,
                                        onClick = {
                                            Toast.makeText(context, "Reviewing unplanned tasks...", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                // 6. Subsequent Tasks Feed (sorted by closest deadline)
                                itemsIndexed(
                                    items = subsequentTasks,
                                    key = { _, task -> task.id }
                                ) { index, task ->
                                    TaskListItemCard(
                                        task = task,
                                        onStatusClick = { selectedTaskForStatus = task },
                                        onCardClick = {
                                            activeDetailTask = task
                                        }
                                    )
                                }

                                // 7. Loading more indicator at bottom of feed
                                if (isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = VioraCalendarCard,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                        "new_task" -> {
                            PlaceholderScreen(
                                title = "New Task Creation",
                                subtitle = "Viora Task allows you to set default deadlines at the list, team, and account level. Keep an eye out for this screen in the next update!",
                                icon = Icons.Rounded.AddTask,
                                onBackHome = { viewModel.selectTab("home") }
                            )
                        }
                        "teams" -> {
                            TeamsScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // Bottom Navigation Bar
            VioraBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { tab -> viewModel.selectTab(tab) },
                onNewTaskClick = {
                    activeDetailTask = Task(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "",
                        client = "Viora design",
                        daysLeft = 7,
                        status = TaskStatus.TODO
                    )
                }
            )
        }

        // Custom Status Selector Sheet/Overlay (to handle status changing cleanly)
        selectedTaskForStatus?.let { task ->
            StatusSelectionDrawer(
                task = task,
                onStatusSelected = { newStatus ->
                    viewModel.updateTaskStatus(task.id, newStatus)
                    selectedTaskForStatus = null
                    Toast.makeText(context, "Status updated to: ${newStatus.toString()}", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { selectedTaskForStatus = null }
            )
        }

        // Beautiful Task Detail Immersive Full Screen Overlay
        AnimatedVisibility(
            visible = activeDetailTask != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = 300f, dampingRatio = 0.82f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = 300f, dampingRatio = 0.82f)
            ) + fadeOut()
        ) {
            activeDetailTask?.let { task ->
                val latestTask = tasks.find { it.id == task.id } ?: (if (nextTask?.id == task.id) nextTask else null) ?: task
                TaskDetailScreen(
                    task = latestTask,
                    onClose = { activeDetailTask = null },
                    onStatusChange = { newStatus ->
                        viewModel.updateTaskStatus(task.id, newStatus)
                    },
                    onTaskSaved = { savedTask ->
                        viewModel.upsertTask(savedTask)
                    },
                    onTaskDeleted = { taskId ->
                        viewModel.deleteTask(taskId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Team Detail Screen Overlay
        AnimatedVisibility(
            visible = viewingTeam != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = 300f, dampingRatio = 0.82f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = 300f, dampingRatio = 0.82f)
            ) + fadeOut()
        ) {
            viewingTeam?.let { team ->
                TeamDetailScreen(
                    teamName = team,
                    onBack = { viewModel.viewTeamDetail(null) },
                    viewModel = viewModel
                )
            }
        }

        // List Detail Screen Overlay
        AnimatedVisibility(
            visible = viewingList != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = 300f, dampingRatio = 0.82f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = 300f, dampingRatio = 0.82f)
            ) + fadeOut()
        ) {
            viewingList?.let { list ->
                ListDetailScreen(
                    listName = list,
                    teamName = viewingTeam ?: "Viora Design", // Fallback or current team
                    onBack = { viewModel.viewListDetail(null) },
                    onTaskClick = { activeDetailTask = it },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(VioraBackground)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hi 👋 ",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$userName!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = "Welcome back",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onSearchClick() }
                    .testTag("search_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Notification button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onNotificationClick() }
                    .testTag("notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Profile picture
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onProfileClick() }
                    .testTag("profile_button")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_mohammad_1783672402325),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun WeatherTimeCard(
    weatherInfo: WeatherInfo,
    timeString: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            
            .testTag("weather_time_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VioraWeatherCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherInfo.dateText,
                    color = VioraDarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Location",
                        tint = VioraDarkText.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = weatherInfo.location,
                        color = VioraDarkText.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeString,
                    color = VioraDarkText,
                    style = MaterialTheme.typography.headlineLarge
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = "Weather Icon",
                        tint = Color(0xFFF7A21C),
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = weatherInfo.temperature,
                        color = VioraDarkText,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherInfo.lastRefreshText,
                    color = VioraDarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "UV Index : ${weatherInfo.uvIndex}",
                    color = VioraDarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun UpcomingEventCard(
    event: CalendarEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            
            .testTag("upcoming_event_card"),
        shape = RoundedCornerShape(99.dp),
        colors = CardDefaults.cardColors(containerColor = VioraCalendarCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circular calendar date badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = event.day,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = event.month,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Column {
                    Text(
                        text = event.title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = event.time,
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 13.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = "Go to Calendar",
                    tint = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NextTaskCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            
            .testTag("next_task_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VioraTaskCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Time Left",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${task.daysLeft} days left",
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Business,
                        contentDescription = "Client",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.client,
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task Title (changes height automatically based on length, max 3 lines, with ellipsis)
            Text(
                text = task.title,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown status pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFD6E3FF))
                        .clickable { onStatusClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("status_dropdown_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = task.status.toString(),
                        color = Color(0xFF001B3E),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "Select status",
                        tint = Color(0xFF001B3E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Stack of Assignee Avatars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-10).dp) // overlapping effect
                ) {
                    // Profile/sara avatar overlap
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_mohammad_1783672402325),
                            contentDescription = "Assignee Mohammad",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_sara_1783672418392),
                            contentDescription = "Assignee Sara",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Plus badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F3F5))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+4",
                            color = VioraGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnplannedTasksCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            
            .testTag("unplanned_tasks_card"),
        shape = RoundedCornerShape(99.dp),
        colors = CardDefaults.cardColors(containerColor = VioraUnplannedCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Review ",
                    color = Color.Black.copy(alpha = 0.55f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
                Text(
                    text = "$count unplanned tasks",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }

            // Dashed circular border button with arrow pointing right
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.8f),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 8f),
                                    0f
                                )
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Review Unplanned",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TaskListItemCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            
            .testTag("task_list_item_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VioraTaskCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Time Left",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${task.daysLeft} days left",
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Business,
                        contentDescription = "Client",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.client,
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task Title
            Text(
                text = task.title,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown status pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFD6E3FF))
                        .clickable { onStatusClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("status_dropdown_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = task.status.toString(),
                        color = Color(0xFF001B3E),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "Select status",
                        tint = Color(0xFF001B3E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Team/Assignee indicators (overlapping)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-10).dp) // overlapping effect
                ) {
                    task.assigneePhotos.forEach { photo ->
                        val resId = if (photo == "sara") {
                            R.drawable.img_avatar_sara_1783672418392
                        } else {
                            R.drawable.img_profile_mohammad_1783672402325
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Assignee $photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    // Add dynamic badge if there are extra/mock team members
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F3F5))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+4",
                            color = VioraGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VioraBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onNewTaskClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("viora_bottom_navigation"),
        color = Color.Black,
        tonalElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Rounded.Home,
                    isSelected = selectedTab == "home",
                    onClick = { onTabSelected("home") }
                )
                BottomNavItem(
                    label = "New Task",
                    icon = Icons.Rounded.Add,
                    isSelected = false,
                    onClick = { onNewTaskClick() }
                )
                BottomNavItem(
                    label = "Teams",
                    icon = Icons.Rounded.Groups,
                    isSelected = selectedTab == "teams",
                    onClick = { onTabSelected("teams") }
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // active background pill
        val backgroundAlpha = if (isSelected) 0.15f else 0.0f
        val iconColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
        val pillBg = if (isSelected) VioraDarkPill else Color.Transparent

        Box(
            modifier = Modifier
                .width(64.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pillBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight(510),
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun StatusSelectionDrawer(
    task: Task,
    onStatusSelected: (TaskStatus) -> Unit,
    onDismiss: () -> Unit
) {
    // Custom slide-up bottom drawer overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() } // Dismiss on clicking background
            .testTag("status_selection_overlay"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Stop clicks propagating to parent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF1E1F22))
                .clickable(enabled = false) {}
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Update Status",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = task.title,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // List of options
                TaskStatus.values().forEach { status ->
                    val isSelected = task.status == status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable { onStatusSelected(status) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = status.toString(),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = VioraCalendarCard,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onBackHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VioraDarkPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = VioraCalendarCard,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(containerColor = VioraCalendarCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Back to Home",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
