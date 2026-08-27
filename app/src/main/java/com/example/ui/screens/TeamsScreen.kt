package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CreateTeamBottomSheet
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.components.TeamListItem
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.animateEnter
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(viewModel: VioraTaskViewModel) {
    val teams by viewModel.teams.collectAsState()
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    
    var showCreateTeamSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = showCreateTeamSheet) {
        showCreateTeamSheet = false
    }

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

        Spacer(modifier = Modifier.height(48.dp))

        // Teams List
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(teams) { index, team ->
                val staggerDelay = if (index < 8) index * 40 else 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateEnter(delayMillis = 100 + staggerDelay)
                ) {
                    TeamListItem(
                        teamName = team,
                        isSelected = team == selectedTeam,
                        onRowClick = { viewModel.viewTeamDetail(team) }
                    )
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
}

