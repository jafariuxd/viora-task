with open("app/src/main/java/com/example/ui/components/AssigneePickerBottomSheet.kt", "r") as f:
    content = f.read()

content = content.replace("    ModalBottomSheet(\n    ModalBottomSheet(\n            onDismissRequest", 
"        ModalBottomSheet(\n            sheetState = sheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState, 28.dp),\n            onDismissRequest")

with open("app/src/main/java/com/example/ui/components/AssigneePickerBottomSheet.kt", "w") as f:
    f.write(content)
