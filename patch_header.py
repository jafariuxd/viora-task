with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun HeaderSection(\n    userName: String,", "fun HeaderSection(\n    userName: String,\n    onVioraPassClick: () -> Unit = {},")

content = content.replace("            HeaderSection(\n                userName = userName,", "            HeaderSection(\n                userName = userName,\n                onVioraPassClick = onVioraPassClick,")

content = content.replace("fun HomeScreen(\n    onVioraPassClick: () -> Unit = {},", "fun HomeScreen(\n    onVioraPassClick: () -> Unit = {},")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
