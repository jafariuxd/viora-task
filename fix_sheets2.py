import re
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

def add_configure(match):
    # match.group(0) is the full match, e.g. "ModalBottomSheet(...) {"
    return match.group(0) + "\n        com.example.ui.utils.ConfigureBottomSheetWindow()"

for file in files_to_fix:
    with open(file, 'r') as f:
        content = f.read()
    
    # We want to match `ModalBottomSheet( ... ) {` where ... can be multiple lines.
    # We use a regex that matches `ModalBottomSheet` followed by anything (non-greedy) until `) {`
    new_content = re.sub(r'ModalBottomSheet\s*\([^)]*\)\s*\{', add_configure, content, flags=re.MULTILINE)
    
    # But wait, `[^)]*` fails if there are nested parentheses (like `onDismissRequest = { ... }`)
    
