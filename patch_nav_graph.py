import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

import_statement = "import com.example.ui.screens.ListDetailScreen"
if import_statement not in content:
    content = content.replace("import com.example.ui.screens.SearchScreen", import_statement + "\nimport com.example.ui.screens.SearchScreen")

nav_type_import = "import androidx.navigation.NavType\nimport androidx.navigation.navArgument"
if "androidx.navigation.NavType" not in content:
    content = content.replace("import androidx.navigation.compose.rememberNavController", nav_type_import + "\nimport androidx.navigation.compose.rememberNavController")

list_detail_route = """        composable(
            route = "list_detail/{listName}/{teamName}",
            arguments = listOf(
                navArgument("listName") { type = NavType.StringType },
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listName = backStackEntry.arguments?.getString("listName") ?: ""
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            ListDetailScreen(
                listName = listName,
                teamName = teamName,
                onBack = { navController.popBackStack() },
                onTaskClick = { /* Can handle clicks here */ },
                viewModel = viewModel
            )
        }
        
        composable("agenda") {"""
        
content = content.replace('        composable("agenda") {', list_detail_route)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
