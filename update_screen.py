import re

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# 1. Add LaunchedEffect for description scroll
scroll_effect_target = """    LaunchedEffect(showSuccessAnim) {
        if (showSuccessAnim) {
            kotlinx.coroutines.delay(1200)
            showSuccessAnim = false
        }
    }"""

scroll_effect_replacement = """    LaunchedEffect(showSuccessAnim) {
        if (showSuccessAnim) {
            kotlinx.coroutines.delay(1200)
            showSuccessAnim = false
        }
    }

    LaunchedEffect(descriptionFocused) {
        if (descriptionFocused) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }"""

content = content.replace(scroll_effect_target, scroll_effect_replacement)

# 2. Replace Description Card
desc_target = """            // Description Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .graphicsLayer { alpha = descriptionAlpha }
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
                        onValueChange = { updateTaskLocal(currentTask.copy(description = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (descriptionFocused && !focusState.isFocused) {
                                    if (currentTask.title.isNotBlank()) {
                                        onTaskSaved(currentTask)
                                    }
                                }
                                descriptionFocused = focusState.isFocused
                            },
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
            }"""

desc_replacement = """            // Description Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .graphicsLayer { alpha = descriptionAlpha }
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (descriptionFocused) 220.dp else 103.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 28.dp)
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
                        onValueChange = { updateTaskLocal(currentTask.copy(description = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = if (descriptionFocused) 120.dp else 40.dp)
                            .onFocusChanged { focusState ->
                                if (descriptionFocused && !focusState.isFocused) {
                                    if (currentTask.title.isNotBlank()) {
                                        onTaskSaved(currentTask)
                                    }
                                }
                                descriptionFocused = focusState.isFocused
                            },
                        cursorBrush = SolidColor(Color.Black),
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
            }"""

content = content.replace(desc_target, desc_replacement)

# 3. Replace Assignee BottomSheet
sheet_target = """    // Assignee Picker Dialog (simplified as a BottomSheet)
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

sheet_replacement = """    // Assignee Picker Dialog (simplified as a BottomSheet)
    if (showAssigneePicker) {
        ModalBottomSheet(
            onDismissRequest = { showAssigneePicker = false },
            containerColor = Color(0xFF1C1C1C),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF2C2C2E))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search users to assign",
                        color = Color(0xFF8E8E93),
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                
                // Users grid (3 equal columns)
                val users = listOf("Delaram", "Mamad", "Mohre", "Noorin", "Sorush", "Tala")
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    users.chunked(3).forEach { rowUsers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            for (i in 0 until 3) {
                                if (i < rowUsers.size) {
                                    val user = rowUsers[i]
                                    val isSelected = currentTask.assigneePhotos.contains(user.lowercase())
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
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
                                        Box(
                                            contentAlignment = Alignment.BottomEnd,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(76.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF636366))
                                            ) {
                                                val resId = when (user.lowercase()) {
                                                    "mohammad" -> R.drawable.img_profile_mohammad_1783672402325
                                                    "sara" -> R.drawable.img_avatar_sara_1783672418392
                                                    "delaram" -> R.drawable.img_profile_mohammad_1783672402325
                                                    "mamad" -> R.drawable.img_profile_mohammad_1783672402325
                                                    "mohre" -> R.drawable.img_profile_mohammad_1783672402325
                                                    "noorin" -> R.drawable.img_avatar_sara_1783672418392
                                                    "sorush" -> R.drawable.img_profile_mohammad_1783672402325
                                                    "tala" -> R.drawable.img_avatar_sara_1783672418392
                                                    else -> R.drawable.img_profile_mohammad_1783672402325
                                                }
                                                Image(
                                                    painter = painterResource(id = resId),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(VioraNeonLime)
                                                        .border(2.dp, Color(0xFF1C1C1C), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = user,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontFamily = SFProDisplayFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
                
                // Sticky Submit button at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = { showAssigneePicker = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Submit",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }
                }
            }
        }
    }"""

content = content.replace(sheet_target, sheet_replacement)

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "w") as f:
    f.write(content)

print("Updated script finished.")
