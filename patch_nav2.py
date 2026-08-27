with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

nav_str = """
            HomeScreen(
                onVioraPassClick = { navController.navigate("viora_pass") },
"""

content = content.replace("            HomeScreen(", nav_str)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
