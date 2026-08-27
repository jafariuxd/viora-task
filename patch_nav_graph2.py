import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

old_home = """            HomeScreen(
                viewModel = viewModel,
                modifier = Modifier,
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("user_profile") },
                onNavigateToAgenda = { navController.navigate("agenda") }
            )"""

new_home = """            HomeScreen(
                viewModel = viewModel,
                modifier = Modifier,
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("user_profile") },
                onNavigateToAgenda = { navController.navigate("agenda") },
                onNavigateToList = { listName, teamName -> 
                    navController.navigate("list_detail/$listName/$teamName")
                }
            )"""

content = content.replace(old_home, new_home)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
