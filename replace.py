import sys

with open('app/src/main/java/com/example/ui/screens/auth/DeadlineScreen.kt', 'r') as f:
    content = f.read()

start_marker = "// Segmented Control"
end_marker = "}\n        }\n    }\n}"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + """val customDays by viewModel.customDays.collectAsStateWithLifecycle()
            com.example.ui.components.DefaultDeadlineSelector(
                selectedOption = selectedOption,
                onOptionSelected = { viewModel.updateDefaultDeadline(it) },
                customDays = customDays,
                onCustomDaysChanged = { viewModel.updateCustomDays(it) },
                textColor = VioraAuthText,
                unselectedTextColor = VioraAuthGrayText,
                borderColor = VioraAuthBorder,
                selectedBackgroundColor = VioraNeonLime,
                selectedItemTextColor = VioraAuthText,
                modifier = Modifier.fillMaxWidth()
            )
""" + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/auth/DeadlineScreen.kt', 'w') as f:
        f.write(new_content)
    print("Replaced!")
else:
    print("Markers not found")
