with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

import_str = "import com.example.ui.screens.TaskDetailScreen\nimport com.example.ui.screens.VioraPassScreen\nimport com.example.ui.screens.ScannerCameraOverlay"
content = content.replace("import com.example.ui.screens.TaskDetailScreen", import_str)

routes_str = """
        composable("viora_pass") {
            VioraPassScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate("scanner_camera") }
            )
        }
        composable("scanner_camera") {
            ScannerCameraOverlay(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("daily_brief") {
"""
content = content.replace("        composable(\"daily_brief\") {", routes_str)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
