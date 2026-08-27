with open("app/src/main/java/com/example/ui/screens/VioraPassScreen.kt", "r") as f:
    content = f.read()

content = content.replace("        scrimColor = Color.Black.copy(alpha = 0.6f),\n        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),", "        scrimColor = Color.Black.copy(alpha = 0.6f),")

with open("app/src/main/java/com/example/ui/screens/VioraPassScreen.kt", "w") as f:
    f.write(content)
