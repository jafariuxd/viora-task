package com.example.ui.screens
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState

import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.drawscope.rotate

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import com.example.R
import com.example.ui.components.CreateTeamBottomSheet
import com.example.model.CalendarEvent
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.model.WeatherInfo
import com.example.ui.theme.*
import com.example.ui.utils.animateEnter
import com.example.ui.utils.shimmerEffect
import com.example.viewmodel.VioraTaskViewModel

object DiscoverCache {
    var dailyInsight: DailyInsight? = null
    var suggestedArticles: List<SuggestedArticle>? = null
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onVioraPassClick: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    viewModel: VioraTaskViewModel,
    agendaViewModel: com.example.viewmodel.AgendaViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToList: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val weatherInfo by viewModel.weatherInfo.collectAsState()
    val upcomingEvent by viewModel.upcomingEvent.collectAsState()
    val agendaEvents by agendaViewModel.events.collectAsState()
    val isAgendaAuthorized by agendaViewModel.isAuthorized.collectAsState()
    val isAgendaLoading by agendaViewModel.isLoading.collectAsState()
    val isDashboardLoading by viewModel.isDashboardLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val firstUpcomingAgendaEvent = agendaEvents.firstOrNull { !it.isPast }
    val tasks by viewModel.tasks.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreToLoad by viewModel.hasMoreToLoad.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val nextTask by viewModel.nextTask.collectAsState()
    val viewingTeam by viewModel.viewingTeam.collectAsState()
    val viewingList by viewModel.viewingList.collectAsState()
    val allLists by viewModel.lists.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserHandle by viewModel.userHandle.collectAsState()
    val currentUserAvatarUri by viewModel.userAvatarUri.collectAsState()

