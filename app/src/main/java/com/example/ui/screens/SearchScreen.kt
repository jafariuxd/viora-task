package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.animateEnter
import com.example.viewmodel.VioraTaskViewModel

@Composable
fun SearchScreen(
    viewModel: VioraTaskViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    var selectedTaskForStatus by remember { mutableStateOf<Task?>(null) }
    var activeDetailTask by remember { mutableStateOf<Task?>(null) }

    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allLists by viewModel.lists.collectAsStateWithLifecycle()
    val nextTask by viewModel.nextTask.collectAsStateWithLifecycle()
    val currentUserName by viewModel.userName.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "searchSpringBack"
    )
    val isOverlayActive = activeDetailTask != null || selectedTaskForStatus != null

    PredictiveBackHandler(enabled = isOverlayActive || true) { progressFlow ->
        if (isOverlayActive) {
            if (activeDetailTask != null) {
                activeDetailTask = null
            } else if (selectedTaskForStatus != null) {
                selectedTaskForStatus = null
            }
        } else {
            try {
                progressFlow.collect { backEvent ->
                    predictiveBackProgress = backEvent.progress * 0.75f
                }
                onBack()
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                predictiveBackProgress = 0f
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filters = listOf("All", "To-Do", "In Progress", "Done")

    val filteredTasks = remember(allTasks, searchQuery, selectedFilter) {
        if (searchQuery.trim().length < 3) {
            emptyList()
        } else {
            allTasks.filter { task ->
                val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                        (task.folder?.contains(searchQuery, ignoreCase = true) == true) ||
                        (task.client?.contains(searchQuery, ignoreCase = true) == true) ||
                        task.tags.any { it.contains(searchQuery, ignoreCase = true) }

                val matchesFilter = when (selectedFilter) {
                    "To-Do" -> task.status == TaskStatus.TODO
                    "In Progress" -> task.status == TaskStatus.IN_PROGRESS
                    "Done" -> task.status == TaskStatus.DONE
                    else -> true
                }

                matchesSearch && matchesFilter
            }
        }
    }

    Box(
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
            .background(VioraBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Search Bar Header
            com.example.ui.components.VioraTopAppBar(
                navigationIcon = {
                    com.example.ui.components.VioraHeaderIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF2A2A2A))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = SFProDisplayFontFamily,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(VioraNeonLime),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search tasks...",
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 18.sp,
                                            fontFamily = SFProDisplayFontFamily
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFAAAAAA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        )

            // Filters Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isSelected) Color(0xFFD3E3F1) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFF4A4A4A),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.Black else Color.White,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Search Results
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.trim().length < 3) "Search across all tasks" else "No tasks found",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SFProDisplayFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.trim().length < 3) "Type at least 3 characters to search" else "Try changing your search terms or filters",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            fontFamily = SFProDisplayFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                        val staggerDelay = if (index < 10) index * 40 else 0
                        Box(
                            modifier = Modifier.animateEnter(delayMillis = staggerDelay)
                        ) {
                            TaskListItemCard(
                                task = task,
                                onStatusClick = { selectedTaskForStatus = task },
                                onCardClick = { activeDetailTask = task }
                            )
                        }
                    }
                }
            }
        }

        // Status Selection Drawer Overlay
        selectedTaskForStatus?.let { task ->
            StatusSelectionDrawer(
                task = task,
                onStatusSelected = { newStatus ->
                    viewModel.updateTaskStatus(task.id, newStatus)
                    selectedTaskForStatus = null
                },
                onDismiss = { selectedTaskForStatus = null }
            )
        }

        // Task Detail Screen Overlay
        var lastActiveDetailTask by remember { mutableStateOf<Task?>(null) }
        if (activeDetailTask != null) {
            lastActiveDetailTask = activeDetailTask
        }

        AnimatedVisibility(
            visible = activeDetailTask != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = 320f, dampingRatio = 0.85f)
            ) + fadeIn(
                animationSpec = tween(280)
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(stiffness = 320f, dampingRatio = 0.85f)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = 320f, dampingRatio = 0.95f)
            ) + fadeOut(
                animationSpec = tween(240)
            ) + scaleOut(
                targetScale = 0.94f,
                animationSpec = spring(stiffness = 320f, dampingRatio = 0.95f)
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            lastActiveDetailTask?.let { task ->
                val latestTask = allTasks.find { it.id == task.id }
                    ?: (if (nextTask?.id == task.id) nextTask else null)
                    ?: task
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
    }
}
