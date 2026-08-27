with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun HomeScreen(", "fun HomeScreen(\n    onVioraPassClick: () -> Unit = {},")

content = content.replace("            HomeScreenTopBar(", "            HomeScreenTopBar(\n                onVioraPassClick = onVioraPassClick,")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
