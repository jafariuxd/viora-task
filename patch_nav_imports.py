with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

import_str = "import com.example.ui.screens.TaskDetailScreen\nimport com.example.ui.screens.VioraPassScreen\nimport com.example.ui.screens.ScannerCameraOverlay\nimport androidx.navigation.compose.NavHost\n"
content = content.replace("import androidx.navigation.compose.NavHost", import_str)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
