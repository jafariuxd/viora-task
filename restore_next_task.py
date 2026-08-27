import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

start_marker = "@Composable\nfun NextTaskCard"
start_idx = content.find(start_marker)

if start_idx != -1:
    next_composable_idx = content.find("@Composable", start_idx + 10)
    if next_composable_idx == -1:
        next_composable_idx = len(content)
        
    new_next_task = """@Composable
fun NextTaskCard(
    task: Task,
    onStatusClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("next_task_card"),
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Time Left",
                        tint = VioraGrayText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${task.daysLeft} days left",
                        color = VioraGrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Business,
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
                            .background(VioraLightBlueBadge)
                            .clickable { onStatusClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("status_dropdown_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "To do", // Default or you can bind to task state
                            color = VioraLightBlueText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Change Status",
                            tint = VioraLightBlueText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Assignee avatars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-8).dp)
                    ) {
                        task.assignees.take(3).forEach { user ->
                            Image(
                                painter = painterResource(id = user.avatarResId),
                                contentDescription = user.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
                
                // Navigate Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VioraNeonLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Go to Task",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
"""
    content = content[:start_idx] + new_next_task + "\n" + content[next_composable_idx:]

    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.write(content)
    print("Replaced NextTaskCard")
else:
    print("Could not find NextTaskCard")