    val authIntent by agendaViewModel.authIntent.collectAsState()
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            agendaViewModel.handleAuthorizationResult(context, result.data)
        } else {
            agendaViewModel.setError("Google Sign-In failed or was canceled. (Code: ${result.resultCode})")
        }
    }

    LaunchedEffect(authIntent) {
        if (authIntent != null) {
            launcher.launch(authIntent!!)
            agendaViewModel.clearAuthIntent()
        }
    }

    // Status picker state
    var selectedTaskForStatus by remember { mutableStateOf<Task?>(null) }

    // Task Detail screen state
    var activeDetailTask by remember { mutableStateOf<Task?>(null) }

    val quickAddSignal by viewModel.quickAddSignal.collectAsState()
    LaunchedEffect(quickAddSignal) {
        if (quickAddSignal) {
            activeDetailTask = Task(
                id = java.util.UUID.randomUUID().toString(),
                title = "",
                client = "Viora design",
                userId = "user1",
                daysLeft = 7,
                status = TaskStatus.TODO,
                tags = emptyList()
            )
            viewModel.consumeQuickAdd()
        }
    }

    // Focus mode state
    var focusedTask by remember { mutableStateOf<Task?>(null) }
    var focusedQuote by remember { mutableStateOf<DailyInsight?>(null) }

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "homeSpringBack"
    )

    val isAnyOverlayActive = activeDetailTask != null || selectedTaskForStatus != null || viewingList != null || viewingTeam != null || currentTab != "home" || focusedTask != null || focusedQuote != null
    PredictiveBackHandler(enabled = isAnyOverlayActive) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                predictiveBackProgress = backEvent.progress * 0.75f
            }
            if (focusedTask != null) {
                focusedTask = null
            } else if (focusedQuote != null) {
                focusedQuote = null
            } else if (activeDetailTask != null) {
                activeDetailTask = null
            } else if (selectedTaskForStatus != null) {
                selectedTaskForStatus = null
            } else if (viewingList != null) {
                viewModel.viewListDetail(null)
            } else if (viewingTeam != null) {
                viewModel.viewTeamDetail(null)
            } else if (currentTab != "home") {
                viewModel.selectTab("home")
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            predictiveBackProgress = 0f
        }
    }
    
    val listState = rememberLazyListState()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        (context as? android.app.Activity)?.let {
            agendaViewModel.authorizeAndFetch(it, silent = true)
        }
    }

    // Detect scroll end for Pagination
    val shouldLoadMore = remember(hasMoreToLoad) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 4 && hasMoreToLoad
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreTasks()
        }
    }

    val subsequentTasks = remember(tasks, nextTask?.id) {
        tasks.filter { it.id != nextTask?.id && it.status != TaskStatus.DONE }
    }

    val wizardCompleted by viewModel.wizardCompleted.collectAsState()

    val onboardingCompleted = remember(wizardCompleted, teams, allLists, tasks) {
        if (wizardCompleted) return@remember true
        val userTeams = teams.filter { it != "All Lists" && it != "Personal" && it != "Personal Space" }
        val userLists = allLists.filter { it.name != "Unplanned Tasks" && it.id != "unplanned_tasks" }
        val hasUserTask = tasks.any { it.listId != null && it.listId != "unplanned_tasks" && it.folder != "Unplanned Tasks" }
        val isCompleted = (userTeams.isNotEmpty() && userLists.isNotEmpty() && hasUserTask) || tasks.isNotEmpty()
        if (isCompleted) {
            viewModel.markWizardCompleted()
        }
        isCompleted
    }

    var dailyInsight by remember { mutableStateOf(DiscoverCache.dailyInsight) }
    var suggestedArticles by remember { mutableStateOf(DiscoverCache.suggestedArticles) }

    LaunchedEffect(Unit) {
        if (DiscoverCache.dailyInsight == null) {
            kotlinx.coroutines.delay(1500)
            val randomQuote = com.example.ui.utils.Quotes.getRandom()
            DiscoverCache.dailyInsight = DailyInsight(randomQuote.first, randomQuote.second)
            dailyInsight = DiscoverCache.dailyInsight
        }
        
        if (DiscoverCache.suggestedArticles == null) {
            kotlinx.coroutines.delay(600)
            val fetchedArticles = fetchRssArticles()
            if (fetchedArticles.isNotEmpty()) {
                DiscoverCache.suggestedArticles = fetchedArticles.take(2)
            } else {
                val fallbackArticles = listOf(
                    SuggestedArticle(
                        title = "The 2-Minute Rule to Stop Procrastination", 
                        readTime = "4 min read", 
                        category = "Productivity", 
                        icon = Icons.Rounded.Article,
                        url = "https://jamesclear.com/how-to-stop-procrastinating"
                    ),
                    SuggestedArticle(
                        title = "Atomic Habits: How to Get 1% Better Every Day", 
                        readTime = "8 min read", 
                        category = "Habits", 
                        icon = Icons.Rounded.TrendingUp,
                        url = "https://jamesclear.com/continuous-improvement"
                    )
                )
                DiscoverCache.suggestedArticles = fallbackArticles
            }
            suggestedArticles = DiscoverCache.suggestedArticles
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VioraBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize().blur(if (focusedTask != null || focusedQuote != null) 16.dp else 0.dp)) {
            // Main Content Area (Scrollable Feed)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = currentTab, 
                    label = "tab_fade",
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) { tab ->
                    when (tab) {
                        "home" -> {
                            val pullState = rememberPullToRefreshState()
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    viewModel.refreshDashboard()
                                    (context as? android.app.Activity)?.let {
                                        agendaViewModel.authorizeAndFetch(it, silent = true)
                                    }
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
                                Column(modifier = Modifier.fillMaxSize()) {
                                    HeaderSection(
                                        userName = currentUserName,
                                        userHandle = currentUserHandle,
                                        avatarUri = currentUserAvatarUri,
                                        onVioraPassClick = onVioraPassClick,
                                        modifier = Modifier.animateEnter(delayMillis = 0),
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
                                    if (tasks.isNotEmpty()) {
                                    Box(modifier = Modifier.animateEnter(delayMillis = 50)) {
                                        WeatherTimeCard(
                                            weatherInfo = weatherInfo.copy(
                                                dateText = if (currentDate.isNotEmpty()) currentDate else weatherInfo.dateText
                                            ),
                                            timeString = if (currentTime.isNotEmpty()) currentTime else "22:13",
                                            isLoading = isDashboardLoading
                                        )
                                    }
                                }

                                }

                                // 3. Upcoming Event Card (Green)
                                item {
                                    if (tasks.isNotEmpty()) {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 100)) {
                                            if (isAgendaAuthorized) {
                                                val eventToDisplay = firstUpcomingAgendaEvent?.let {
                                                    val dateStr = it.originalDateTime
                                                    var monthStr = ""
                                                    try {
                                                        if (dateStr.length >= 10) {
                                                            val d = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(dateStr.substring(0, 10))
                                                            if (d != null) monthStr = java.text.SimpleDateFormat("MMM", java.util.Locale.US).format(d)
                                                        }
                                                    } catch(e: Exception) {}
                                                    com.example.model.CalendarEvent(day = it.day, month = monthStr, title = it.title, time = it.time)
                                                }
                                                UpcomingEventCard(
                                                    event = eventToDisplay,
                                                    onClick = { onNavigateToAgenda() },
                                                    isLoading = isDashboardLoading || isAgendaLoading
                                                )
                                            } else {
                                                ConnectCalendarCard(
                                                    onClick = {
                                                        (context as? android.app.Activity)?.let {
                                                            agendaViewModel.authorizeAndFetch(it, silent = false)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 4. Empty Tasks Call To Action Card (Gray)
                                if (!onboardingCompleted) {
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 150)) {
                                            EmptyTasksCtaCard(
                                                teams = teams,
                                                allLists = allLists,
                                                onTeamCreated = { teamName, days ->
                                                    viewModel.addTeam(teamName, days)
                                                },
                                                onListCreated = { teamName, listName, days ->
                                                    viewModel.addListToTeam(teamName, listName, days)
                                                },
                                                onCreateTaskClick = { list ->
                                                    activeDetailTask = Task(
                                                        id = java.util.UUID.randomUUID().toString(),
                                                        title = "",
                                                        client = "Viora design",
                                                        userId = "user1",
                                                        listId = list?.id,
                                                        teamId = list?.teamId,
                                                        folder = list?.name ?: "Unplanned Tasks",
                                                        daysLeft = 7,
                                                        status = TaskStatus.TODO
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                // 5. Next Task Card (White) or All Done State
                                if (nextTask != null) {
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 150)) {
                                            NextTaskCard(
                                                task = nextTask!!,
                                                onStatusClick = { selectedTaskForStatus = nextTask },
                                                onCardClick = {
                                                    activeDetailTask = nextTask
                                                },
                                                onLongPressStart = { focusedTask = nextTask }
                                            )
                                        }
                                    }
                                } else if (onboardingCompleted) {
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 150)) {
                                            AllTasksDoneCard(
                                                onCreateTaskClick = { 
                                                    activeDetailTask = Task(
                                                        id = "temp_${System.currentTimeMillis()}",
                                                        title = "",
                                                        client = "",
                                                        status = TaskStatus.TODO,
                                                        assigneePhotos = listOf()
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                // 5. Completed Tasks Card (Yellow)
                                if (completedCount > 0) {
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 200)) {
                                            CompletedTasksCard(
                                                count = completedCount,
                                                onClick = {
                                                    viewModel.viewListDetail("Done Tasks")
                                                }
                                            )
                                        }
                                    }
                                }

                                // 6. Subsequent Tasks Feed (sorted by closest deadline)
                                itemsIndexed(
                                    items = subsequentTasks,
                                    key = { _, task -> task.id }
                                ) { index, task ->
                                    val staggerDelay = if (index < 5) 250 + (index * 40) else 50
                                    Box(modifier = Modifier.animateEnter(delayMillis = staggerDelay)) {
                                        TaskListItemCard(
                                            task = task,
                                            onStatusClick = { selectedTaskForStatus = task },
                                            onCardClick = {
                                                activeDetailTask = task
                                            },
                                            onLongPressStart = { focusedTask = task }
                                        )
                                    }
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

                                // 8. Discover Section
                                if (!isLoadingMore) {
                                    item {
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            text = "Discover",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily,
                                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 16.dp)
                                        )
                                    }
                                    
                                    item {
                                        Box(modifier = Modifier.animateEnter()) {
                                            DailyInsightCard(
                                                insight = dailyInsight,
                                                onLongPressStart = {
                                                    dailyInsight?.let { focusedQuote = it }
                                                }
                                            )
                                        }
                                    }
                                    
                                    item {
                                        Box(modifier = Modifier.animateEnter()) {
                                            val context = LocalContext.current
                                            SuggestedArticlesSection(articles = suggestedArticles) { url ->
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                context.startActivity(intent)
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
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
                        userId = "user1",
                        daysLeft = 7,
                        status = TaskStatus.TODO
                    )
                }
            )
        }

    // Keep reference to the last non-null states to allow smooth exit animations
    var lastViewingTeam by remember { mutableStateOf<String?>(null) }
    if (viewingTeam != null) {
        lastViewingTeam = viewingTeam
    }

    var lastViewingList by remember { mutableStateOf<String?>(null) }
    var lastViewingListTeam by remember { mutableStateOf("Personal") }
    if (viewingList != null) {
        lastViewingList = viewingList
        if (viewingTeam != null) {
            lastViewingListTeam = viewingTeam!!
        }
    }

    var lastActiveDetailTask by remember { mutableStateOf<Task?>(null) }
    if (activeDetailTask != null) {
        lastActiveDetailTask = activeDetailTask
    }

    var lastFocusedTask by remember { mutableStateOf<Task?>(null) }
    if (focusedTask != null) {
        lastFocusedTask = focusedTask
    }
    var lastFocusedQuote by remember { mutableStateOf<DailyInsight?>(null) }
    if (focusedQuote != null) {
        lastFocusedQuote = focusedQuote
    }

    if (viewingTeam != null) {
        lastViewingTeam?.let { team ->
            TeamDetailScreen(
                teamName = team,
                onBack = { viewModel.viewTeamDetail(null) },
                viewModel = viewModel,
                backHandlerEnabled = viewingTeam != null
            )
        }
    }

    if (viewingList != null) {
        lastViewingList?.let { list ->
            ListDetailScreen(
                listName = list,
                teamName = lastViewingListTeam,
                onBack = { viewModel.viewListDetail(null) },
                onTaskClick = { activeDetailTask = it },
                onStatusClick = { selectedTaskForStatus = it },
                viewModel = viewModel,
                backHandlerEnabled = viewingList != null
            )
        }
    }

    // Beautiful Task Detail Immersive Full Screen Overlay
    AnimatedVisibility(
        visible = activeDetailTask != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = 650f, dampingRatio = 0.72f)
        ) + fadeIn(
            animationSpec = tween(180)
        ) + scaleIn(
            initialScale = 0.94f,
            animationSpec = spring(stiffness = 650f, dampingRatio = 0.72f)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = 650f, dampingRatio = 0.85f)
        ) + fadeOut(
            animationSpec = tween(160)
        ) + scaleOut(
            targetScale = 0.94f,
            animationSpec = spring(stiffness = 650f, dampingRatio = 0.85f)
        )
    ) {
        lastActiveDetailTask?.let { task ->
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
                lists = allLists,
                getTeamName = { teamId -> viewModel.getTeamNameById(teamId) },
                getTeamDefaultDeadline = { teamId -> viewModel.getTeamDefaultDeadline(teamId) },
                getUserDefaultDeadline = { viewModel.getUserDefaultDeadline() },
                getTeamMembers = { teamId -> viewModel.getTeamMembers(teamId) },
                getTeamMemberUsers = { teamId -> viewModel.getTeamMemberUsers(teamId) },
                currentUsername = currentUserName,
                backHandlerEnabled = activeDetailTask != null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }


        // Focus Mode Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = focusedTask != null || focusedQuote != null,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val activity = context as? android.app.Activity
            DisposableEffect(focusedTask != null || focusedQuote != null) {
                if (focusedTask != null || focusedQuote != null) {
                    activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusedTask = null
                            focusedQuote = null
                        }
                    )
            ) {


                // Centered Card
                if (focusedTask != null) {
                    lastFocusedTask?.let { task ->
                        val latestTask = tasks.find { it.id == task.id } ?: (if (nextTask?.id == task.id) nextTask else null) ?: task
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .graphicsLayer {
                                    val progress = animatedBackProgress
                                    val currentScale = 1.05f * (1f - (progress * 0.15f))
                                    scaleX = currentScale
                                    scaleY = currentScale
                                    alpha = 1f - (progress * 0.4f)
                                    shape = RoundedCornerShape((progress * 32).dp)
                                    clip = true
                                }
                        ) {
                            if (latestTask.id == nextTask?.id) {
                                NextTaskCard(
                                    task = latestTask,
                                    onStatusClick = { selectedTaskForStatus = latestTask },
                                    onCardClick = {
                                        activeDetailTask = latestTask
                                        focusedTask = null
                                    }
                                )
                            } else {
                                TaskListItemCard(
                                    task = latestTask,
                                    onStatusClick = { selectedTaskForStatus = latestTask },
                                    onCardClick = {
                                        activeDetailTask = latestTask
                                        focusedTask = null
                                    }
                                )
                            }
                        }
                    }
                } else if (focusedQuote != null) {
                    lastFocusedQuote?.let { quote ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .graphicsLayer {
                                    val progress = animatedBackProgress
                                    val currentScale = 1.05f * (1f - (progress * 0.15f))
                                    scaleX = currentScale
                                    scaleY = currentScale
                                    alpha = 1f - (progress * 0.4f)
                                    shape = RoundedCornerShape((progress * 32).dp)
                                    clip = true
                                }
                        ) {
                            DailyInsightCard(
                                insight = quote,
                                onLongPressStart = null
                            )
                        }
                    }
                }

                // Bottom hint to tap to close
                Text(
                    text = "Tap anywhere to close",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(com.example.R.font.sf_pro_display_medium)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                )
            }
        }

        // Custom Status Selector Sheet/Overlay (to handle status changing cleanly)
        selectedTaskForStatus?.let { task ->
            StatusSelectionDrawer(
                task = task,
                onStatusSelected = { newStatus ->
                    viewModel.updateTaskStatus(task.id, newStatus)
                    selectedTaskForStatus = null
                    
                    val statusMessage = when (newStatus) {
                        TaskStatus.DONE -> "Task completed"
                        TaskStatus.IN_PROGRESS -> "Moved to In Progress"
                        TaskStatus.TODO -> "Moved to To-Do"
                    }
                    Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
                },
                onDismiss = { selectedTaskForStatus = null }
            )
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    onVioraPassClick: () -> Unit = {},
    userHandle: String = userName,
    avatarUri: String? = null,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val greetingText = buildAnnotatedString {
        append("Hi 👋 ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("$userName!")
        }
    }
    com.example.ui.components.VioraTopAppBar(
        modifier = modifier.background(VioraBackground),
        contentPadding = PaddingValues(horizontal = 20.dp),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = greetingText,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Welcome back",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {

            com.example.ui.components.VioraHeaderIconButton(
                icon = Icons.Rounded.Search,

                contentDescription = "Search",
                onClick = onSearchClick,
                modifier = Modifier.testTag("search_button")
            )

            com.example.ui.components.VioraHeaderIconButton(
                icon = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                onClick = onNotificationClick,
                modifier = Modifier.testTag("notification_button")
            )

            com.example.ui.components.VioraHeaderCustomButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("profile_button")
            ) {
                com.example.ui.components.UserAvatar(
                    userId = userHandle.ifBlank { "User" },
                    avatarUri = avatarUri,
                    size = 48.dp
                )
            }
        }
    )
}

@Composable
fun WeatherTimeCard(
    weatherInfo: WeatherInfo,
    timeString: String,
    isLoading: Boolean = false
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
                    val weatherEmoji = when (weatherInfo.condition) {
                        "Sunny" -> "☀️"
                        "Cloudy" -> "☁️"
                        "Foggy" -> "🌫️"
                        "Rainy" -> "🌧️"
                        "Snowy" -> "❄️"
                        "Thunderstorm" -> "⛈️"
                        else -> "☀️"
                    }
                    Text(
                        text = weatherEmoji,
                        fontSize = 30.sp
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
fun ConnectCalendarCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("connect_calendar_card"),
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
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Event,
                        contentDescription = "Calendar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Connect Calendar",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Sync your agenda seamlessly",
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
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Connect",
                    tint = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun UpcomingEventCard(
    event: CalendarEvent?,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("upcoming_event_card"),
        shape = RoundedCornerShape(99.dp),
        colors = CardDefaults.cardColors(containerColor = VioraCalendarCard)
    ) {
        AnimatedContent(
            targetState = isLoading to (event == null),
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "event_anim"
        ) { (loading, isEmpty) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                // Shimmer layout
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.25f)).shimmerEffect())
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.25f)).shimmerEffect())
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .shimmerEffect()
                )
            } else if (event == null) {
                // Empty state layout
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clear Schedule",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Time is yours to command",
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
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
            } else {
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

                Spacer(modifier = Modifier.width(14.dp))

                // Text column is weighted so it expands to fill remaining space
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = event.time,
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right-side circle (CalendarToday icon)
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
    }
}

