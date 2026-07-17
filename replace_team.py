import sys

with open('app/src/main/java/com/example/ui/screens/TeamsScreen.kt', 'r') as f:
    content = f.read()

import_marker = "import androidx.compose.ui.unit.sp"
import_replacement = import_marker + "\nimport com.example.ui.components.DefaultDeadlineSelector"
content = content.replace(import_marker, import_replacement)

# add var customDays by remember { mutableStateOf(3) } in CreateTeamBottomSheet
state_marker = "var deadline by remember { mutableStateOf(\"Weekly\") }"
state_replacement = state_marker + "\n    var customDays by remember { mutableStateOf(3) }"
content = content.replace(state_marker, state_replacement)

start_marker = "// Step 2: Segmented Control"
end_marker = "Spacer(modifier = Modifier.height(32.dp))"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + """com.example.ui.components.DefaultDeadlineSelector(
                    selectedOption = deadline,
                    onOptionSelected = { deadline = it },
                    customDays = customDays,
                    onCustomDaysChanged = { customDays = it },
                    textColor = Color.White,
                    unselectedTextColor = Color.White,
                    borderColor = Color.White,
                    selectedBackgroundColor = Color(0xFF1E3300),
                    selectedItemTextColor = VioraNeonLime,
                    modifier = Modifier.fillMaxWidth()
                )
                """ + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/TeamsScreen.kt', 'w') as f:
        f.write(new_content)
    print("Replaced in TeamsScreen!")
else:
    print("Markers not found in TeamsScreen")
