import sys

with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'r') as f:
    content = f.read()

import_marker = "import androidx.compose.ui.unit.sp"
import_replacement = import_marker + "\nimport com.example.ui.components.DefaultDeadlineSelector"
content = content.replace(import_marker, import_replacement)

# add var customDays by remember { mutableStateOf(3) } in EditProfileScreen
state_marker = "var defaultDeadline by remember { mutableStateOf(\"Monthly\") }"
state_replacement = state_marker + "\n    var customDays by remember { mutableStateOf(3) }"
content = content.replace(state_marker, state_replacement)

start_marker = "// Segmented Control"
end_marker = "Spacer(modifier = Modifier.height(16.dp))"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + """com.example.ui.components.DefaultDeadlineSelector(
                selectedOption = defaultDeadline,
                onOptionSelected = { defaultDeadline = it },
                customDays = customDays,
                onCustomDaysChanged = { customDays = it },
                textColor = Color.White,
                unselectedTextColor = Color.White,
                borderColor = Color.White,
                selectedBackgroundColor = VioraNeonLime,
                selectedItemTextColor = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            
            """ + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'w') as f:
        f.write(new_content)
    print("Replaced in EditProfileScreen!")
else:
    print("Markers not found in EditProfileScreen")
