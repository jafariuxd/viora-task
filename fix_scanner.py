with open("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", "r") as f:
    content = f.read()

content = content.replace("    if (showBottomSheet && scannedUser != null) {\n        ModalBottomSheet(\n    if (showBottomSheet && scannedUser != null) {\n        ModalBottomSheet(\n            sheetState = sheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n            onDismissRequest", 
"    if (showBottomSheet && scannedUser != null) {\n        ModalBottomSheet(\n            sheetState = sheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState, 28.dp),\n            onDismissRequest")

content = content.replace("            containerColor = Color(0xFF1C1C1E),\n            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)", "            containerColor = Color(0xFF1C1C1E)")

with open("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", "w") as f:
    f.write(content)
