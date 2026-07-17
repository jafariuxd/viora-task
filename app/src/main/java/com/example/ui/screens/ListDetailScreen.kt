package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Task
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListDetailScreen(
    listName: String,
    teamName: String,
    onBack: () -> Unit,
    onTaskClick: (Task) -> Unit,
    viewModel: VioraTaskViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Tasks") }
    var showOptionsMenu by remember { mutableStateOf(false) }
    
    val tasks by viewModel.tasks.collectAsState()
    
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }

    Scaffold(
        containerColor = VioraBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add Task */ },
                containerColor = VioraNeonLime,
                contentColor = Color.Black,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Task", modifier = Modifier.size(32.dp))
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, Color(0xFF4A4A4A), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Shared avatars
                    Box(modifier = Modifier.width(64.dp).height(40.dp)) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray).align(Alignment.CenterStart).border(2.dp, Color.Black, CircleShape))
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.DarkGray).align(Alignment.Center).offset(x = 12.dp).border(2.dp, Color.Black, CircleShape))
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime)
                                .align(Alignment.CenterEnd)
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
                    
                    if (isScrolled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
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
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (!isScrolled) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = listName,
                                        color = Color.White,
                                        fontSize = 40.sp,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = (-1).sp
                                    )
                                    Text(
                                        text = teamName,
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
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Search bar
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
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                
                stickyHeader {
                    val filters = listOf("All Tasks", "To-Do", "In Progress", "Done")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VioraBackground)
                            .padding(vertical = 8.dp),
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
                                    .clickable { selectedFilter = filter }
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Tasks
                val filteredTasks = tasks.filter { task ->
                    val matchesSearch = task.title.contains(searchQuery, ignoreCase = true)
                    val matchesFilter = when (selectedFilter) {
                        "To-Do" -> task.status.name.equals("TODO", ignoreCase = true)
                        "In Progress" -> task.status.name.equals("IN_PROGRESS", ignoreCase = true)
                        "Done" -> task.status.name.equals("DONE", ignoreCase = true)
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }
                
                items(filteredTasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                        TaskListItemCard(
                            task = task,
                            onStatusClick = { /* Handle status click later */ },
                            onCardClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }
}
