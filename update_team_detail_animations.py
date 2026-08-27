with open("app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt", "r") as f:
    content = f.read()

# Make sure animateEnter is imported
if "import com.example.ui.utils.animateEnter" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport com.example.ui.utils.animateEnter")

# Top Bar
target_top_bar = """            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),"""
rep_top_bar = """            // Top Bar
            Row(
                modifier = Modifier
                    .animateEnter(delayMillis = 0)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),"""
content = content.replace(target_top_bar, rep_top_bar)

with open("app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt", "w") as f:
    f.write(content)
