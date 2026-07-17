package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.ui.components.StatusButton
import com.example.ui.components.DetailBadge
import java.util.UUID
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.rounded.Notes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    onClose: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onTaskSaved: (Task) -> Unit,
    onTaskDeleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isNewTask by remember { mutableStateOf(task.title.isEmpty()) }
    var currentTask by remember { mutableStateOf(task) }
    
    // Focus tracking for saving new task
    var titleFocused by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isNewTask) {
        if (isNewTask) {
            titleFocusRequester.requestFocus()
        }
    }

    // Dialog states
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var showAssigneePicker by remember { mutableStateOf(false) }
    var showTagInput by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    // Temporary input states
    var newTagText by remember { mutableStateOf("") }
    
    val updateTask = { newTask: Task ->
        currentTask = newTask
        if (!isNewTask) {
            onTaskSaved(newTask)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Folder Picker
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .clickable { showFolderPicker = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = "Folder",
                        tint = VioraNeonLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isNewTask && currentTask.folder == "My Tasks") "Select List" else currentTask.folder,
                        color = if (isNewTask && currentTask.folder == "My Tasks") VioraNeonLime else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = if (isNewTask && currentTask.folder == "My Tasks") VioraNeonLime else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // More Menu
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable { showMoreMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Remove Task", color = Color.Red, fontSize = 16.sp) },
                            onClick = {
                                showMoreMenu = false
                                showDeleteConfirm = true
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color.Red)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task Title Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    BasicTextField(
                        value = currentTask.title,
                        onValueChange = { updateTask(currentTask.copy(title = it)) },
                        modifier = Modifier
                            .focusRequester(titleFocusRequester)
                            .fillMaxWidth()
                            .padding(bottom = if (titleFocused) 16.dp else 0.dp)
                            .align(Alignment.TopStart)
                            .onFocusChanged { focusState ->
                                if (titleFocused && !focusState.isFocused && currentTask.title.isNotBlank()) {
                                    // Focus lost, save task
                                    if (isNewTask) {
                                        isNewTask = false
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                StatusButton(
                    text = "To-Do",
                    isSelected = currentTask.status == TaskStatus.TODO,
                    onClick = {
                        updateTask(currentTask.copy(status = TaskStatus.TODO))
                        if (!isNewTask) onStatusChange(TaskStatus.TODO)
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    text = "In Progress",
                    isSelected = currentTask.status == TaskStatus.IN_PROGRESS,
                    onClick = {
                        updateTask(currentTask.copy(status = TaskStatus.IN_PROGRESS))
                        if (!isNewTask) onStatusChange(TaskStatus.IN_PROGRESS)
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    text = "Done",
                    isSelected = currentTask.status == TaskStatus.DONE,
                    onClick = {
                        updateTask(currentTask.copy(status = TaskStatus.DONE))
                        if (!isNewTask) onStatusChange(TaskStatus.DONE)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Due Date & Assignees grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Due Date Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(101.dp)
                        .clickable { showDatePicker = true }
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
                        
                        if (currentTask.dueDateText.isEmpty()) {
                            Text(
                                text = "Set date",
                                fontSize = 20.sp,
                                fontFamily = SFProDisplayFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        } else {
                            Column {
                                Text(
                                    text = currentTask.dueDateText,
                                    fontSize = 20.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentTask.daysLeft} days left",
                                    fontSize = 14.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Assignees Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(101.dp)
                        .clickable { showAssigneePicker = true }
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
                                horizontalArrangement = Arrangement.spacedBy((-10).dp)
                            ) {
                                // Show avatars
                                currentTask.assigneePhotos.take(2).forEach { photoName ->
                                    val resId = when (photoName.lowercase()) {
                                        "mohammad" -> R.drawable.img_profile_mohammad_1783672402325
                                        "sara" -> R.drawable.img_avatar_sara_1783672418392
                                        "delaram" -> R.drawable.img_profile_mohammad_1783672402325 // using fallback for now
                                        "tala" -> R.drawable.img_avatar_sara_1783672418392 // using fallback for now
                                        else -> R.drawable.img_profile_mohammad_1783672402325
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.White, CircleShape)
                                    ) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = "Assignee",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                
                                // Plus Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(VioraNeonLime)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
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

            Spacer(modifier = Modifier.height(5.dp))

            // Tags Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            top = 12.dp,
                            bottom = if (currentTask.tags.isEmpty() && !showTagInput) 18.dp else 16.dp
                        )
                ) {
                    DetailBadge(
                        text = "Tags",
                        icon = Icons.Rounded.Tag,
                        containerColor = Color(0xFFEBF2FF),
                        textColor = Color(0xFF224075)
                    )
                    
                    Spacer(
                        modifier = Modifier.height(
                            if (currentTask.tags.isEmpty() && !showTagInput) 18.dp else 12.dp
                        )
                    )
                    
                    if (currentTask.tags.isEmpty() && !showTagInput) {
                        Text(
                            text = "Enter first tag...",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            modifier = Modifier.clickable { showTagInput = true }
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
                                                updateTask(currentTask.copy(tags = currentTask.tags - tag))
                                            }
                                    )
                                }
                            }
                            
                            if (showTagInput) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    BasicTextField(
                                        value = newTagText,
                                        onValueChange = { newTagText = it },
                                        modifier = Modifier
                                            .width(120.dp)
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(Color(0xFFF2F4F7))
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black, fontFamily = SFProDisplayFontFamily),
                                        singleLine = true
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD6E3FF))
                                            .clickable {
                                                if (newTagText.isNotBlank()) {
                                                    updateTask(currentTask.copy(tags = currentTask.tags + newTagText.trim()))
                                                    newTagText = ""
                                                    showTagInput = false
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Add",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(VioraNeonLime)
                                        .clickable { showTagInput = true },
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
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Description Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 103.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp)
                ) {
                    DetailBadge(
                        text = "Description",
                        icon = Icons.AutoMirrored.Rounded.Notes,
                        containerColor = Color(0xFFFFF4D1),
                        textColor = Color(0xFF6B5300)
                    )
                    
                    Spacer(modifier = Modifier.height(17.dp))
                    
                    BasicTextField(
                        value = currentTask.description,
                        onValueChange = { updateTask(currentTask.copy(description = it)) },
                        modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(32.dp))
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

    // Assignee Picker Dialog (simplified as a BottomSheet)
    if (showAssigneePicker) {
        ModalBottomSheet(
            onDismissRequest = { showAssigneePicker = false },
            containerColor = Color(0xFF2A2A2A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    Text("Search users to assign", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Users grid (mock)
                val users = listOf("Delaram", "Mamad", "Mohre", "Noorin", "Sorush", "Tala")
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    users.forEach { user ->
                        val isSelected = currentTask.assigneePhotos.contains(user.lowercase())
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                val newAssignees = if (isSelected) {
                                    currentTask.assigneePhotos - user.lowercase()
                                } else {
                                    currentTask.assigneePhotos + user.lowercase()
                                }
                                updateTask(currentTask.copy(assigneePhotos = newAssignees))
                            }
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                ) {
                                    // Normally image here
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(VioraNeonLime),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(user, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { showAssigneePicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Folder Picker Dialog (simplified)
    if (showFolderPicker) {
        ModalBottomSheet(
            onDismissRequest = { showFolderPicker = false },
            containerColor = Color(0xFF2A2A2A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                
                val lists = listOf("Charchoob", "GymShow", "Hub", "Mobile app", "Romak", "Users dashboard")
                lists.forEach { listName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                updateTask(currentTask.copy(folder = listName))
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
                                Text("Viora design", color = Color.Gray, fontSize = 14.sp)
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Due date picker (Options)
    if (showDatePicker) {
        ModalBottomSheet(
            onDismissRequest = { showDatePicker = false },
            containerColor = Color(0xFF383838),
            dragHandle = null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    "Tomorrow" to "1d",
                    "Next 2 days" to "2d",
                    "Next week" to "1w",
                    "Next 2 week" to "2w"
                )
                
                options.forEach { (text, badge) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                updateTask(currentTask.copy(dueDateText = text)) // simplify
                                showDatePicker = false
                            }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(badge, color = VioraNeonLime, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                        Text(text, color = Color.White, fontSize = 20.sp)
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f))
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker = false
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
}