@Composable
fun NextTaskCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit,
    onLongPressStart: () -> Unit = {}
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .indication(interactionSource, androidx.compose.foundation.LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val release = tryAwaitRelease()
                        if (release) {
                            interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Cancel(press))
                        }
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onLongPressStart()
                    },
                    onTap = {
                        onCardClick()
                    }
                )
            }
            .testTag("next_task_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = VioraTaskCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isOverdue = task.daysLeft < 0 && task.status != com.example.model.TaskStatus.DONE
                val timeColor = if (isOverdue) Color.Red else VioraGrayText
                val timeText = when {
                    isOverdue -> "overdue"
                    task.daysLeft == 1 -> "1 day left"
                    else -> "${task.daysLeft} days left"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isOverdue) Icons.Rounded.EventBusy else Icons.Rounded.Schedule,
                        contentDescription = "Time Left",
                        tint = timeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timeText,
                        color = timeColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.List,
                        contentDescription = "List",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.folder,
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Task Title
            Text(
                text = task.title,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dropdown status pill
                    val statusPair = com.example.ui.theme.VioraColors.forStatus(task.status)
                    val statusText = when (task.status) {
                        TaskStatus.TODO -> "To-Do"
                        TaskStatus.IN_PROGRESS -> "In Progress"
                        TaskStatus.DONE -> "Done"
                    }
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(statusPair.container)
                            .clickable { onStatusClick() }
                            .padding(horizontal = 14.dp)
                            .testTag("status_dropdown_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusPair.content,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Change Status",
                            tint = statusPair.content,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Assignee avatars on the bottom-right
                if (task.assigneePhotos.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-10).dp) // overlapping effect
                    ) {
                        val maxAvatars = 3
                        val displayPhotos = task.assigneePhotos.take(maxAvatars)
                        
                        displayPhotos.forEach { photo ->
                            com.example.ui.components.UserAvatar(
                                userId = photo,
                                modifier = Modifier.border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                        
                        if (task.assigneePhotos.size > maxAvatars) {
                            val extra = task.assigneePhotos.size - maxAvatars
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F5))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$extra",
                                    color = VioraGrayText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, VioraGrayText, RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Unassigned",
                            tint = VioraGrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Unassigned",
                            color = VioraGrayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllTasksDoneCard(
    onCreateTaskClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("all_tasks_done_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26262A)),
        border = BorderStroke(1.dp, Color(0xFF38383E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF1E1E20), CircleShape)
                    .border(1.dp, Color(0xFF2E2E32), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = VioraNeonLime,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "You're all set.",
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Great work! Your list is empty. Take a break, or start planning what's next.",
                color = Color(0xFF98989D),
                fontSize = 16.sp,
                fontFamily = SFProDisplayFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            Button(
                onClick = onCreateTaskClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start a new task",
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedTasksCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable { onClick() }
            .testTag("unplanned_tasks_card"),
        shape = RoundedCornerShape(99.dp),
        colors = CardDefaults.cardColors(containerColor = VioraUnplannedCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Review",
                    color = Color.Black.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "$count completed tasks",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.Black,
                            radius = size.width / 2,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f
                                )
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Review",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TeamSpaceIllustration() {
    Box(
        modifier = Modifier
            .size(width = 180.dp, height = 110.dp),
        contentAlignment = Alignment.Center
    ) {
        // Orbit lines / dotted paths using Canvas or drawBehind
        Box(
            modifier = Modifier
                .size(90.dp)
                .drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f)
                        )
                    )
                }
        )
        // Orbit satellite 1
        Box(
            modifier = Modifier
                .offset(x = (-38).dp, y = (-25).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
        }
        // Orbit satellite 2
        Box(
            modifier = Modifier
                .offset(x = 42.dp, y = 20.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
        }
        // Central premium workspace hub (glow and double rings)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Group,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RoadmapIllustration() {
    Box(
        modifier = Modifier
            .size(width = 180.dp, height = 110.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First column (tilted slightly, secondary)
            Box(
                modifier = Modifier
                    .size(width = 65.dp, height = 75.dp)
                    .rotate(-4f)
                    .background(Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.width(30.dp).height(4.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(42.dp).height(3.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(20.dp).height(3.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(1.dp)))
                }
            }
            
            // Second column (active, highlighted with Neon Lime, adding list)
            Box(
                modifier = Modifier
                    .size(width = 75.dp, height = 85.dp)
                    .background(Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(12.dp))
                    .border(
                        width = 1.2.dp,
                        color = VioraNeonLime.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Header of active list
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VioraNeonLime))
                        Box(modifier = Modifier.width(35.dp).height(5.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                    }
                    
                    // List item placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    )
                    
                    // Second list item placeholder (pulsing or dotted)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .drawBehind {
                                drawRoundRect(
                                    color = VioraNeonLime.copy(alpha = 0.3f),
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()), 0f)
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDeploymentIllustration() {
    Box(
        modifier = Modifier
            .size(width = 180.dp, height = 110.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background card
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 65.dp)
                .offset(x = (-16).dp, y = (-10).dp)
                .rotate(-8f)
                .background(Color.White.copy(alpha = 0.02f), shape = RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
        )

        // Active highlighted Task Card
        Box(
            modifier = Modifier
                .size(width = 125.dp, height = 75.dp)
                .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = VioraNeonLime.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(5.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.weight(1f))

                // Checkbox row with Lime active indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(VioraNeonLime.copy(alpha = 0.15f))
                            .border(1.dp, VioraNeonLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = VioraNeonLime,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                            .background(VioraNeonLime.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // Floating glowing active Deploy action bubble
        Box(
            modifier = Modifier
                .offset(x = 54.dp, y = 28.dp)
                .size(34.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = VioraNeonLime, spotColor = VioraNeonLime)
                .background(VioraNeonLime, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    active: Boolean,
    completed: Boolean,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completed -> VioraNeonLime
                        active -> VioraNeonLime.copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.04f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = when {
                        completed -> VioraNeonLime
                        active -> VioraNeonLime
                        else -> Color.White.copy(alpha = 0.15f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (active) VioraNeonLime else Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = label,
            color = if (active || completed) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyTasksCtaCard(
    teams: List<String>,
    allLists: List<com.example.model.TaskList>,
    onTeamCreated: (String, Int?) -> Unit,
    onListCreated: (String, String, Int?) -> Unit,
    onCreateTaskClick: (com.example.model.TaskList?) -> Unit
) {
    val userTeams = remember(teams) { teams.filter { it != "All Lists" && it != "Personal" && it != "Personal Space" } }
    val userLists = remember(allLists) { allLists.filter { it.name != "Unplanned Tasks" && it.id != "unplanned_tasks" } }
    val step = when {
        userTeams.isEmpty() -> 1
        userLists.isEmpty() -> 2
        else -> 3
    }

    var showCreateTeamSheet by remember { mutableStateOf(false) }
    var showCreateListSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(28.dp)
            )
            .testTag("empty_tasks_cta_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121315))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Step indicator badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(stepNumber = 1, active = step == 1, completed = step > 1, label = "Team")
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                StepIndicator(stepNumber = 2, active = step == 2, completed = step > 2, label = "List")
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                StepIndicator(stepNumber = 3, active = step == 3, completed = step > 3, label = "Task")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Dynamic Custom Illustration per step
            Crossfade(targetState = step, label = "step_illustration") { currentStep ->
                when (currentStep) {
                    1 -> TeamSpaceIllustration()
                    2 -> RoadmapIllustration()
                    else -> TaskDeploymentIllustration()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Copy Writing (UX writing) based on step
            val title = when (step) {
                1 -> "Assemble Your Team"
                2 -> "Define Your List"
                else -> "Launch Your First Task"
            }

            val subtitle = when (step) {
                1 -> "Viora organizes your work into Teams. Create your first team to set the stage for success."
                2 -> {
                    val activeTeam = userTeams.firstOrNull() ?: "Team"
                    "Now that you have '$activeTeam', create a list to structure your goals and focus your energy."
                }
                else -> {
                    val activeList = userLists.firstOrNull()?.name ?: "List"
                    "Your team and list are ready. Add your first task to '$activeList' to build momentum."
                }
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SFProDisplayFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SFProDisplayFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Flow Forms
            when (step) {
                1 -> {
                    // Step 1: Open Team Bottom Sheet Button
                    Button(
                        onClick = { showCreateTeamSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("create_team_cta_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Group,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Assemble Team",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                }
                2 -> {
                    // Step 2: Open List Bottom Sheet Button
                    Button(
                        onClick = { showCreateListSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("create_list_cta_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Define List",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                }
                3 -> {
                    // Step 3: Create Task Button
                    Button(
                        onClick = {
                            val activeList = userLists.firstOrNull()
                            onCreateTaskClick(activeList)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("create_task_cta_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Launch Task",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets rendered conditionally
    if (showCreateTeamSheet) {
        CreateTeamBottomSheet(
            onDismiss = { showCreateTeamSheet = false },
            onCreate = { teamName, deadline ->
                val days = when (deadline) {
                    "Daily" -> 1
                    "Weekly" -> 7
                    "Monthly" -> 30
                    "Account Default", "Team Default" -> null
                    else -> 3
                }
                onTeamCreated(teamName, days)
                showCreateTeamSheet = false
            }
        )
    }

    if (showCreateListSheet) {
        CreateListBottomSheet(
            onDismiss = { showCreateListSheet = false },
            onCreate = { listName, deadline ->
                val days = when (deadline) {
                    "Daily" -> 1
                    "Weekly" -> 7
                    "Monthly" -> 30
                    "Account Default", "Team Default" -> null
                    else -> 3
                }
                val activeTeam = userTeams.firstOrNull() ?: "All Lists"
                onListCreated(activeTeam, listName, days)
                showCreateListSheet = false
            }
        )
    }
}

@Composable
fun TaskListItemCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit,
    isOffline: Boolean = false,
    testTag: String = "task_list_item_card_${task.id}",
    onLongPressStart: () -> Unit = {}
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .indication(interactionSource, androidx.compose.foundation.LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val release = tryAwaitRelease()
                        if (release) {
                            interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Cancel(press))
                        }
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onLongPressStart()
                    },
                    onTap = {
                        onCardClick()
                    }
                )
            }
            .testTag(testTag),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = VioraTaskCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isOverdue = task.daysLeft < 0 && task.status != com.example.model.TaskStatus.DONE
                    val timeColor = if (isOverdue) Color.Red else VioraGrayText
                    val timeText = when {
                        isOverdue -> "overdue"
                        task.daysLeft == 1 -> "1 day left"
                        else -> "${task.daysLeft} days left"
                    }
                    
                    Row(
                        modifier = Modifier.background(Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOverdue) Icons.Rounded.EventBusy else Icons.Rounded.Schedule,
                            contentDescription = "Time Left",
                            tint = timeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = timeText,
                            color = timeColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    if (task.deadlineSource == com.example.model.DeadlineSource.TEAM || 
                        task.deadlineSource == com.example.model.DeadlineSource.LIST) {
                        val sourceText = if (task.deadlineSource == com.example.model.DeadlineSource.TEAM) "Team Deadline" else "List Deadline"
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F3F5))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = VioraGrayText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = sourceText,
                                color = VioraGrayText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.List,
                        contentDescription = "List",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.folder,
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Task Title
            Text(
                text = task.title,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dropdown status pill
                    val statusPair = com.example.ui.theme.VioraColors.forStatus(task.status)
                    val statusText = when (task.status) {
                        TaskStatus.TODO -> "To-Do"
                        TaskStatus.IN_PROGRESS -> "In Progress"
                        TaskStatus.DONE -> "Done"
                    }
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(statusPair.container)
                            .clickable { onStatusClick() }
                            .padding(horizontal = 14.dp)
                            .testTag("status_dropdown_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusPair.content,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Select status",
                            tint = statusPair.content,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    if (isOffline) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = VioraGrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Team/Assignee indicators
                if (task.assigneePhotos.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, VioraGrayText, RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Unassigned",
                            tint = VioraGrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Unassigned",
                            color = VioraGrayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-10).dp) // overlapping effect
                    ) {
                        val maxAvatars = 3
                        val displayPhotos = task.assigneePhotos.take(maxAvatars)
                        
                        displayPhotos.forEach { photo ->
                            com.example.ui.components.UserAvatar(
                                userId = photo,
                                modifier = Modifier.border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                        
                        if (task.assigneePhotos.size > maxAvatars) {
                            val extra = task.assigneePhotos.size - maxAvatars
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F5))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$extra",
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
            letterSpacing = 0.5.sp
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
                    fontSize = 24.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = task.title,
                    color = Color(0xFFAAAAAA),
                    fontSize = 16.sp,
                    fontFamily = SFProDisplayFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    com.example.ui.components.StatusButton(
                        text = "To-Do",
                        isSelected = task.status == TaskStatus.TODO,
                        onClick = {
                            onStatusSelected(TaskStatus.TODO)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    com.example.ui.components.StatusButton(
                        text = "In Progress",
                        isSelected = task.status == TaskStatus.IN_PROGRESS,
                        onClick = {
                            onStatusSelected(TaskStatus.IN_PROGRESS)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    com.example.ui.components.StatusButton(
                        text = "Done",
                        isSelected = task.status == TaskStatus.DONE,
                        onClick = {
                            onStatusSelected(TaskStatus.DONE)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
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

// Discover Data Classes
data class DailyInsight(val quote: String, val author: String)
data class SuggestedArticle(val title: String, val readTime: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val url: String)

// Discover Composables
@Composable
fun DailyInsightCard(
    insight: DailyInsight?,
    onLongPressStart: (() -> Unit)? = null
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26262A)),
        border = BorderStroke(1.dp, Color(0xFF38383E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .indication(interactionSource, androidx.compose.foundation.LocalIndication.current)
            .then(
                if (onLongPressStart != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                                interactionSource.emit(press)
                                val release = tryAwaitRelease()
                                if (release) {
                                    interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                                } else {
                                    interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Cancel(press))
                                }
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onLongPressStart()
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Rounded.FormatQuote,
                contentDescription = null,
                tint = VioraNeonLime,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(
                targetState = insight,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "insight_anim"
            ) { currentInsight ->
            if (currentInsight == null) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            } else {
                Column {
                    Text(
                        text = currentInsight.quote,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = SFProDisplayFontFamily,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "— ${currentInsight.author}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }
            }
        }
    }
}

@Composable
fun SuggestedArticlesSection(articles: List<SuggestedArticle>?, onArticleClick: (String) -> Unit) {
    AnimatedContent(
        targetState = articles,
        transitionSpec = {
            (fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(400))) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "articles_anim"
    ) { currentArticles ->
    Column(modifier = Modifier.padding(bottom = 0.dp)) {
        if (currentArticles == null) {
            for (i in 0..1) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26262A)),
                    border = BorderStroke(1.dp, Color(0xFF38383E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (i < 1) 12.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth(0.9f).height(18.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        }
                    }
                }
            }
        } else {
            currentArticles.forEachIndexed { index, article ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF26262A)),
                border = BorderStroke(1.dp, Color(0xFF38383E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (index < currentArticles.size - 1) 12.dp else 0.dp)
                    .clickable { onArticleClick(article.url) }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = article.icon,
                            contentDescription = null,
                            tint = Color(0xFF2A2A2C),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = article.category,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SFProDisplayFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = article.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SFProDisplayFontFamily,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = article.readTime,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        }
    }
}
}

suspend fun fetchRssArticles(): List<SuggestedArticle> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val feeds = listOf(
        "https://zenhabits.net/feed/" to "Zen Habits",
        "https://fs.blog/feed/" to "Farnam Street"
    )
    val articles = mutableListOf<SuggestedArticle>()
    
    for ((feedUrl, sourceName) in feeds) {
        try {
            val url = java.net.URL(feedUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val inputStream = connection.inputStream
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            
            var eventType = parser.eventType
            var currentTitle = ""
            var currentLink = ""
            var isInsideItem = false
            
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    org.xmlpull.v1.XmlPullParser.START_TAG -> {
                        if (name.equals("item", ignoreCase = true)) {
                            isInsideItem = true
                            currentTitle = ""
                            currentLink = ""
                        } else if (isInsideItem) {
                            if (name.equals("title", ignoreCase = true)) {
                                currentTitle = parser.nextText()
                            } else if (name.equals("link", ignoreCase = true)) {
                                currentLink = parser.nextText()
                            }
                        }
                    }
                    org.xmlpull.v1.XmlPullParser.END_TAG -> {
                        if (name.equals("item", ignoreCase = true)) {
                            isInsideItem = false
                            if (currentTitle.isNotEmpty() && currentLink.isNotEmpty()) {
                                val icon = if (sourceName == "Zen Habits") androidx.compose.material.icons.Icons.Rounded.SelfImprovement else androidx.compose.material.icons.Icons.Rounded.Article
                                articles.add(
                                    SuggestedArticle(
                                        title = currentTitle,
                                        readTime = "5 min read",
                                        category = sourceName,
                                        icon = icon,
                                        url = currentLink
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    articles.shuffled()
}
