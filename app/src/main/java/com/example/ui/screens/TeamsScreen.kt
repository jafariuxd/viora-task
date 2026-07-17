package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.components.TeamListItem
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(viewModel: VioraTaskViewModel) {
    val teams by viewModel.teams.collectAsState()
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    
    var showCreateTeamSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraBackground)
            .padding(top = 48.dp) // Status bar padding
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showCreateTeamSheet = true }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Team",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Team",
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

        Spacer(modifier = Modifier.height(48.dp))

        // Teams List
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(teams) { team ->
                TeamListItem(
                    teamName = team,
                    isSelected = team == selectedTeam,
                    onRowClick = { viewModel.viewTeamDetail(team) },
                    onRadioClick = { viewModel.selectTeam(team) }
                )
            }
        }
    }
    
    if (showCreateTeamSheet) {
        CreateTeamBottomSheet(
            onDismiss = { showCreateTeamSheet = false },
            onCreate = { teamName, _ ->
                viewModel.addTeam(teamName)
                showCreateTeamSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var teamName by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("Daily") }
    var customDays by remember { mutableStateOf(3) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF333333),
        dragHandle = null, // No drag handle in design
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
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
                    text = if (step == 1) "Create Team" else "Team Default\nDeadline",
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
                text = if (step == 1) "By deleting this task, it will be permanently unavailable.\nIf you think you may need it later." else "Lists without deadlines use this deadline.",
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 1) {
                // Step 1: TextField
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team name", fontFamily = SFProDisplayFontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioraNeonLime,
                        unfocusedBorderColor = VioraNeonLime,
                        focusedLabelColor = VioraNeonLime,
                        unfocusedLabelColor = VioraNeonLime,
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
                        if (teamName.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFAAAAAA), CircleShape)
                                    .clickable { teamName = "" },
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
                    onClick = { if (teamName.isNotBlank()) step = 2 },
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
                    onClick = { onCreate(teamName, deadline) },
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
                    onClick = { onCreate(teamName, "Account Default") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Skip & use account default",
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
