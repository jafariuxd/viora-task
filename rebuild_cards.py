import sys

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

start_marker = "@Composable\nfun NextTaskCard"
end_marker = "@Composable\nfun VioraBottomNavigation"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Markers not found")
    sys.exit(1)

new_code = """@Composable
fun NextTaskCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit
) {
    TaskListItemCard(
        task = task,
        onStatusClick = onStatusClick,
        onCardClick = onCardClick,
        testTag = "next_task_card"
    )
}

@Composable
fun UnplannedTasksCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("unplanned_tasks_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.List,
                        contentDescription = "Unplanned",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Unplanned tasks",
                    color = Color.Black,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$count",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TaskListItemCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit,
    isOffline: Boolean = false,
    testTag: String = "task_list_item_card_${task.id}"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VioraTaskCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isOverdue = task.daysLeft < 0
                    val isNear = task.daysLeft == 0
                    
                    val timeColor = when {
                        isOverdue -> Color(0xFFD32F2F)
                        isNear -> Color(0xFFF57C00)
                        else -> VioraGrayText
                    }
                    
                    val timeText = when {
                        isOverdue -> "Overdue !"
                        isNear -> "23 hours left"
                        else -> "${task.daysLeft} days left"
                    }
                    
                    Row(
                        modifier = Modifier.background(
                            color = if (task.deadlineSource == com.example.model.DeadlineSource.TEAM || task.deadlineSource == com.example.model.DeadlineSource.LIST) Color.Transparent else Color.Transparent
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isOverdue) {
                            Icon(
                                imageVector = Icons.Rounded.EventBusy,
                                contentDescription = "Overdue",
                                tint = timeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Time Left",
                                tint = timeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = timeText,
                            color = timeColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isOverdue || isNear) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    
                    if (task.deadlineSource == com.example.model.DeadlineSource.TEAM || 
                        task.deadlineSource == com.example.model.DeadlineSource.LIST) {
                        val sourceText = if (task.deadlineSource == com.example.model.DeadlineSource.TEAM) "Team Deadline" else "List Deadline"
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F3F5))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = VioraGrayText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = sourceText,
                                color = VioraGrayText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Group,
                        contentDescription = "Client",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.client,
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Task Title
            Text(
                text = task.title,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dropdown status pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFD6E3FF))
                            .clickable { onStatusClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("status_dropdown_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = task.status.toString(),
                            color = Color(0xFF001B3E),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Select status",
                            tint = Color(0xFF001B3E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (isOffline) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = VioraGrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Team/Assignee indicators
                if (task.assigneePhotos.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, VioraGrayText, RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Unassigned",
                            tint = VioraGrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Unassigned",
                            color = VioraGrayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-10).dp) // overlapping effect
                    ) {
                        val maxAvatars = 3
                        val displayPhotos = task.assigneePhotos.take(maxAvatars)
                        
                        displayPhotos.forEach { photo ->
                            val resId = if (photo == "sara") {
                                R.drawable.img_avatar_sara_1783672418392
                            } else {
                                R.drawable.img_profile_mohammad_1783672402325
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Assignee $photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        if (task.assigneePhotos.size > maxAvatars) {
                            val extra = task.assigneePhotos.size - maxAvatars
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F5))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$extra",
                                    color = VioraGrayText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

"""

content = content[:start_idx] + new_code + content[end_idx:]
with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
print("Replaced successfully")

