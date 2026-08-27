package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CreateTeamBottomSheet
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.animateEnter
import com.example.viewmodel.VioraTaskViewModel
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamName: String,
    onBack: () -> Unit,
    viewModel: VioraTaskViewModel,
    backHandlerEnabled: Boolean = true
) {
    var showCreateListSheet by remember { mutableStateOf(false) }
    var showEditTeamSheet by remember { mutableStateOf(false) }
    var showAddMemberSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    

    val allLists by viewModel.lists.collectAsState()
    val lists = remember(allLists, teamName) {
        val teamId = viewModel.getTeamIdByName(teamName) ?: teamName.lowercase().replace(" ", "_")
        if (teamName == "All Lists") {
            allLists.map { it.name }
        } else if (teamName == "Personal" || teamName == "Personal Space" || teamId == "personal_space") {
            allLists.filter { it.teamId.isEmpty() || it.teamId == "personal_space" }.map { it.name }
        } else {
            allLists.filter { it.teamId == teamId }.map { it.name }
        }
    }
    
    val allTeams by viewModel.teams.collectAsState()
    val teamMembers = remember(allTeams, teamName) {
        val teamId = viewModel.getTeamIdByName(teamName) ?: teamName.lowercase().replace(" ", "_")
        viewModel.getTeamMembers(teamId)
    }


    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onBack,
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
            label = "teamDetailSpringBack"
        )
        val hasSubDialog = showDeleteConfirm || showArchiveConfirm || showCreateListSheet || showEditTeamSheet || showAddMemberSheet || showOptionsMenu

        PredictiveBackHandler(enabled = backHandlerEnabled) { progressFlow ->
            if (hasSubDialog) {
                if (showDeleteConfirm || showArchiveConfirm) {
                    showDeleteConfirm = false
                    showArchiveConfirm = false
                } else 

        if (showCreateListSheet) {
                    showCreateListSheet = false
                } else if (showAddMemberSheet) {
                    showAddMemberSheet = false
                } else if (showEditTeamSheet) {
                    showEditTeamSheet = false
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VioraBackground)
                    .statusBarsPadding()
            ) {
                // Top Bar
                com.example.ui.components.VioraTopAppBar(
                    navigationIcon = {
                        com.example.ui.components.VioraHeaderIconButton(
                            icon = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Back",
                            onClick = { onBack() }
                        )
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pull down to close",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    },
                    actions = {
                        com.example.ui.components.VioraHeaderIconButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "Add List",
                            onClick = { showCreateListSheet = true },
                            iconSize = 24.dp
                        )
                    }
                )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title and Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = teamName,
                            color = Color.White,
                            fontSize = 40.sp,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-1).sp
                        )
                        
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
                                                    Icons.Rounded.PersonAdd,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Add members",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showAddMemberSheet = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.Edit,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Edit team",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showEditTeamSheet = true
                                        },
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
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Archive team",
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
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Delete team",
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontFamily = SFProDisplayFontFamily,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Shared with
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF4A4A4A), RoundedCornerShape(32.dp))
                            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Shared with ${teamMembers.size} users",
                            color = Color(0xFFAAAAAA),
                            fontSize = 16.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (teamMembers.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((-10).dp),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    val maxAvatars = 3
                                    val displayPhotos = teamMembers.take(maxAvatars)
                                    
                                    displayPhotos.forEach { photo ->
                                        com.example.ui.components.UserAvatar(
                                            userId = photo,
                                            size = 32.dp,
                                            modifier = Modifier.border(1.5.dp, Color(0xFF1C1C1E), CircleShape)
                                        )
                                    }
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(VioraNeonLime)
                                    .clickable { showAddMemberSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonAdd,
                                    contentDescription = "Add Member",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text("Search lists/teams", color = Color(0xFFAAAAAA), fontFamily = SFProDisplayFontFamily) 
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
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose list",
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Alphabet",
                                color = VioraNeonLime,
                                fontSize = 14.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            val filteredLists = lists.filter { it.contains(searchQuery, ignoreCase = true) }
            itemsIndexed(filteredLists) { index, listName ->
                val staggerDelay = if (index < 8) index * 40 else 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateEnter(delayMillis = staggerDelay)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.viewListDetail(listName) }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // Avatar/Initials
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE082)), // Random mock color
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = listName.take(1).uppercase(),
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = teamName.lowercase(),
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
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
                        text = if (showDeleteConfirm) "Are you sure\ndelete this team?" else "Are you sure\narchive this team?",
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
                            "By deleting this team, it will be permanently unavailable. If you think you may need it later, try archiving it."
                        } else {
                            "By archiving this team, it will be temporarily hidden. You can restore it from settings anytime."
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
                                viewModel.deleteTeam(teamName)
                                onBack()
                            } else {
                                showArchiveConfirm = false
                                viewModel.archiveTeam(teamName)
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
    }
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
                viewModel.addListToTeam(teamName, listName, days)
                showCreateListSheet = false
            }
        )
    }
    
    if (showEditTeamSheet) {
        val teamDefaultDeadline = viewModel.getTeamDefaultDeadline(teamName)
        CreateTeamBottomSheet(
            onDismiss = { showEditTeamSheet = false },
            onCreate = { newTeamName, deadline ->
                val days = when (deadline) {
                    "Daily" -> 1
                    "Weekly" -> 7
                    "Monthly" -> 30
                    "Account Default", "Team Default" -> null
                    else -> 3
                }
                viewModel.updateTeam(teamName, newTeamName, days)
                showEditTeamSheet = false
            },
            initialTeamName = teamName,
            initialDeadline = when (teamDefaultDeadline) {
                1 -> "Daily"
                7 -> "Weekly"
                30 -> "Monthly"
                else -> "Team Default"
            },
            title = "Edit Team"
        )
    }

    if (showAddMemberSheet) {
        val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddMemberSheet = false },
            sheetState = addSheetState,
            containerColor = Color(0xFF262626),
            dragHandle = null,
    
        ) {
            AddMemberBottomSheet(
                onDismiss = { showAddMemberSheet = false },
                onAdd = { username ->
                    if (username.isNotBlank()) {
                        viewModel.updateTeamMembers(teamName, listOf(username), null)
                    }
                    showAddMemberSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var listName by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("Daily") }
    var customDays by remember { mutableStateOf(3) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
        containerColor = Color(0xFF333333),
        dragHandle = null,

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header: Title and dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (step == 1) "Create List" else "List Default\nDeadline",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (step >= 1) VioraNeonLime else Color.Black)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (step >= 2) VioraNeonLime else Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (step == 1) "By deleting this task, it will be permanently unavailable.\nIf you think you may need it later." else "Tasks without deadlines use this deadline.",
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 1) {
                // Step 1: TextField
                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text("List name", fontFamily = SFProDisplayFontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioraNeonLime,
                        unfocusedBorderColor = VioraNeonLime,
                        focusedLabelColor = Color.White.copy(alpha = 0.5f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = VioraNeonLime,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (listName.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFAAAAAA), CircleShape)
                                    .clickable { listName = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFAAAAAA),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    textStyle = TextStyle(fontSize = 18.sp, fontFamily = SFProDisplayFontFamily)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { if (listName.isNotBlank()) step = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                com.example.ui.components.DefaultDeadlineSelector(
                    selectedOption = deadline,
                    onOptionSelected = { deadline = it },
                    customDays = customDays,
                    onCustomDaysChanged = { customDays = it },
                    textColor = Color.White,
                    unselectedTextColor = Color.White,
                    borderColor = Color.White,
                    selectedBackgroundColor = Color(0xFF1E3300),
                    selectedItemTextColor = VioraNeonLime,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onCreate(listName, deadline) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Create",
                        fontSize = 18.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { onCreate(listName, "Team Default") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Skip & use team default",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}



@Composable
fun AddMemberBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            text = "Add Member",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enter username or email address to add to team.",
            color = Color(0xFFCCCCCC),
            fontSize = 14.sp,
            fontFamily = SFProDisplayFontFamily,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username or Email", fontFamily = SFProDisplayFontFamily) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioraNeonLime,
                unfocusedBorderColor = VioraNeonLime,
                focusedLabelColor = Color.White.copy(alpha = 0.5f),
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = VioraNeonLime,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            trailingIcon = {
                if (username.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFAAAAAA), CircleShape)
                            .clickable { username = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            textStyle = TextStyle(fontSize = 18.sp, fontFamily = SFProDisplayFontFamily)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { if (username.isNotBlank()) onAdd(username) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Add to Team",
                fontSize = 18.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Cancel",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
