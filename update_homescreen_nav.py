import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Update HomeScreen signature
old_sig = """fun HomeScreen(
    viewModel: VioraTaskViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {}
) {"""

new_sig = """fun HomeScreen(
    viewModel: VioraTaskViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToList: (String, String) -> Unit = { _, _ -> }
) {"""
content = content.replace(old_sig, new_sig)

# Update UnplannedTasksCard click handler
old_click = """                                        onClick = {
                                            Toast.makeText(context, "Reviewing unplanned tasks...", Toast.LENGTH_SHORT).show()
                                        }"""

new_click = """                                        onClick = {
                                            onNavigateToList("Unplanned Tasks", "Personal Space")
                                        }"""
content = content.replace(old_click, new_click)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
