import re

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "private val _scannedUser = MutableStateFlow<com.example.model.viora.UserResponseDto?>" in line:
        if any("private val _scannedUser = MutableStateFlow" in l for l in new_lines):
            continue
    if "val scannedUser: StateFlow<com.example.model.viora.UserResponseDto?>" in line:
        if any("val scannedUser: StateFlow" in l for l in new_lines):
            continue
    if "private val _isScannedUserLoading = MutableStateFlow(false)" in line:
        if any("private val _isScannedUserLoading" in l for l in new_lines):
            continue
    if "val isScannedUserLoading: StateFlow<Boolean>" in line:
        if any("val isScannedUserLoading: StateFlow" in l for l in new_lines):
            continue
    if "val fullTeamsList: List<com.example.model.Team>" in line:
        if any("val fullTeamsList: List<com.example.model.Team>" in l for l in new_lines):
            skip = True
            continue
    if skip and "get() = mockTeams.values.toList()" in line:
        skip = False
        continue
    
    new_lines.append(line)

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
    f.writelines(new_lines)
