import os

files_to_fix = [
    "app/src/main/java/com/example/ui/components/TeamComponents.kt",
    "app/src/main/java/com/example/ui/components/AssigneePickerBottomSheet.kt",
    "app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt",
    "app/src/main/java/com/example/ui/screens/VioraPassScreen.kt",
    "app/src/main/java/com/example/ui/screens/ListDetailScreen.kt",
    "app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt",
    "app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt"
]

for file in files_to_fix:
    with open(file, 'r') as f:
        content = f.read()
    
    # We want to replace `ModalBottomSheet(... ) {` with `ModalBottomSheet(... ) { \ncom.example.ui.utils.ConfigureBottomSheetWindow()`
    # But some might have nested braces. 
    # Let's just find `contentWindowInsets` if it exists.
    # Actually, a simple regex or string search is enough.
    
    lines = content.split('\n')
    new_lines = []
    in_modal = False
    
    for line in lines:
        new_lines.append(line)
        if "ModalBottomSheet(" in line:
            in_modal = True
        
        if in_modal and line.strip().endswith(") {"):
            new_lines.append("        com.example.ui.utils.ConfigureBottomSheetWindow()")
            in_modal = False
            
    with open(file, 'w') as f:
        f.write('\n'.join(new_lines))

