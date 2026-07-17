package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamName: String,
    onBack: () -> Unit,
    viewModel: VioraTaskViewModel
) {
    var showCreateListSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Mock lists for the team
    val lists = listOf("Charchoob", "GymShow", "Hub", "Romak")

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.PushPin, // Approximation for the map pin icon
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pull down to select team",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = SFProDisplayFontFamily
                )
            }
            
            IconButton(onClick = { showCreateListSheet = true }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add List",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

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
                            
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit list", color = Color.Black, fontFamily = SFProDisplayFontFamily) },
                                    onClick = { showOptionsMenu = false },
                                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.Black) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Archive list", color = Color.Black, fontFamily = SFProDisplayFontFamily) },
                                    onClick = { showOptionsMenu = false },
                                    leadingIcon = { Icon(Icons.Rounded.Archive, contentDescription = null, tint = Color.Black) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete list", color = Color.Red, fontFamily = SFProDisplayFontFamily) },
                                    onClick = { showOptionsMenu = false },
                                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red) }
                                )
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
                            text = "Shared with 4 users",
                            color = Color(0xFFAAAAAA),
                            fontSize = 16.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mock avatars
                            Box(modifier = Modifier.width(56.dp).height(32.dp)) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray).align(Alignment.CenterStart))
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.DarkGray).align(Alignment.Center).offset(x = 12.dp))
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(VioraNeonLime)
                                    .clickable { /* Share */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.IosShare,
                                    contentDescription = "Share",
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
            
            items(lists.filter { it.contains(searchQuery, ignoreCase = true) }) { listName ->
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
    
    if (showCreateListSheet) {
        CreateListBottomSheet(
            onDismiss = { showCreateListSheet = false },
            onCreate = { listName, _ ->
                // Here we would create a new list for the team
                showCreateListSheet = false
            }
        )
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
        containerColor = Color(0xFF333333),
        dragHandle = null,
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
