package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import com.example.model.TeamRole
import com.example.model.User
import com.example.ui.components.TeamRoleBadge
import com.example.ui.components.TeamMembersManagementBottomSheet
import com.example.ui.components.RoleSelectorBottomSheet
import com.example.ui.components.CreateTeamBottomSheet
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraGrayText
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.animateEnter
import com.example.viewmodel.VioraTaskViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
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
    var showTeamMembersSheet by remember { mutableStateOf(false) }
    var showInviteSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    var showRoleSelectorSheet by remember { mutableStateOf(false) }
    var selectedMemberForRole by remember { mutableStateOf<User?>(null) }
    

    val allTeams by viewModel.teams.collectAsState()
    val allLists by viewModel.lists.collectAsState()
    val personalListId by viewModel.personalListId.collectAsState()
    val lists = remember(allLists, allTeams, teamName, personalListId) {
        val teamId = viewModel.getTeamIdByName(teamName) ?: teamName.lowercase().replace(" ", "_")
        val validNonPersonalTeamIds = allTeams.filter { it != "All Lists" && it != "Personal" && it != "Personal Space" }
            .mapNotNull { viewModel.getTeamIdByName(it) }
            .toSet()

        if (teamName == "All Lists") {
            val pinned = allLists.filter { it.isPersonal || it.id == personalListId || it.name.equals("My Tasks", ignoreCase = true) }
            val other = allLists.filter { it !in pinned }
            (pinned + other).map { it.name }
        } else if (teamName == "Personal" || teamName == "Personal Space" || teamId == "personal_space") {
            // All lists without a team belong to Personal Space!
            val personalLists = allLists.filter { 
                it.teamId.isBlank() || it.teamId == "personal_space" || it.teamId !in validNonPersonalTeamIds
            }
            val pinned = personalLists.filter { it.isPersonal || it.id == personalListId || it.name.equals("My Tasks", ignoreCase = true) }
            val other = personalLists.filter { it !in pinned }
            (pinned + other).map { it.name }
        } else {
            allLists.filter { it.teamId == teamId }.map { it.name }
        }
    }
    val teamUpdateTrigger by viewModel.teamUpdateTrigger.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()

    val teamMemberUsers = remember(allTeams, teamName, teamUpdateTrigger) {
        val teamId = viewModel.getTeamIdByName(teamName) ?: teamName.lowercase().replace(" ", "_")
        viewModel.getTeamMemberUsers(teamId)
    }
    val teamMembers = remember(teamMemberUsers) {
        teamMemberUsers.map { it.username }
    }
    val currentTeamId = remember(teamName) {
        viewModel.getTeamIdByName(teamName) ?: teamName.lowercase().replace(" ", "_")
    }
    val pendingInvites = remember(allTeams, teamName) {
        viewModel.getTeamPendingInvitations(teamName)
    }

    val currentUserRole = remember(teamName, currentUserName, teamUpdateTrigger, allTeams) {
        viewModel.getCurrentUserRole(teamName)
    }
    val canManageMembers = currentUserRole.canManageMembers
    val canManageSettings = currentUserRole.canManageSettings
    val canDeleteTeam = currentUserRole.canDeleteTeam


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
        val hasSubDialog = showDeleteConfirm || showArchiveConfirm || showCreateListSheet || showEditTeamSheet || showAddMemberSheet || showTeamMembersSheet || showInviteSheet || showOptionsMenu || showRoleSelectorSheet

        PredictiveBackHandler(enabled = backHandlerEnabled) { progressFlow ->
            if (hasSubDialog) {
                if (showDeleteConfirm || showArchiveConfirm) {
                    showDeleteConfirm = false
                    showArchiveConfirm = false
                } else if (showCreateListSheet) {
                    showCreateListSheet = false
                } else if (showAddMemberSheet) {
                    showAddMemberSheet = false
                } else if (showTeamMembersSheet) {
                    showTeamMembersSheet = false
                } else if (showInviteSheet) {
                    showInviteSheet = false
                } else if (showRoleSelectorSheet) {
                    showRoleSelectorSheet = false
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

        val filteredLists = lists.filter { it.contains(searchQuery, ignoreCase = true) }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (filteredLists.isEmpty() && searchQuery.isEmpty()) 200.dp else 24.dp)
            ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                                    Icons.Rounded.Link,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Invite link",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showInviteSheet = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.Group,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Team members",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showTeamMembersSheet = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                    if (canManageMembers) {
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
                                    }
                                    if (canManageSettings) {
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
                                    }
                                    if (canDeleteTeam) {
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
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF2A2A2A))
                            .border(1.dp, Color(0xFF4A4A4A), RoundedCornerShape(32.dp))
                            .clickable { showTeamMembersSheet = true }
                            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Shared with ${teamMembers.size} users",
                                color = Color(0xFFAAAAAA),
                                fontSize = 16.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                            TeamRoleBadge(role = currentUserRole)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (teamMembers.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((-10).dp),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    val maxAvatars = 3
                                    val displayUsers = teamMemberUsers.take(maxAvatars)
                                    
                                    displayUsers.forEach { user ->
                                        com.example.ui.components.UserAvatar(
                                            userId = user.username,
                                            avatarUri = user.avatarUrl,
                                            size = 32.dp,
                                            modifier = Modifier.border(1.5.dp, Color(0xFF1C1C1E), CircleShape)
                                        )
                                    }
                                }
                            }
                            
                            if (canManageMembers) {
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
                    }

                    // Pending Invites Section
                    if (pendingInvites.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1C1C1E))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(VioraNeonLime)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pending Invites (${pendingInvites.size})",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SFProDisplayFontFamily
                                    )
                                }
                                Text(
                                    text = "Waiting for response",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            pendingInvites.forEach { pendingUser ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2A2A2A))
                                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = pendingUser.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "@$pendingUser",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                            Text(
                                                text = "Invitation pending",
                                                color = Color(0xFFAAAAAA),
                                                fontSize = 12.sp,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(VioraNeonLime.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Invited",
                                            color = VioraNeonLime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (lists.isNotEmpty()) {
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
            }
            
            if (filteredLists.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No lists found matching \"$searchQuery\"",
                            color = Color(0xFFAAAAAA),
                            fontSize = 15.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }
                }
            } else {
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
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        val teamColor = remember(teamName) { getTeamColor(teamName) }
                        // List Icon / Indicator with team's color
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(teamColor.copy(alpha = 0.15f))
                                .border(1.dp, teamColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val target = allLists.find { it.name == listName }
                            val isMyTasksEarly = listName.equals("My Tasks", ignoreCase = true) || target?.isPersonal == true || target?.id == personalListId || ((teamName == "Personal" || teamName == "Personal Space") && index == 0)
                            if (isMyTasksEarly) {
                                Icon(
                                    imageVector = Icons.Rounded.PushPin,
                                    contentDescription = "My Tasks Pinned",
                                    tint = VioraNeonLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(teamColor)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        val isMyTasks = remember(allLists, listName, personalListId) {
                            val target = allLists.find { it.name == listName }
                            listName.equals("My Tasks", ignoreCase = true) || target?.isPersonal == true || target?.id == personalListId || ((teamName == "Personal" || teamName == "Personal Space") && index == 0)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = listName,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isMyTasks) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = "My Tasks Pinned",
                                        tint = VioraNeonLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isMyTasks) "personal space • private" else teamName.lowercase(),
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

        // Pinned Bottom Neu-Brutalist Collaborate Card
        if (filteredLists.isEmpty() && searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Collaborate in $teamName",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = SFProDisplayFontFamily,
                            letterSpacing = (-0.4).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Invite members to start organizing lists and tasks together.",
                            color = Color(0xFF555555),
                            fontSize = 13.sp,
                            fontFamily = SFProDisplayFontFamily,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { showInviteSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VioraNeonLime,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Invite Members",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = SFProDisplayFontFamily,
                                color = Color.Black
                            )
                        }
                    }
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
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = addSheetState, defaultRadius = 28.dp),
            containerColor = Color(0xFF1C1C1E),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }
        ) {
            AddMemberBottomSheet(
                onDismiss = { showAddMemberSheet = false },
                onAdd = { username ->
                    viewModel.inviteUserToTeam(teamName, username)
                    showAddMemberSheet = false
                },
                onInviteViaLinkClick = {
                    showAddMemberSheet = false
                    showInviteSheet = true
                },
                teamName = teamName,
                teamId = currentTeamId,
                viewModel = viewModel
            )
        }
    }

    // Team Members Management Bottom Sheet
    TeamMembersManagementBottomSheet(
        visible = showTeamMembersSheet,
        onDismiss = { showTeamMembersSheet = false },
        teamName = teamName,
        teamMembers = teamMemberUsers,
        currentUsername = currentUserName,
        currentUserRole = currentUserRole,
        getMemberRole = { username -> viewModel.getTeamRoleForUser(teamName, username) },
        onInviteClick = {
            showTeamMembersSheet = false
            showAddMemberSheet = true
        },
        onMemberClick = { member ->
            selectedMemberForRole = member
            showRoleSelectorSheet = true
        }
    )

    // Role Selector Bottom Sheet
    RoleSelectorBottomSheet(
        visible = showRoleSelectorSheet,
        targetUser = selectedMemberForRole,
        teamName = teamName,
        currentRole = selectedMemberForRole?.let { viewModel.getTeamRoleForUser(teamName, it.username) } ?: TeamRole.MEMBER,
        currentUserRole = currentUserRole,
        currentUsername = currentUserName,
        onDismiss = { showRoleSelectorSheet = false },
        onRoleSelected = { newRole ->
            selectedMemberForRole?.let { target ->
                viewModel.updateMemberRole(teamName, target.username, newRole)
            }
        },
        onRemoveMember = {
            selectedMemberForRole?.let { target ->
                viewModel.removeMemberFromTeam(teamName, target.username)
            }
        }
    )

    com.example.ui.components.InviteBottomSheet(
        visible = showInviteSheet,
        onDismiss = { showInviteSheet = false },
        inviteContext = com.example.ui.components.InviteContext.TeamMembership(
            teamName = teamName,
            teamId = currentTeamId
        )
    )
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
        containerColor = Color(0xFF1C1C1E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp)
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
                    fontSize = 32.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (step == 1) "Organize your tasks into focused project lists." else "Tasks without deadlines use this deadline.",
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

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
    onAdd: (String) -> Unit,
    onInviteViaLinkClick: (() -> Unit)? = null,
    teamName: String = "",
    teamId: String = "",
    viewModel: com.example.viewmodel.VioraTaskViewModel? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<com.example.model.viora.UserSearchResultDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var invitedUsers by remember { mutableStateOf<Set<String>>(emptySet()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    // Debounced live user search via ViewModel
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length < 3) {
            searchResults = emptyList()
            isSearching = false
        } else {
            isSearching = true
            kotlinx.coroutines.delay(300)
            if (viewModel != null) {
                searchResults = viewModel.searchUsers(q)
            }
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            text = "Invite to Team",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Search registered users to send a team invitation. They must accept before joining.",
            color = Color(0xFFCCCCCC),
            fontSize = 14.sp,
            fontFamily = SFProDisplayFontFamily,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search username or email (min 3 chars)", color = Color(0xFFAAAAAA), fontFamily = SFProDisplayFontFamily, fontSize = 15.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioraNeonLime,
                unfocusedBorderColor = Color(0xFF333333),
                focusedLabelColor = Color.White.copy(alpha = 0.5f),
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = VioraNeonLime,
                focusedContainerColor = Color(0xFF242426),
                unfocusedContainerColor = Color(0xFF242426)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (searchQuery.isNotEmpty()) VioraNeonLime else Color(0xFFAAAAAA),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = VioraNeonLime,
                        strokeWidth = 2.dp
                    )
                } else if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333333))
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            },
            textStyle = TextStyle(fontSize = 16.sp, fontFamily = SFProDisplayFontFamily)
        )

        Spacer(Modifier.height(16.dp))

        // Live Search Results
        if (searchQuery.trim().length >= 3) {
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Searching users...",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No users found matching \"$searchQuery\"",
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                        Spacer(Modifier.height(12.dp))
                        // Allow direct invite by typed username anyway if desired
                        Button(
                            onClick = {
                                val target = searchQuery.trim()
                                if (target.isNotEmpty()) {
                                    invitedUsers = invitedUsers + target
                                    onAdd(target)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2A2A),
                                contentColor = VioraNeonLime
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Send invite to @$searchQuery anyway", fontSize = 13.sp, fontFamily = SFProDisplayFontFamily)
                        }
                    }
                }
            } else {
                Text(
                    text = "Matching Users (${searchResults.size})",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    fontFamily = SFProDisplayFontFamily,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    searchResults.forEach { user ->
                        val isInvited = invitedUsers.contains(user.username)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF242426))
                                .border(1.dp, if (isInvited) VioraNeonLime.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                com.example.ui.components.UserAvatar(
                                    userId = user.username,
                                    avatarUri = com.example.util.ImageUtil.resolveAvatarUrl(user.avatar),
                                    size = 40.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.fullName.ifBlank { user.username },
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = SFProDisplayFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "@${user.username} • ${user.email}",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 12.sp,
                                        fontFamily = SFProDisplayFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            if (isInvited) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VioraNeonLime.copy(alpha = 0.15f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = VioraNeonLime,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Invited",
                                            color = VioraNeonLime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        invitedUsers = invitedUsers + user.username
                                        onAdd(user.username)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VioraNeonLime,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Invite",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SFProDisplayFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Copy Link Outline / Tonal Button
        val inviteLink = remember(teamName, teamId) {
            val encodedName = try {
                java.net.URLEncoder.encode(teamName, "UTF-8")
            } catch (e: Exception) {
                teamName
            }
            "https://viora.app/join/team?name=$encodedName&id=$teamId"
        }

        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Viora Team Invite Link", inviteLink)
                clipboard?.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "Invite link copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                isCopied = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isCopied) VioraNeonLime else Color.White.copy(alpha = 0.15f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isCopied) VioraNeonLime.copy(alpha = 0.12f) else Color(0xFF262628),
                contentColor = if (isCopied) VioraNeonLime else Color.White
            )
        ) {
            Icon(
                imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.Link,
                contentDescription = null,
                tint = if (isCopied) VioraNeonLime else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isCopied) "Link Copied!" else "Copy Invite Link",
                fontSize = 15.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
