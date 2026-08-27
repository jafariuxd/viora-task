with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# Make sure animateEnter is imported
if "import com.example.ui.utils.animateEnter" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport com.example.ui.utils.animateEnter")

# Top Bar
target_top_bar = """            // Top Bar
            Row(
                modifier = Modifier
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),"""
rep_top_bar = """            // Top Bar
            Row(
                modifier = Modifier
                    .animateEnter(delayMillis = 0)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),"""
content = content.replace(target_top_bar, rep_top_bar)

# Task Title Card
target_title_card = """            // Task Title Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .graphicsLayer { alpha = titleAlpha }
                    .fillMaxWidth()
            ) {"""
rep_title_card = """            // Task Title Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .animateEnter(delayMillis = 50)
                    .graphicsLayer { alpha = titleAlpha }
                    .fillMaxWidth()
            ) {"""
content = content.replace(target_title_card, rep_title_card)

# Status Row
target_status_row = """            // Status Row
            Row(
                modifier = Modifier
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth(),"""
rep_status_row = """            // Status Row
            Row(
                modifier = Modifier
                    .animateEnter(delayMillis = 100)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth(),"""
content = content.replace(target_status_row, rep_status_row)

# Due Date & Assignees grid
target_due_date_grid = """            // Due Date & Assignees grid
            Row(
                modifier = Modifier
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth(),"""
rep_due_date_grid = """            // Due Date & Assignees grid
            Row(
                modifier = Modifier
                    .animateEnter(delayMillis = 150)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth(),"""
content = content.replace(target_due_date_grid, rep_due_date_grid)

# Tags Card
target_tags_card = """            // Tags Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth()"""
rep_tags_card = """            // Tags Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .animateEnter(delayMillis = 200)
                    .graphicsLayer { alpha = otherAlpha }
                    .fillMaxWidth()"""
content = content.replace(target_tags_card, rep_tags_card)

# Description Card
target_desc_card = """            // Description Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .graphicsLayer { alpha = descriptionAlpha }
                    .fillMaxWidth()"""
rep_desc_card = """            // Description Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .animateEnter(delayMillis = 250)
                    .graphicsLayer { alpha = descriptionAlpha }
                    .fillMaxWidth()"""
content = content.replace(target_desc_card, rep_desc_card)

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "w") as f:
    f.write(content)
