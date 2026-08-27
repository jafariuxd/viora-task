import re

with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "if (showAddMemberSheet) {" in line and "onClick" not in line:
        skip = True
    
    if skip and "}" in line:
        # Check if we should stop skipping. It's tricky to count braces by lines.
        pass

# Let's use regex on the full string instead.
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'r') as f:
    content = f.read()

# Just remove all occurrences of the if (showAddMemberSheet) block.
pattern = r'[ \t]*if \(showAddMemberSheet\) \{[ \t\n]*ModalBottomSheet\([^)]*\)[ \t\n]*\{[ \t\n]*AddMemberBottomSheet\([^)]*\)[ \t\n]*\}[ \t\n]*\}'
content = re.sub(pattern, "", content)

# Then insert it once at the end of the TeamDetailScreen function.
# Look for the end of TeamDetailScreen. It ends right before AddMemberBottomSheet.
end_idx = content.find("@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun AddMemberBottomSheet")

if end_idx != -1:
    good_block = """
    if (showAddMemberSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddMemberSheet = false },
            containerColor = Color(0xFF1C1C1E),
        ) {
            AddMemberBottomSheet(
                onDismiss = { showAddMemberSheet = false },
                onAdd = { username ->
                    viewModel.updateTeamMembers(teamName, listOf(username), null)
                    showAddMemberSheet = false
                }
            )
        }
    }
"""
    content = content[:end_idx] + good_block + content[end_idx:]

with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'w') as f:
    f.write(content)
