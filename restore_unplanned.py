import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

start_marker = "@Composable\nfun UnplannedTasksCard"
start_idx = content.find(start_marker)

if start_idx != -1:
    next_composable_idx = content.find("@Composable", start_idx + 10)
    if next_composable_idx == -1:
        next_composable_idx = len(content)
        
    new_unplanned_card = """@Composable
fun UnplannedTasksCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clickable { onClick() }
            .testTag("unplanned_tasks_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VioraUnplannedCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.List,
                        contentDescription = "Unplanned",
                        tint = VioraUnplannedCard,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = count.toString(),
                    color = Color.Black,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unplanned\ntasks",
                    color = Color.Black.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
"""
    content = content[:start_idx] + new_unplanned_card + "\n" + content[next_composable_idx:]

    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.write(content)
    print("Replaced UnplannedTasksCard")
else:
    print("Could not find UnplannedTasksCard")
