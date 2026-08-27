import re

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "r") as f:
    content = f.read()

target = """    // Assignee Picker Dialog (simplified as a BottomSheet)
    if (showAssigneePicker) {
        ModalBottomSheet(
            onDismissRequest = { showAssigneePicker = false },
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
                    Text("Search users to assign", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Users grid (mock)
                val users = listOf("Delaram", "Mamad", "Mohre", "Noorin", "Sorush", "Tala")
                
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    users.chunked(3).forEach { rowUsers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowUsers.forEach { user ->
                                val isSelected = currentTask.assigneePhotos.contains(user.lowercase())
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
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
                                            val resId = when (user.lowercase()) {
                                                "mohammad" -> R.drawable.img_profile_mohammad_1783672402325
                                                "sara" -> R.drawable.img_avatar_sara_1783672418392
                                                "delaram" -> R.drawable.img_profile_mohammad_1783672402325 // fallback
                                                "tala" -> R.drawable.img_avatar_sara_1783672418392 // fallback
                                                else -> null
                                            }
                                            if (resId != null) {
                                                Image(
                                                    painter = painterResource(id = resId),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(VioraNeonLime)
                                                    .border(2.dp, Color(0xFF2A2A2A), CircleShape),
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
                            // Add empty spacers to keep alignment
                            if (rowUsers.size < 3) {
                                repeat(3 - rowUsers.size) {
                                    Spacer(modifier = Modifier.width(72.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
    }"""

replacement = """    // Assignee Picker Dialog (simplified as a BottomSheet)
    if (showAssigneePicker) {
        ModalBottomSheet(
            onDismissRequest = { showAssigneePicker = false },
            containerColor = Color(0xFF1E1E1E), // Darker gray based on image
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) },
            modifier = Modifier.fillMaxHeight(0.8f) // Ensures fixed height for scrollable content and sticky button
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2C2C2C))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search users to assign", color = Color.Gray, fontSize = 16.sp, fontFamily = SFProDisplayFontFamily)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Users grid (mock)
                val users = listOf("Delaram", "Mamad", "Mohre", "Noorin", "Sorush", "Tala")
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    users.chunked(3).forEach { rowUsers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowUsers.forEach { user ->
                                val isSelected = currentTask.assigneePhotos.contains(user.lowercase())
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val newAssignees = if (isSelected) {
                                            currentTask.assigneePhotos - user.lowercase()
                                        } else {
                                            currentTask.assigneePhotos + user.lowercase()
                                        }
                                        updateTask(currentTask.copy(assigneePhotos = newAssignees))
                                    }
                                ) {
                                    Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF757575))
                                        ) {
                                            // Normally image here
                                            val resId = when (user.lowercase()) {
                                                "mohammad" -> R.drawable.img_profile_mohammad_1783672402325
                                                "sara" -> R.drawable.img_avatar_sara_1783672418392
                                                "delaram" -> R.drawable.img_profile_mohammad_1783672402325 // fallback
                                                "tala" -> R.drawable.img_avatar_sara_1783672418392 // fallback
                                                else -> null
                                            }
                                            if (resId != null) {
                                                Image(
                                                    painter = painterResource(id = resId),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(VioraNeonLime)
                                                    .border(2.dp, Color(0xFF1E1E1E), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Text(user, color = Color.White, fontSize = 15.sp, fontFamily = SFProDisplayFontFamily)
                                }
                            }
                            // Add empty spacers to keep alignment
                            if (rowUsers.size < 3) {
                                repeat(3 - rowUsers.size) {
                                    Spacer(modifier = Modifier.width(80.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = { showAssigneePicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Submit", fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                    }
                }
            }
        }
    }"""

new_content = content.replace(target, replacement)
if new_content == content:
    print("No change")
else:
    with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "w") as f:
        f.write(new_content)
    print("Changed")
