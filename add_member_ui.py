with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'r') as f:
    content = f.read()

# Add showAddMemberSheet
content = content.replace("var showEditTeamSheet by remember { mutableStateOf(false) }", "var showEditTeamSheet by remember { mutableStateOf(false) }\n    var showAddMemberSheet by remember { mutableStateOf(false) }")

# Add to hasSubDialog
content = content.replace("showCreateListSheet || showEditTeamSheet || showOptionsMenu", "showCreateListSheet || showEditTeamSheet || showAddMemberSheet || showOptionsMenu")
content = content.replace("} else if (showEditTeamSheet) {", "} else if (showAddMemberSheet) {\n                    showAddMemberSheet = false\n                } else if (showEditTeamSheet) {")

# Add DropdownMenuItem
dropdown = """                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.PersonAdd,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    "Add members",
                                                    color = Color.Black,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.width(132.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            showAddMemberSheet = true
                                        },
                                        modifier = Modifier.height(44.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                                    )
"""
content = content.replace('Text(\n                                                    "Edit team",', dropdown + '                                    Text(\n                                                    "Edit team",')

# Add AddMemberBottomSheet component
bottom_sheet = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Add Member",
            color = Color.White,
            fontFamily = SFProDisplayFontFamily,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioraNeonLime,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onAdd(username) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime)
        ) {
            Text("Add", color = Color.Black, fontFamily = SFProDisplayFontFamily)
        }
    }
}
"""

content = content + bottom_sheet

# Call AddMemberBottomSheet
call_sheet = """
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
content = content.replace("if (showCreateListSheet) {", call_sheet + "\n        if (showCreateListSheet) {")

content = content.replace("import androidx.compose.material.icons.rounded.Edit", "import androidx.compose.material.icons.rounded.Edit\nimport androidx.compose.material.icons.rounded.PersonAdd")
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'w') as f:
    f.write(content)
