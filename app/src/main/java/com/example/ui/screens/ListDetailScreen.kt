package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.model.Task
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.animateEnter
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListDetailScreen(
    listName: String,
    teamName: String,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onStatusClick: (Task) -> Unit = {},
    viewModel: VioraTaskViewModel,
    backHandlerEnabled: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("All Tasks", "To-Do", "In Progress", "Done")
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    
    val tasks by viewModel.allTasks.collectAsState()
    val nextTask by viewModel.nextTask.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()

    val allLists by viewModel.lists.collectAsState()
    val personalListId by viewModel.personalListId.collectAsState()
    val currentList = remember(allLists, listName) {
        allLists.find { it.name.equals(listName, ignoreCase = true) }
    }
    val isPersonalList = remember(currentList, listName, teamName, personalListId) {
        currentList?.isPersonal == true ||
        currentList?.id == personalListId ||
        listName.equals("Personal", ignoreCase = true) ||
        listName.equals("Personal Tasks", ignoreCase = true) ||
        listName.equals("My Tasks", ignoreCase = true)
    }

    var showEditSheet by remember { mutableStateOf(false) }
    var editListName by remember(listName) { mutableStateOf(listName) }
    var editListDescription by remember(currentList) { mutableStateOf(currentList?.description ?: "") }

    var activeDetailTask by remember { mutableStateOf<Task?>(null) }
    var lastActiveDetailTask by remember { mutableStateOf<Task?>(null) }
    if (activeDetailTask != null) {
        lastActiveDetailTask = activeDetailTask
    }
    
    val pagerState = rememberPagerState(initialPage = 0) { filters.size }
    val selectedFilter = filters[pagerState.currentPage]
    val coroutineScope = rememberCoroutineScope()
    
    val onFilterSelect = { filter: String ->
        val targetIndex = filters.indexOf(filter)
        if (targetIndex != -1 && targetIndex != pagerState.currentPage) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    val listTasks = remember(tasks, listName) {
        if (listName == "Done Tasks") {
            tasks.filter { it.status.name.equals("DONE", ignoreCase = true) }
        } else if (listName == "Quick Tasks") {
            tasks.filter { it.tags.any { tag -> tag.equals("Quick Task", ignoreCase = true) } }
        } else {
            val targetListId = viewModel.getListIdByName(listName) ?: listName.lowercase().replace(" ", "_")
            tasks.filter { it.folder == listName || it.listId == targetListId }
        }
    }
    val hasTasks = listTasks.isNotEmpty()

    val density = androidx.compose.ui.platform.LocalDensity.current
    val headerHeight = if (hasTasks) 140.dp else 80.dp
    val headerHeightPx = with(density) { headerHeight.toPx() }
    var headerOffsetHeightPx by remember { mutableStateOf(0f) }
    
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                return if (delta < 0) {
                    val newOffset = headerOffsetHeightPx + delta
                    val coerced = newOffset.coerceIn(-headerHeightPx, 0f)
                    val consumed = coerced - headerOffsetHeightPx
                    headerOffsetHeightPx = coerced
                    androidx.compose.ui.geometry.Offset(x = 0f, y = consumed)
                } else {
                    androidx.compose.ui.geometry.Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                return if (delta > 0) {
                    val newOffset = headerOffsetHeightPx + delta
                    val coerced = newOffset.coerceIn(-headerHeightPx, 0f)
                    val consumedAmount = coerced - headerOffsetHeightPx
                    headerOffsetHeightPx = coerced
                    androidx.compose.ui.geometry.Offset(x = 0f, y = consumedAmount)
                } else {
                    androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }
    }

    val isScrolled by remember {
        derivedStateOf { headerOffsetHeightPx < -10f }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            if (activeDetailTask != null) {
                activeDetailTask = null
            } else if (showDeleteConfirm || showArchiveConfirm) {
                showDeleteConfirm = false
                showArchiveConfirm = false
            } else if (showOptionsMenu) {
                showOptionsMenu = false
            } else {
                onBack()
            }
        },
        properties = androidx.compose.material3.ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        ),
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
        containerColor = VioraBackground,
        dragHandle = null,
        scrimColor = Color.Transparent,
        
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        com.example.ui.utils.ConfigureBottomSheetWindow()
        var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
        val animatedBackProgress by animateFloatAsState(
            targetValue = predictiveBackProgress,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 700f
            ),
            label = "listDetailSpringBack"
        )
        val hasSubDialog = activeDetailTask != null || showDeleteConfirm || showArchiveConfirm || showOptionsMenu

        PredictiveBackHandler(enabled = backHandlerEnabled) { progressFlow ->
            if (hasSubDialog) {
                if (activeDetailTask != null) {
                    activeDetailTask = null
                } else if (showDeleteConfirm || showArchiveConfirm) {
                    showDeleteConfirm = false
                    showArchiveConfirm = false
                } else if (showOptionsMenu) {
                    showOptionsMenu = false
                }
            } else {
                try {
                    progressFlow.collect { backEvent ->
                        predictiveBackProgress = backEvent.progress * 0.75f
                    }
                    coroutineScope.launch {
                        sheetState.hide()
                        onBack()
                    }
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    predictiveBackProgress = 0f
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
        ) {
            Scaffold(
                containerColor = VioraBackground,
                floatingActionButton = {
                    if (activeDetailTask == null) {
                        FloatingActionButton(
                            onClick = {
                                val listId = viewModel.getListIdByName(listName) ?: listName.lowercase().replace(" ", "_")
                                val teamId = viewModel.getTeamIdByName(teamName) ?: teamName.lowercase().replace(" ", "_")
                                val newTask = Task(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = "",
                                    client = teamName,
                                    folder = listName,
                                    listId = listId,
                                    teamId = teamId,
                                    userId = "user1",
                                    daysLeft = 7,
                                    status = com.example.model.TaskStatus.TODO
                                )
                                activeDetailTask = newTask
                            },
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Task", modifier = Modifier.size(32.dp))
                        }
                    }
                },
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .imePadding()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // Top Bar
            com.example.ui.components.VioraTopAppBar(
                modifier = Modifier.animateEnter(delayMillis = 0),
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.example.ui.components.VioraHeaderIconButton(
                            icon = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Back",
                            onClick = { onBack() }
                        )
                        
                        if (isScrolled) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = listName,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = teamName.replaceFirstChar { it.uppercase() },
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (isScrolled) {
                        Box {
                            com.example.ui.components.VioraHeaderIconButton(
                                icon = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                onClick = { showOptionsMenu = true }
                            )
                            
                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme.copy(
                                    surface = Color.White,
                                    surfaceContainer = Color.White,
                                    surfaceContainerHigh = Color.White,
                                    surfaceContainerLow = Color.White,
                                    surfaceContainerLowest = Color.White,
                                    surfaceContainerHighest = Color.White,
                                    onSurface = Color.Black
                                )
                            ) {
                                DropdownMenu(
                                    expanded = showOptionsMenu,
                                    onDismissRequest = { showOptionsMenu = false },
                                    shape = RoundedCornerShape(24.dp),
                                    containerColor = Color.White,
                                    modifier = Modifier
                                        .padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.Edit,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Edit list",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = { showOptionsMenu = false },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.Archive,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Archive list",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showArchiveConfirm = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.Delete,
                                                    contentDescription = null,
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Delete list",
                                                    color = Color.Red,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showDeleteConfirm = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                if (!hasTasks) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight, bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1C1C1E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TaskAlt,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tasks yet",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the + button below to create your first task in $listName.",
                                color = Color(0xFFAAAAAA),
                                fontSize = 14.sp,
                                fontFamily = SFProDisplayFontFamily,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val filter = filters[page]
                        val filteredTasks = tasks.filter { task ->
                            val matchesSearch = task.title.contains(searchQuery, ignoreCase = true)
                            val matchesFilter = when (filter) {
                                "To-Do" -> task.status.name.equals("TODO", ignoreCase = true)
                                "In Progress" -> task.status.name.equals("IN_PROGRESS", ignoreCase = true)
                                "Done" -> task.status.name.equals("DONE", ignoreCase = true)
                                else -> true
                            }
                            val matchesList = if (listName == "Done Tasks") {
                                task.status.name.equals("DONE", ignoreCase = true)
                            } else if (listName == "Quick Tasks") {
                                task.tags.any { tag -> tag.equals("Quick Task", ignoreCase = true) }
                            } else {
                                task.folder == listName || task.listId == (viewModel.getListIdByName(listName) ?: listName.lowercase().replace(" ", "_"))
                            }
                            matchesSearch && matchesFilter && matchesList
                        }

                        val pageListState = rememberLazyListState()

                        LazyColumn(
                            state = pageListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = headerHeight + 64.dp, bottom = 80.dp)
                        ) {
                            itemsIndexed(filteredTasks, key = { _, it -> it.id }) { index, task ->
                                val staggerDelay = if (index < 8) index * 40 else 0
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp, vertical = 4.dp)
                                        .animateEnter(delayMillis = staggerDelay)
                                ) {
                                    TaskListItemCard(
                                        task = task,
                                        onStatusClick = { onStatusClick(task) },
                                        onCardClick = { activeDetailTask = task }
                                    )
                                }
                            }
                        }
                    }
                }

                // Collapsible Header (listName, teamName, Search Bar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .offset { IntOffset(0, if (hasTasks) headerOffsetHeightPx.roundToInt() else 0) }
                        .background(VioraBackground)
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = listName,
                                    color = Color.White,
                                    fontSize = 38.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-1).sp
                                )
                                if (listName.equals("My Tasks", ignoreCase = true) || isPersonalList) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = "My Tasks Pinned",
                                        tint = VioraNeonLime,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isPersonalList || teamName == "Personal Space" || teamName.isBlank()) "Personal Space" else teamName,
                                color = Color(0xFFAAAAAA),
                                fontSize = 16.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                        
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF2A2A2A), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White
                                )
                            }
                            
                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme.copy(
                                    surface = Color.White,
                                    surfaceContainer = Color.White,
                                    surfaceContainerHigh = Color.White,
                                    surfaceContainerLow = Color.White,
                                    surfaceContainerLowest = Color.White,
                                    surfaceContainerHighest = Color.White,
                                    onSurface = Color.Black
                                )
                            ) {
                                DropdownMenu(
                                    expanded = showOptionsMenu,
                                    onDismissRequest = { showOptionsMenu = false },
                                    shape = RoundedCornerShape(24.dp),
                                    containerColor = Color.White,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.Edit,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Edit list",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            editListName = listName
                                            editListDescription = currentList?.description ?: ""
                                            showEditSheet = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                    if (!isPersonalList) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Rounded.Archive,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        "Archive list",
                                                        color = Color.Black,
                                                        fontFamily = SFProDisplayFontFamily,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.width(132.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                showOptionsMenu = false
                                                showArchiveConfirm = true
                                            },
                                            modifier = Modifier.height(44.dp),
                                            contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Rounded.Delete,
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        "Delete list",
                                                        color = Color.Red,
                                                        fontFamily = SFProDisplayFontFamily,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.width(132.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                showOptionsMenu = false
                                                showDeleteConfirm = true
                                            },
                                            modifier = Modifier.height(44.dp),
                                            contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (hasTasks) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { 
                                Text("Search tasks", color = Color(0xFFAAAAAA), fontFamily = SFProDisplayFontFamily) 
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFFAAAAAA))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = VioraNeonLime
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true
                        )
                    }
                }

                if (hasTasks) {
                    // Sticky Tab Bar (Filters)
                    // Positioned at: headerHeight + headerOffsetHeightPx
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(0, (headerHeightPx + headerOffsetHeightPx).roundToInt()) }
                            .background(VioraBackground)
                            .padding(vertical = 4.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                IconButton(
                                    onClick = { /* Filter */ },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF2A2A2A), CircleShape)
                                ) {
                                    Icon(Icons.Rounded.FilterList, contentDescription = "Filter", tint = Color.White)
                                }
                            }
                            
                            items(filters) { filter ->
                                val isSelected = selectedFilter == filter
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (isSelected) Color(0xFFD3E3F1) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else Color(0xFF4A4A4A),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clickable { onFilterSelect(filter) }
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        if (showEditSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEditSheet = false },
                containerColor = Color(0xFF1C1C1E),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Text(
                        text = if (isPersonalList) "Edit Personal List" else "Edit List",
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPersonalList) "Update the name and description of your personal space list." else "Update list details.",
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = editListName,
                        onValueChange = { editListName = it },
                        label = { Text("List Name", fontFamily = SFProDisplayFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioraNeonLime,
                            unfocusedBorderColor = Color(0xFF3A3A3C),
                            focusedContainerColor = Color(0xFF2C2C2E),
                            unfocusedContainerColor = Color(0xFF2C2C2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = VioraNeonLime,
                            focusedLabelColor = VioraNeonLime,
                            unfocusedLabelColor = Color(0xFF8E8E93)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editListDescription,
                        onValueChange = { editListDescription = it },
                        label = { Text("Description (Optional)", fontFamily = SFProDisplayFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioraNeonLime,
                            unfocusedBorderColor = Color(0xFF3A3A3C),
                            focusedContainerColor = Color(0xFF2C2C2E),
                            unfocusedContainerColor = Color(0xFF2C2C2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = VioraNeonLime,
                            focusedLabelColor = VioraNeonLime,
                            unfocusedLabelColor = Color(0xFF8E8E93)
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (editListName.isNotBlank()) {
                                showEditSheet = false
                                if (isPersonalList) {
                                    viewModel.updatePersonalList(
                                        name = editListName,
                                        description = editListDescription.ifBlank { null }
                                    )
                                } else {
                                    val targetId = currentList?.id ?: viewModel.getListIdByName(listName) ?: listName
                                    viewModel.updateList(
                                        listId = targetId,
                                        name = editListName,
                                        description = editListDescription.ifBlank { null }
                                    )
                                }
                            }
                        },
                        enabled = editListName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF2C2C2E),
                            disabledContentColor = Color(0xFF5E6064)
                        )
                    ) {
                        Text(
                            text = "Save Changes",
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        if (showDeleteConfirm || showArchiveConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .statusBarsPadding()
            ) {
                // Top-Left Close Button
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp)
                        .size(48.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            showDeleteConfirm = false
                            showArchiveConfirm = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Content Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (showDeleteConfirm) "Are you sure\ndelete this list?" else "Are you sure\narchive this list?",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily,
                        lineHeight = 40.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = if (showDeleteConfirm) {
                            "By deleting this list, it will be permanently unavailable. If you think you may need it later, try archiving it."
                        } else {
                            "By archiving this list, it will be temporarily hidden. You can restore it from settings anytime."
                        },
                        color = Color.Gray,
                        fontSize = 15.sp,
                        fontFamily = SFProDisplayFontFamily,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Action Button
                    Button(
                        onClick = {
                            if (showDeleteConfirm) {
                                showDeleteConfirm = false
                                viewModel.deleteList(listName)
                                onBack()
                            } else {
                                showArchiveConfirm = false
                                val listId = viewModel.getListIdByName(listName) ?: listName.lowercase().replace(" ", "_")
                                viewModel.archiveList(listId)
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showDeleteConfirm) Color(0xFFFF453A) else VioraNeonLime,
                            contentColor = if (showDeleteConfirm) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = if (showDeleteConfirm) "Delete" else "Archive",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cancel Button
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = SFProDisplayFontFamily,
                        modifier = Modifier
                            .clickable {
                                showDeleteConfirm = false
                                showArchiveConfirm = false
                            }
                            .padding(12.dp)
                    )
                }
            }
        }

        // Beautiful Task Detail Immersive Full Screen Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = activeDetailTask != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = 320f, dampingRatio = 0.85f)
            ) + fadeIn(
                animationSpec = tween(280)
            ) + scaleIn(
                initialScale = 0.94f,
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
                    onFetchTaskDetails = { taskId -> viewModel.fetchTaskDetails(taskId) },
                    backHandlerEnabled = activeDetailTask != null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    }
    }
}
