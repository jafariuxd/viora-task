package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.VioraTaskViewModel

@Composable
fun ArchivesScreen(
    onBack: () -> Unit,
    viewModel: VioraTaskViewModel
) {
    val archivedTeams by viewModel.archivedTeams.collectAsState()
    val archivedLists by viewModel.archivedLists.collectAsState()
    val archivedTasks by viewModel.archivedTasks.collectAsState()

    var activeTab by remember { mutableStateOf("Teams") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        com.example.ui.components.VioraTopAppBar(
            navigationIcon = {
                com.example.ui.components.VioraHeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
            },
            title = {
                Text(
                    text = "Archives",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFProDisplayFontFamily
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Teams", "Lists", "Tasks")
            tabs.forEach { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isSelected) VioraNeonLime else Color(0xFF1E1E1E))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) VioraNeonLime else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { activeTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            when (activeTab) {
                "Teams" -> {
                    if (archivedTeams.isEmpty()) {
                        EmptyStateView(text = "No archived teams found.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(archivedTeams) { team ->
                                ArchivedItemRow(
                                    title = team.name,
                                    subtitle = "Created by you",
                                    onRestore = { viewModel.restoreTeam(team.id) }
                                )
                            }
                        }
                    }
                }
                "Lists" -> {
                    if (archivedLists.isEmpty()) {
                        EmptyStateView(text = "No archived lists found.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(archivedLists) { list ->
                                val teamName = viewModel.getTeamNameById(list.teamId)
                                ArchivedItemRow(
                                    title = list.name,
                                    subtitle = "In team $teamName",
                                    onRestore = { viewModel.restoreList(list.id) }
                                )
                            }
                        }
                    }
                }
                "Tasks" -> {
                    if (archivedTasks.isEmpty()) {
                        EmptyStateView(text = "No archived tasks found.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(archivedTasks) { task ->
                                ArchivedItemRow(
                                    title = task.title,
                                    subtitle = task.folder,
                                    onRestore = { viewModel.restoreTask(task.id) }
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
fun ArchivedItemRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26262A)),
        border = BorderStroke(1.dp, Color(0xFF38383E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SFProDisplayFontFamily
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontFamily = SFProDisplayFontFamily,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = "Restore",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Restore",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 16.sp,
            fontFamily = SFProDisplayFontFamily
        )
    }
}
