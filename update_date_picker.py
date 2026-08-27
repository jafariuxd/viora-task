import sys

with open('app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

import_statement = "import java.util.UUID"
new_imports = """import java.util.UUID
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat"""
content = content.replace(import_statement, new_imports)

old_options = """                val options = listOf(
                    "Tomorrow" to "1d",
                    "Next 2 days" to "2d",
                    "Next week" to "1w",
                    "Next 2 week" to "2w"
                )
                
                options.forEach { (text, badge) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                updateTask(currentTask.copy(dueDateText = text)) // simplify
                                showDatePicker = false
                            }"""

new_options = """                val options = listOf(
                    Triple("Tomorrow", "1d", 1),
                    Triple("Next 2 days", "2d", 2),
                    Triple("Next week", "1w", 7),
                    Triple("Next 2 weeks", "2w", 14)
                )
                
                options.forEach { (text, badge, days) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, days)
                                val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                                val formattedDate = dateFormat.format(calendar.time)
                                updateTask(currentTask.copy(specificDeadlineDays = days, dueDateText = formattedDate))
                                showDatePicker = false
                            }"""

if old_options in content:
    content = content.replace(old_options, new_options)
    with open('app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Could not find old options block")
