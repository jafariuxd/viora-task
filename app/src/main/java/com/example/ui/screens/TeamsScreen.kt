package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PushPin
import com.example.ui.theme.VioraNeonLime
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Task
import com.example.model.TaskList
import com.example.ui.components.CreateTeamBottomSheet
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.animateEnter
import com.example.viewmodel.VioraTaskViewModel

fun getTeamColor(teamName: String): Color {
    return when {
        teamName.equals("Personal Space", ignoreCase = true) || teamName.equals("Personal", ignoreCase = true) -> Color(0xFF38BDF8) // Desktop cyan
        teamName.equals("Nexus", ignoreCase = true) -> Color(0xFFFF5E7E) // Desktop coral pink
        else -> {
            val teamPalette = listOf(
                Color(0xFFA78BFA), // Lavender / Purple
                Color(0xFFFF5E7E), // Coral Pink
                Color(0xFF34D399), // Emerald Mint
                Color(0xFFFBBF24), // Amber Gold
                Color(0xFFF472B6), // Rose
                Color(0xFFB4FF00), // Neon Lime
                Color(0xFF60A5FA)  // Sky Blue
            )
            val index = kotlin.math.abs(teamName.hashCode()) % teamPalette.size
            teamPalette[index]
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(viewModel: VioraTaskViewModel) {
    val teams by viewModel.teams.collectAsState()
    val allLists by viewModel.lists.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    
    var showCreateTeamSheet by remember { mutableStateOf(false) }
    var targetTeamForNewList by remember { mutableStateOf<String?>(null) }
    var showCreateListSheet by remember { mutableStateOf(false) }



    BackHandler(enabled = showCreateTeamSheet || showCreateListSheet) {
        showCreateTeamSheet = false
        showCreateListSheet = false
        targetTeamForNewList = null
    }

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshDashboard() },
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
                .background(VioraBackground)
                .statusBarsPadding()
        ) {
            // Top Bar
            Box(modifier = Modifier.animateEnter(delayMillis = 0)) {
                com.example.ui.components.VioraTopAppBar(
                    actions = {
                        com.example.ui.components.VioraHeaderIconButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "Add Team",
                            onClick = { showCreateTeamSheet = true },
                            iconSize = 24.dp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Box(modifier = Modifier.animateEnter(delayMillis = 50)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Teams",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have ${teams.size} created teams",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Pending Invitations Section (if user has received team invites)
            val pendingInvitations by viewModel.pendingTeamInvitations.collectAsState()

            if (pendingInvitations.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .animateEnter(delayMillis = 80)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1C1C1E))
                            .border(1.5.dp, VioraNeonLime.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
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
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Pending Invitations (${pendingInvitations.size})",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                            Text(
                                text = "Action required",
                                color = VioraNeonLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        pendingInvitations.forEach { invite ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF262628),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = invite.teamName,
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                            Text(
                                                text = if (invite.invitedBy != null) "Invited by @${invite.invitedBy.username}" else "Team invitation",
                                                color = Color(0xFFAAAAAA),
                                                fontSize = 13.sp,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.acceptTeamInvitation(invite.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = VioraNeonLime,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text(
                                                text = "Accept",
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFProDisplayFontFamily,
                                                fontSize = 14.sp
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.declineTeamInvitation(invite.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = Color(0xFF8E8E93)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text(
                                                text = "Decline",
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = SFProDisplayFontFamily,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Teams & Nested Lists Tree Hierarchy
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(teams) { index, team ->
                    val staggerDelay = if (index < 8) index * 40 else 0
                    val teamId = remember(team) { viewModel.getTeamIdByName(team) ?: team.lowercase().replace(" ", "_") }
                    val childLists = remember(allLists, teams, team, teamId) {
                        val validNonPersonalTeamIds = teams.filter { it != "All Lists" && it != "Personal" && it != "Personal Space" }
                            .mapNotNull { viewModel.getTeamIdByName(it) }
                            .toSet()

                        if (team == "All Lists") {
                            val distinct = allLists.distinctBy { if (it.name.equals("My Tasks", ignoreCase = true)) "My Tasks" else it.id }
                            val pinned = distinct.filter { it.isPersonal || it.name.equals("My Tasks", ignoreCase = true) }
                            val others = distinct.filter { it !in pinned }
                            pinned + others
                        } else if (team == "Personal" || team == "Personal Space" || teamId == "personal_space") {
                            // All lists without a team belong to Personal Space!
                            val personalLists = allLists.filter { 
                                it.teamId.isBlank() || it.teamId == "personal_space" || it.teamId !in validNonPersonalTeamIds
                            }
                            val distinct = personalLists.distinctBy { if (it.name.equals("My Tasks", ignoreCase = true)) "My Tasks" else it.id }
                            val pinned = distinct.filter { it.isPersonal || it.name.equals("My Tasks", ignoreCase = true) }
                            val others = distinct.filter { it !in pinned }
                            pinned + others
                        } else {
                            allLists.filter { it.teamId == teamId || it.teamId == team }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .animateEnter(delayMillis = 100 + staggerDelay)
                    ) {
                        TeamAccordionCard(
                            teamName = team,
                            onSettingsClick = {
                                viewModel.viewTeamDetail(team)
                            },
                            onListClick = { listName ->
                                viewModel.viewListDetail(listName)
                            },
                            onAddListClick = {
                                targetTeamForNewList = team
                                showCreateListSheet = true
                            },
                            childLists = childLists,
                            tasks = tasks,
                            isSelected = team == selectedTeam
                        )
                    }
                }
            }
        }
    }

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
                viewModel.addTeam(teamName, days)
                showCreateTeamSheet = false
            }
        )
    }

    if (showCreateListSheet && targetTeamForNewList != null) {
        CreateListBottomSheet(
            onDismiss = {
                showCreateListSheet = false
                targetTeamForNewList = null
            },
            onCreate = { listName, deadline ->
                val days = when (deadline) {
                    "Daily" -> 1
                    "Weekly" -> 7
                    "Monthly" -> 30
                    "Account Default", "Team Default" -> null
                    else -> 3
                }
                viewModel.addListToTeam(targetTeamForNewList!!, listName, days)
                showCreateListSheet = false
                targetTeamForNewList = null
            }
        )
    }
}

@Composable
fun TeamAccordionCard(
    teamName: String,
    onSettingsClick: () -> Unit,
    onListClick: (String) -> Unit,
    onAddListClick: () -> Unit,
    childLists: List<TaskList>,
    tasks: List<Task>,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 6.dp)
    ) {
        // Main Team Header Row (Outside and Above the Card)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Name
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSettingsClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team Name (larger, bold)
                Text(
                    text = teamName,
                    color = if (isSelected) VioraNeonLime else Color.White,
                    fontSize = 22.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }

            // Action Buttons: Add List (+), Open In Full - same 24.dp icon size as the left team icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Add new list button (icon 24.dp)
                IconButton(
                    onClick = onAddListClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("add_list_btn_${teamName.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add List to $teamName",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Open In Full button (replaces settings icon, same 24.dp size)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("team_settings_btn_${teamName.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInFull,
                        contentDescription = "Open $teamName",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Gray Card containing all child lists (no border, Viora dark gray card)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1C1C1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (childLists.isEmpty()) {
                    // Empty state: Clickable row with "+" and "Add list" text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onAddListClick() }
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add list to $teamName",
                            tint = VioraNeonLime,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add list",
                            color = VioraNeonLime,
                            fontFamily = SFProDisplayFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    childLists.forEachIndexed { index, list ->
                        val activeTasksCount = tasks.count { 
                            (it.listId == list.id || it.folder == list.name) && 
                            it.status != com.example.model.TaskStatus.DONE && 
                            !it.isArchived 
                        }

                        if (index > 0) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onListClick(list.name) }
                                .padding(horizontal = 24.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.List,
                                    contentDescription = null,
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))

                                // List title (larger, medium font weight)
                                Text(
                                    text = list.name,
                                    color = Color.White,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                if (list.name.equals("My Tasks", ignoreCase = true) || list.isPersonal) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = "Pinned My Tasks",
                                        tint = VioraNeonLime,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (activeTasksCount > 0) {
                                    Text(
                                        text = "$activeTasksCount",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = SFProDisplayFontFamily,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
