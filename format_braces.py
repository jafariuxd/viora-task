import re

with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'r') as f:
    content = f.read()

bad = """                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
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
    }
}
@OptIn(ExperimentalMaterial3Api::class)"""

good = """                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
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
    }
}
@OptIn(ExperimentalMaterial3Api::class)"""

if bad in content:
    content = content.replace(bad, good)
else:
    print("Not matched exactly. Trying regex.")
    
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'w') as f:
    f.write(content)
