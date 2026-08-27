import re

def update_file(filename, state_inits, replacements):
    with open(filename, 'r') as f:
        content = f.read()
    
    # 1. Add state inits if needed before ModalBottomSheet
    for init_target, init_text in state_inits.items():
        if init_text not in content:
            content = content.replace(init_target, init_text + "\n" + init_target)
            
    # 2. Add or replace shape parameter and ensure sheetState is passed
    for target, replacement in replacements.items():
        content = content.replace(target, replacement)
        
    with open(filename, 'w') as f:
        f.write(content)

# 1. TeamComponents.kt
update_file("app/src/main/java/com/example/ui/components/TeamComponents.kt", {}, {
    "        sheetState = sheetState,\n": "        sheetState = sheetState,\n        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n"
})

# 2. AssigneePickerBottomSheet.kt
update_file("app/src/main/java/com/example/ui/components/AssigneePickerBottomSheet.kt", {
    "    ModalBottomSheet(": "    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)\n    ModalBottomSheet("
}, {
    "    ModalBottomSheet(\n        onDismissRequest": "    ModalBottomSheet(\n        sheetState = sheetState,\n        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n        onDismissRequest"
})

# 3. ScannerCameraOverlay.kt
update_file("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", {
    "    if (showBottomSheet && scannedUser != null) {\n        ModalBottomSheet(": "    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)\n    if (showBottomSheet && scannedUser != null) {\n        ModalBottomSheet("
}, {
    "        ModalBottomSheet(\n            onDismissRequest": "        ModalBottomSheet(\n            sheetState = sheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n            onDismissRequest"
})

# 4. VioraPassScreen.kt
update_file("app/src/main/java/com/example/ui/screens/VioraPassScreen.kt", {}, {
    "        sheetState = sheetState,\n        containerColor": "        sheetState = sheetState,\n        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n        containerColor"
})

# 5. ListDetailScreen.kt
update_file("app/src/main/java/com/example/ui/screens/ListDetailScreen.kt", {}, {
    "        sheetState = sheetState,\n        containerColor": "        sheetState = sheetState,\n        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n        containerColor",
    "shape = androidx.compose.ui.graphics.RectangleShape,": ""
})

# 6. TeamDetailScreen.kt
update_file("app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt", {}, {
    "        sheetState = sheetState,\n        containerColor": "        sheetState = sheetState,\n        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),\n        containerColor",
    "        shape = androidx.compose.ui.graphics.RectangleShape,\n": "",
    "        sheetState = addSheetState,\n        containerColor": "        sheetState = addSheetState,\n        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = addSheetState, defaultRadius = 28.dp),\n        containerColor",
    "        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)\n": "\n",
    "        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)": "\n"
})

# 7. TaskDetailScreen.kt
# Needs state inits for each ModalBottomSheet
with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", 'r') as f:
    content = f.read()

# Folder Picker
if "val folderSheetState" not in content:
    content = content.replace("        ModalBottomSheet(\n            onDismissRequest = { showFolderPicker = false },", "        val folderSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)\n        ModalBottomSheet(\n            sheetState = folderSheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = folderSheetState, defaultRadius = 28.dp),\n            onDismissRequest = { showFolderPicker = false },")

# Deadline Options
if "val deadlineSheetState" not in content:
    content = content.replace("        ModalBottomSheet(\n            onDismissRequest = { showDeadlineOptionsSheet = false },", "        val deadlineSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)\n        ModalBottomSheet(\n            sheetState = deadlineSheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = deadlineSheetState, defaultRadius = 28.dp),\n            onDismissRequest = { showDeadlineOptionsSheet = false },")

# Skip Time
if "val skipTimeSheetState" not in content:
    content = content.replace("        ModalBottomSheet(\n            onDismissRequest = performSkipTime,", "        val skipTimeSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)\n        ModalBottomSheet(\n            sheetState = skipTimeSheetState,\n            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = skipTimeSheetState, defaultRadius = 28.dp),\n            onDismissRequest = performSkipTime,")

# Tag Input
if "val tagSheetState" not in content:
    content = content.replace("            ModalBottomSheet(\n                onDismissRequest = { showTagInput = false },", "            val tagSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)\n            ModalBottomSheet(\n                sheetState = tagSheetState,\n                shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = tagSheetState, defaultRadius = 28.dp),\n                onDismissRequest = { showTagInput = false },")

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", 'w') as f:
    f.write(content)

print("Done")
