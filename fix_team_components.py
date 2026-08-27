with open("app/src/main/java/com/example/ui/components/TeamComponents.kt", "r") as f:
    content = f.read()

content = content.replace("        dragHandle = null,\n        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)", "        dragHandle = null")

with open("app/src/main/java/com/example/ui/components/TeamComponents.kt", "w") as f:
    f.write(content)
