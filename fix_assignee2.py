with open("app/src/main/java/com/example/ui/components/AssigneePickerBottomSheet.kt", "r") as f:
    content = f.read()

content = content.replace("            },\n            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)\n        ) {", "            }\n        ) {")

with open("app/src/main/java/com/example/ui/components/AssigneePickerBottomSheet.kt", "w") as f:
    f.write(content)
