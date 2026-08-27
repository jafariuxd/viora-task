package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.utils.animateEnter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.ui.components.StatusButton
import com.example.ui.components.DetailBadge
import java.util.UUID
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.rounded.Notes

fun computeTaskDeadlineLocal(
    task: Task,
    lists: List<com.example.model.TaskList>,
    getTeamDefaultDeadline: (String) -> Int? = { null },
    getUserDefaultDeadline: () -> Int = { 5 }
): Task {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    val deadlineDate = Calendar.getInstance()
    var source = com.example.model.DeadlineSource.SPECIFIC
    
    if (task.selectedDeadlineMillis != null) {
        deadlineDate.timeInMillis = task.selectedDeadlineMillis
    } else if (task.computedDeadlineMillis != null) {
        deadlineDate.timeInMillis = task.computedDeadlineMillis
    } else {
        var deadlineDays = getUserDefaultDeadline()
        source = com.example.model.DeadlineSource.USER
        
        val listObj = lists.find { it.id == task.listId || it.name == task.listId || it.name == task.folder }
        val teamIdResolved = task.teamId ?: listObj?.teamId
        if (teamIdResolved != null && teamIdResolved.isNotEmpty()) {
            val teamDeadline = getTeamDefaultDeadline(teamIdResolved)
            if (teamDeadline != null) {
                deadlineDays = teamDeadline
                source = com.example.model.DeadlineSource.TEAM
            }
        }
        
        if (listObj != null && listObj.defaultDeadlineDays != null) {
            deadlineDays = listObj.defaultDeadlineDays
            source = com.example.model.DeadlineSource.LIST
        }
            
        deadlineDate.timeInMillis = task.createdAtMillis
        deadlineDate.add(Calendar.DAY_OF_YEAR, deadlineDays)
        deadlineDate.set(Calendar.HOUR_OF_DAY, 23)
        deadlineDate.set(Calendar.MINUTE, 59)
        deadlineDate.set(Calendar.SECOND, 59)
        deadlineDate.set(Calendar.MILLISECOND, 999)
    }
    
    val dayStartCal = (deadlineDate.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diff = dayStartCal.timeInMillis - today.timeInMillis
    val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
    
    val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
    val dueDateText = if (task.dueDateText.isNotEmpty()) task.dueDateText else dateFormat.format(deadlineDate.time)
    
    return task.copy(
        computedDeadlineMillis = deadlineDate.timeInMillis,
        daysLeft = daysLeft,
        dueDateText = dueDateText,
        deadlineSource = source
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    onClose: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onTaskSaved: (Task) -> Unit,
    onTaskDeleted: (String) -> Unit,
    lists: List<com.example.model.TaskList> = emptyList(),
    getTeamName: (String) -> String = { teamId -> if (teamId.isBlank()) "Personal" else "Personal" },
    getTeamDefaultDeadline: (String) -> Int? = { null },
    getUserDefaultDeadline: () -> Int = { 5 },
    getTeamMembers: (String?) -> List<String> = { emptyList() },
    getTeamMemberUsers: (String?) -> List<com.example.model.User> = { emptyList() },
    currentUsername: String = "User",
    backHandlerEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var isNewTask by remember(task.id) { mutableStateOf(task.title.isEmpty()) }
    var currentTask by remember(task.id) { mutableStateOf(computeTaskDeadlineLocal(task, lists, getTeamDefaultDeadline, getUserDefaultDeadline)) }
    
    // Focus tracking for saving new task
    var titleFocused by remember(task.id) { mutableStateOf(false) }
    var descriptionFocused by remember(task.id) { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val isAnyFieldFocused = titleFocused || descriptionFocused



    val titleAlpha by animateFloatAsState(
        targetValue = if (descriptionFocused) 0.6f else 1f,
        label = "titleAlpha"
    )
    val descriptionAlpha by animateFloatAsState(
        targetValue = if (titleFocused) 0.6f else 1f,
        label = "descriptionAlpha"
    )
    val otherAlpha by animateFloatAsState(
        targetValue = if (titleFocused || descriptionFocused) 0.6f else 1f,
        label = "otherAlpha"
    )

    LaunchedEffect(isNewTask) {
        if (isNewTask) {
            titleFocusRequester.requestFocus()
        }
    }

    // Dialog states
    var showMoreMenu by remember(task.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember(task.id) { mutableStateOf(false) }
    var showFolderPicker by remember(task.id) { mutableStateOf(false) }
    var showAssigneePicker by remember(task.id) { mutableStateOf(false) }
    var showTagInput by remember(task.id) { mutableStateOf(false) }
    var showDeadlineOptionsSheet by remember(task.id) { mutableStateOf(false) }
    var showCalendarPicker by remember(task.id) { mutableStateOf(false) }
    var showTimePickerSheet by remember(task.id) { mutableStateOf(false) }
    var pendingDateForTimePicker by remember(task.id) { mutableStateOf<Calendar?>(null) }

    val handleClose = {
        if (currentTask.title.isNotBlank()) {
            onTaskSaved(currentTask)
        }
        onClose()
    }

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "taskDetailSpringBack"
    )

    val hasSubDialog = showMoreMenu || showDeleteConfirm || showFolderPicker || showAssigneePicker || showTagInput || showDeadlineOptionsSheet || showCalendarPicker || showTimePickerSheet

    // Temporary input states
    var newTagText by remember { mutableStateOf("") }
    
    // Animation state
    var showSuccessAnim by remember { mutableStateOf(false) }
    
    LaunchedEffect(showSuccessAnim) {
        if (showSuccessAnim) {
            kotlinx.coroutines.delay(1200)
            showSuccessAnim = false
        }
    }

    LaunchedEffect(descriptionFocused) {
        if (descriptionFocused) {
            kotlinx.coroutines.delay(150)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val updateTask = { newTask: Task ->
        val computed = computeTaskDeadlineLocal(newTask, lists, getTeamDefaultDeadline, getUserDefaultDeadline)
        currentTask = computed
        if (!isNewTask) {
            onTaskSaved(computed)
        }
    }

    val performSkipTime: () -> Unit = {
        val cal = (pendingDateForTimePicker ?: Calendar.getInstance()).clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val isToday = pendingDateForTimePicker?.let {
            val today = Calendar.getInstance()
            it.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            it.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        } ?: true

        val dayPrefix = if (isToday) "Today" else "Tomorrow"
        val formattedDate = "$dayPrefix at 23:59"

        updateTask(currentTask.copy(
            selectedDeadlineMillis = cal.timeInMillis,
            dueDateText = formattedDate
        ))
        showTimePickerSheet = false
    }

    PredictiveBackHandler(enabled = backHandlerEnabled) { progressFlow ->
        if (hasSubDialog) {
            if (showMoreMenu) showMoreMenu = false
            else if (showDeleteConfirm) showDeleteConfirm = false
            else if (showFolderPicker) showFolderPicker = false
            else if (showAssigneePicker) showAssigneePicker = false
            else if (showTagInput) showTagInput = false
            else if (showDeadlineOptionsSheet) showDeadlineOptionsSheet = false
            else if (showCalendarPicker) showCalendarPicker = false
            else if (showTimePickerSheet) performSkipTime()
        } else {
            try {
                progressFlow.collect { backEvent ->
                    predictiveBackProgress = backEvent.progress * 0.75f
                }
                handleClose()
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                predictiveBackProgress = 0f
            }
        }
    }

    val updateTaskLocal = { newTask: Task ->
        val computed = computeTaskDeadlineLocal(newTask, lists, getTeamDefaultDeadline, getUserDefaultDeadline)
        currentTask = computed
    }

    Box(
        modifier = modifier
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
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }
                .padding(horizontal = 2.dp)
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            com.example.ui.components.VioraTopAppBar(
                modifier = Modifier
                    .animateEnter(delayMillis = 0)
                    .graphicsLayer { alpha = otherAlpha },
                navigationIcon = {
                    com.example.ui.components.VioraHeaderIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close",
                        onClick = {
                            if (isAnyFieldFocused) {
                                focusManager.clearFocus()
                            } else {
                                handleClose()
                            }
                        }
                    )
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                .clickable {
                                    if (isAnyFieldFocused) {
                                        focusManager.clearFocus()
                                    } else {
                                        focusManager.clearFocus()
                                        showFolderPicker = true
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.List,
                                contentDescription = "List",
                                tint = VioraNeonLime,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isNewTask && (currentTask.folder == "My Tasks" || currentTask.folder == "Unplanned Tasks")) "Unplanned Tasks" else currentTask.folder,
                                color = if (isNewTask && (currentTask.folder == "My Tasks" || currentTask.folder == "Unplanned Tasks")) VioraNeonLime else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = if (isNewTask && (currentTask.folder == "My Tasks" || currentTask.folder == "Unplanned Tasks")) VioraNeonLime else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        com.example.ui.components.VioraHeaderIconButton(
                            icon = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            onClick = {
                                if (isAnyFieldFocused) {
                                    focusManager.clearFocus()
                                } else {
                                    focusManager.clearFocus()
                                    showMoreMenu = true
                                }
                            }
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
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            shape = RoundedCornerShape(24.dp),
                            containerColor = Color.White,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Remove Task",
                                            color = Color.Red,
                                            fontFamily = SFProDisplayFontFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.width(132.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteConfirm = true
                                },
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                            )
                        }
                    }
                }
            }
        )

            Spacer(modifier = Modifier.height(8.dp))

            // Task Title Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .animateEnter(delayMillis = 50)
                    .graphicsLayer { alpha = titleAlpha }
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    BasicTextField(
                        value = currentTask.title,
                        onValueChange = { updateTaskLocal(currentTask.copy(title = it)) },
                        modifier = Modifier
                            .focusRequester(titleFocusRequester)
                            .fillMaxWidth()
                            .padding(bottom = if (titleFocused) 16.dp else 0.dp)
                            .align(Alignment.TopStart)
                            .onFocusChanged { focusState ->
                                if (titleFocused && !focusState.isFocused) {
                                    if (currentTask.title.isNotBlank()) {
                                        if (isNewTask) {
                                            isNewTask = false
                                        }
                                        onTaskSaved(currentTask)
                                    }
                                }
                                titleFocused = focusState.isFocused
                            },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.Black),
                        cursorBrush = SolidColor(Color.Black),
                        decorationBox = { innerTextField ->
                            if (currentTask.title.isEmpty()) {
                                Text(
                                    text = "Enter task title...",
                                    color = Color.Black.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (titleFocused) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(VioraNeonLime)
                                .align(Alignment.BottomStart)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Status Row
            Row(
                modifier = Modifier
                    .animateEnter(delayMillis = 100)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                StatusButton(
                    text = "To-Do",
                    isSelected = currentTask.status == TaskStatus.TODO,
                    onClick = {
                        if (isAnyFieldFocused) {
                            focusManager.clearFocus()
                        } else {
                            updateTask(currentTask.copy(status = TaskStatus.TODO))
                            if (!isNewTask) onStatusChange(TaskStatus.TODO)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    text = "In Progress",
                    isSelected = currentTask.status == TaskStatus.IN_PROGRESS,
                    onClick = {
                        if (isAnyFieldFocused) {
                            focusManager.clearFocus()
                        } else {
                            updateTask(currentTask.copy(status = TaskStatus.IN_PROGRESS))
                            if (!isNewTask) onStatusChange(TaskStatus.IN_PROGRESS)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    text = "Done",
                    isSelected = currentTask.status == TaskStatus.DONE,
                    onClick = {
                        if (isAnyFieldFocused) {
                            focusManager.clearFocus()
                        } else {
                            val wasDone = currentTask.status == TaskStatus.DONE
                            updateTask(currentTask.copy(status = TaskStatus.DONE))
                            if (!isNewTask) onStatusChange(TaskStatus.DONE)
                            if (!wasDone) showSuccessAnim = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Due Date & Assignees grid
            Row(
                modifier = Modifier
                    .animateEnter(delayMillis = 150)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Due Date Card
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(101.dp)
                        .clickable {
                            if (isAnyFieldFocused) {
                                focusManager.clearFocus()
                            } else {
                                focusManager.clearFocus()
                                showDeadlineOptionsSheet = true
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        DetailBadge(
                            text = "Due date",
                            icon = Icons.Rounded.Schedule,
                            containerColor = Color(0xFFFFEBEB),
                            textColor = Color(0xFF5C1C1C)
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = currentTask.dueDateText.ifEmpty { "Set date" },
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        if (currentTask.selectedDeadlineMillis == null) {
                            val deadlineLabel = when (currentTask.deadlineSource) {
                                com.example.model.DeadlineSource.LIST -> "List Deadline"
                                com.example.model.DeadlineSource.TEAM -> "Team Deadline"
                                else -> "Default Deadline"
                            }
                            Row(
                                modifier = Modifier
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(40.dp))
                                    .background(Color(0xFFEFF1F5))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.VioraGrayText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = deadlineLabel,
                                    color = com.example.ui.theme.VioraGrayText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                        } else {
                            Text(
                                text = "${currentTask.daysLeft} days left",
                                fontSize = 14.sp,
                                fontFamily = SFProDisplayFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = com.example.ui.theme.VioraGrayText
                            )
                        }
                    }
                }

                // Assignees Card
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(101.dp)
                        .clickable {
                            if (isAnyFieldFocused) {
                                focusManager.clearFocus()
                            } else {
                                focusManager.clearFocus()
                                showAssigneePicker = true
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        DetailBadge(
                            text = "Assignees",
                            icon = Icons.Rounded.Group,
                            containerColor = Color(0xFFEBF8D0),
                            textColor = Color(0xFF2E4D00)
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        if (currentTask.assigneePhotos.isEmpty()) {
                            Column {
                                Text(
                                    text = "Not Assigned",
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Row(
                                    modifier = Modifier.height(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PersonAdd,
                                        contentDescription = null,
                                        tint = Color(0xFF8CC63F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Add user",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF8CC63F)
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((-12).dp)
                            ) {
                                // Show avatars
                                currentTask.assigneePhotos.take(2).forEach { photoName ->
                                    com.example.ui.components.UserAvatar(
                                        userId = photoName,
                                        modifier = Modifier.border(2.dp, Color.White, CircleShape),
                                        size = 48.dp
                                    )
                                }
                                
                                // Plus Button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(VioraNeonLime)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentTask.assigneePhotos.size > 2) {
                                        Text(
                                            text = "+${currentTask.assigneePhotos.size - 2}",
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add assignee",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Tags Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .animateEnter(delayMillis = 200)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth()
                    .clickable {
                        if (isAnyFieldFocused) {
                            focusManager.clearFocus()
                        } else {
                            focusManager.clearFocus()
                            showTagInput = true
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            top = 12.dp,
                            bottom = 18.dp
                        )
                ) {
                    DetailBadge(
                        text = "Tags",
                        icon = Icons.Rounded.Tag,
                        containerColor = Color(0xFFEBF2FF),
                        textColor = Color(0xFF224075)
                    )
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    if (currentTask.tags.isEmpty()) {
                        Text(
                            text = "Enter first tag...",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            modifier = Modifier.clickable {
                                if (isAnyFieldFocused) {
                                    focusManager.clearFocus()
                                } else {
                                    focusManager.clearFocus()
                                    showTagInput = true
                                }
                            }
                        )
                    } else {
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            currentTask.tags.forEach { tag ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(Color(0xFFF2F4F7))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "# $tag",
                                        color = Color.Black,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove tag",
                                        tint = Color.Black.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable {
                                                if (isAnyFieldFocused) {
                                                    focusManager.clearFocus()
                                                } else {
                                                    updateTask(currentTask.copy(tags = currentTask.tags - tag))
                                                }
                                            }
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(VioraNeonLime)
                                    .clickable {
                                        if (isAnyFieldFocused) {
                                            focusManager.clearFocus()
                                        } else {
                                            focusManager.clearFocus()
                                            showTagInput = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add tag",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Description Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .animateEnter(delayMillis = 250)
                    .graphicsLayer { alpha = descriptionAlpha }
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 103.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 18.dp)
                ) {
                    DetailBadge(
                        text = "Description",
                        icon = Icons.AutoMirrored.Rounded.Notes,
                        containerColor = Color(0xFFFFF4D1),
                        textColor = Color(0xFF6B5300)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    BasicTextField(
                        value = currentTask.description,
                        onValueChange = { updateTaskLocal(currentTask.copy(description = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = if (descriptionFocused) 60.dp else 40.dp)
                            .onFocusChanged { focusState ->
                                if (descriptionFocused && !focusState.isFocused) {
                                    if (currentTask.title.isNotBlank()) {
                                        onTaskSaved(currentTask)
                                    }
                                }
                                descriptionFocused = focusState.isFocused
                            },
                        cursorBrush = SolidColor(Color.Black),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontFamily = SFProDisplayFontFamily,
                            fontSize = 18.sp,
                            lineHeight = 24.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (currentTask.description.isEmpty()) {
                                Text(
                                    text = "Write task description...",
                                    color = Color.Gray,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontSize = 18.sp,
                                    lineHeight = 24.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF2A2A2A),
            title = {
                Text(
                    text = "Are you sure about this?",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "By deleting this task, it will be permanently unavailable. If you think you may need it later, try archiving it.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {},
            dismissButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            onClose()
                            onTaskDeleted(currentTask.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Delete", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        )
    }

    // Assignee Picker Dialog (global BottomSheet)
    com.example.ui.components.AssigneePickerBottomSheet(
        visible = showAssigneePicker,
        onDismiss = { showAssigneePicker = false },
        initialAssignees = currentTask.assigneePhotos,
        allTeamMembers = getTeamMemberUsers(currentTask.teamId),
        currentUsername = currentUsername,
        onSubmit = { newAssignees ->
            updateTask(currentTask.copy(assigneePhotos = newAssignees))
        }
    )

    // Folder Picker Dialog (simplified)

        // Success Overlay
        if (showSuccessAnim) {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showSuccessAnim,
                    enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + androidx.compose.animation.scaleIn(androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing), initialScale = 0.5f),
                    exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) + androidx.compose.animation.scaleOut(androidx.compose.animation.core.tween(300), targetScale = 1.2f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(VioraNeonLime.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, VioraNeonLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Success",
                            tint = VioraNeonLime,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }

    if (showFolderPicker) {
        val folderSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            sheetState = folderSheetState,
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = folderSheetState, defaultRadius = 28.dp),
            onDismissRequest = { showFolderPicker = false },
            containerColor = Color(0xFF2A2A2A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF383838))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search lists/teams", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                val listsNames = remember(lists) { lists.map { it.name } }
                if (listsNames.isEmpty()) {
                    Text(
                        text = "No lists available. Please create a list under a team first.",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 24.dp),
                        fontFamily = SFProDisplayFontFamily
                    )
                } else {
                    listsNames.forEach { listName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val parentList = lists.find { it.name == listName }
                                    updateTask(currentTask.copy(
                                        folder = listName,
                                        listId = parentList?.id ?: listName,
                                        teamId = parentList?.teamId ?: currentTask.teamId
                                    ))
                                    showFolderPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD54F)), // arbitrary
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(listName.first().toString(), color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(listName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                    val parentList = lists.find { it.name == listName }
                                    val parentTeamName = parentList?.let { listObj ->
                                        if (listObj.teamId.isNotBlank()) getTeamName(listObj.teamId) else "Personal"
                                    } ?: "Personal"
                                    Text(parentTeamName, color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                            
                            // Radio button
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (currentTask.folder == listName) VioraNeonLime else Color.Gray, CircleShape)
                                    .padding(4.dp)
                            ) {
                                if (currentTask.folder == listName) {
                                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(VioraNeonLime))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Due date picker (Options)
    if (showDeadlineOptionsSheet) {
        val deadlineSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            sheetState = deadlineSheetState,
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = deadlineSheetState, defaultRadius = 28.dp),
            onDismissRequest = { showDeadlineOptionsSheet = false },
            containerColor = Color(0xFF383838),
            dragHandle = null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Option: Today at ...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDeadlineOptionsSheet = false
                            pendingDateForTimePicker = Calendar.getInstance()
                            showTimePickerSheet = true
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("0d", color = VioraNeonLime, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
                    Text("Today at ...", color = Color.White, fontSize = 20.sp)
                }
                Divider(color = Color.White.copy(alpha = 0.1f))

                // Option: Tomorrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDeadlineOptionsSheet = false
                            pendingDateForTimePicker = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            showTimePickerSheet = true
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1d", color = VioraNeonLime, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
                    Text("Tomorrow", color = Color.White, fontSize = 20.sp)
                }
                Divider(color = Color.White.copy(alpha = 0.1f))

                // Direct options (2d, 1w, 2w)
                val options = listOf(
                    Triple("Next 2 days", "2d", 2),
                    Triple("Next week", "1w", 7),
                    Triple("Next 2 weeks", "2w", 14)
                )
                
                options.forEach { (text, badge, days) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, days)
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 0)
                                }
                                val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                                val formattedDate = dateFormat.format(calendar.time)
                                updateTask(currentTask.copy(selectedDeadlineMillis = calendar.timeInMillis, dueDateText = formattedDate))
                                showDeadlineOptionsSheet = false
                            }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(badge, color = VioraNeonLime, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
                        Text(text, color = Color.White, fontSize = 20.sp)
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f))
                }
                
                // Choose date...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDeadlineOptionsSheet = false
                            showCalendarPicker = true
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = VioraNeonLime, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("Choose date...", color = Color.White, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
        
    // DatePicker Modal
    @OptIn(ExperimentalMaterial3Api::class)
    if (showCalendarPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentTask.selectedDeadlineMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCalendarPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCalendarPicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedCalendar = Calendar.getInstance().apply { timeInMillis = millis }
                            val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                            val formattedDate = dateFormat.format(selectedCalendar.time)
                            updateTask(currentTask.copy(selectedDeadlineMillis = millis, dueDateText = formattedDate))
                        }
                    }
                ) {
                    Text("OK", color = VioraNeonLime)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarPicker = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = VioraNeonLime,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color.Gray,
                    subheadContentColor = Color.Gray,
                    navigationContentColor = Color.White,
                    yearContentColor = Color.White,
                    currentYearContentColor = VioraNeonLime,
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = VioraNeonLime,
                    dayContentColor = Color.White,
                    disabledDayContentColor = Color.DarkGray,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = VioraNeonLime,
                    todayContentColor = VioraNeonLime,
                    todayDateBorderColor = VioraNeonLime
                )
            )
        }
    }

    // TimePicker Bottom Sheet for Today / Tomorrow
    @OptIn(ExperimentalMaterial3Api::class)
    if (showTimePickerSheet) {
        val now = Calendar.getInstance()
        var selectedHour by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY)) }
        var selectedMinute by remember { mutableIntStateOf(now.get(Calendar.MINUTE)) }

        val skipTimeSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            sheetState = skipTimeSheetState,
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = skipTimeSheetState, defaultRadius = 28.dp),
            onDismissRequest = performSkipTime,
            containerColor = Color(0xFF242426),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isToday = pendingDateForTimePicker?.let {
                    val today = Calendar.getInstance()
                    it.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    it.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                } ?: true

                Text(
                    text = if (isToday) "Select time for Today" else "Select time for Tomorrow",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                CupertinoWheelPicker(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    onTimeChanged = { h, m ->
                        selectedHour = h
                        selectedMinute = m
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = performSkipTime
                    ) {
                        Text(
                            text = "Skip",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            val cal = (pendingDateForTimePicker ?: Calendar.getInstance()).clone() as Calendar
                            cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                            cal.set(Calendar.MINUTE, selectedMinute)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)

                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                            val dayPrefix = if (isToday) "Today" else "Tomorrow"
                            val formattedDate = "$dayPrefix at $formattedTime"

                            updateTask(currentTask.copy(
                                selectedDeadlineMillis = cal.timeInMillis,
                                dueDateText = formattedDate
                            ))
                            showTimePickerSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Set Time",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

        // Tag Input Modal
        if (showTagInput) {
            val tagSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ModalBottomSheet(
                sheetState = tagSheetState,
                shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = tagSheetState, defaultRadius = 28.dp),
                onDismissRequest = { showTagInput = false },
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
            ) {
                // To keep focus and show keyboard automatically
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    if (currentTask.tags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp)
                                .padding(bottom = 12.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentTask.tags.forEach { tag ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(Color(0xFFF2F4F7))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "# $tag",
                                        color = Color.Black,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove tag",
                                        tint = Color.Black.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable {
                                                updateTask(currentTask.copy(tags = currentTask.tags - tag))
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontSize = 19.sp,
                                color = Color.Black,
                                fontFamily = SFProDisplayFontFamily
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#",
                                        color = Color.Black.copy(alpha = 0.4f),
                                        fontSize = 24.sp,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (newTagText.isEmpty()) {
                                            Text(
                                                text = "Enter tag here...",
                                                color = Color.Gray.copy(alpha = 0.5f),
                                                fontSize = 19.sp,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD6E3FF))
                                .clickable {
                                    if (newTagText.isNotBlank()) {
                                        updateTask(currentTask.copy(tags = currentTask.tags + newTagText.trim()))
                                        newTagText = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add tag",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CupertinoWheelPicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var hourState by remember { mutableIntStateOf(initialHour) }
    var minuteState by remember { mutableIntStateOf(initialMinute) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scroll wheels • Tap selected center time to edit",
            color = Color.Gray.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WheelColumn(
                items = (0..23).map { String.format(Locale.getDefault(), "%02d", it) },
                selectedIndex = hourState,
                onSelectedIndexChanged = { h ->
                    hourState = h
                    onTimeChanged(hourState, minuteState)
                },
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            WheelColumn(
                items = (0..59).map { String.format(Locale.getDefault(), "%02d", it) },
                selectedIndex = minuteState,
                onSelectedIndexChanged = { m ->
                    minuteState = m
                    onTimeChanged(hourState, minuteState)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 52.dp
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(selectedIndex) { mutableStateOf(String.format(Locale.getDefault(), "%02d", selectedIndex)) }
    val focusRequester = remember { FocusRequester() }

    // Sync selected index when user scrolls
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isEditing) {
            val newIdx = listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex)
            onSelectedIndexChanged(newIdx)
            editText = String.format(Locale.getDefault(), "%02d", newIdx)
        }
    }

    // Scroll to item when selectedIndex changes externally
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != selectedIndex) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    // Automatically cancel text edit mode if user starts scrolling the wheel
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && isEditing) {
            isEditing = false
        }
    }

    Box(
        modifier = modifier.height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        // Highlighting center box for selected position
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF333336))
                .border(
                    width = if (isEditing) 1.5.dp else 0.dp,
                    color = if (isEditing) VioraNeonLime else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 59.dp)
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = listState.firstVisibleItemIndex == index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable(enabled = isSelected) {
                            if (isSelected) {
                                isEditing = !isEditing
                                editText = item
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected && isEditing) {
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        BasicTextField(
                            value = editText,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(2)
                                editText = digitsOnly
                                val parsed = digitsOnly.toIntOrNull()
                                if (parsed != null && parsed in 0..items.lastIndex) {
                                    onSelectedIndexChanged(parsed)
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        isEditing = false
                                    }
                                },
                            textStyle = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = VioraNeonLime,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isEditing = false
                                }
                            ),
                            cursorBrush = SolidColor(VioraNeonLime)
                        )
                    } else {
                        Text(
                            text = item,
                            fontSize = if (isSelected) 28.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) VioraNeonLime else Color.White.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}
