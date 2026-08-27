import re

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "r") as f:
    content = f.read()

target = """                // Users grid (mock)
                val users = listOf("Delaram", "Mamad", "Mohre", "Noorin", "Sorush", "Tala")
                @OptIn(ExperimentalLayoutApi::class)
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    FlowRow(
                        maxItemsInEachRow = 3,
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    users.forEach { user ->
                        val isSelected = currentTask.assigneePhotos.contains(user.lowercase())
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
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
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(VioraNeonLime),
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
                }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { showAssigneePicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }"""

replacement = """                // Users grid (mock)
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
                }"""

new_content = content.replace(target, replacement)
if new_content == content:
    print("No change")
else:
    with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "w") as f:
        f.write(new_content)
    print("Changed")
