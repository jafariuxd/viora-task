import re

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    content = f.read()

qr_props = """
    private val _scannedUser = MutableStateFlow<com.example.model.viora.UserResponseDto?>(null)
    val scannedUser: StateFlow<com.example.model.viora.UserResponseDto?> = _scannedUser.asStateFlow()
    
    private val _isScannedUserLoading = MutableStateFlow(false)
    val isScannedUserLoading: StateFlow<Boolean> = _isScannedUserLoading.asStateFlow()
    
    val fullTeamsList: List<com.example.model.Team>
        get() = mockTeams.values.toList()
"""

qr_funcs = """
    fun fetchScannedUser(userId: String) {
        viewModelScope.launch {
            _isScannedUserLoading.value = true
            try {
                val res = com.example.network.VioraNetworkModule.api.getUserProfile(userId)
                if (res.success && res.data != null) {
                    _scannedUser.value = res.data
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            } finally {
                _isScannedUserLoading.value = false
            }
        }
    }

    fun addScannedUserToTeam(teamId: String, username: String) {
        viewModelScope.launch {
            try {
                val req = com.example.model.viora.UpdateTeamDto(addUsernames = listOf(username))
                val res = com.example.network.VioraNetworkModule.api.updateTeam(teamId, req)
                if (res.success) {
                    showMessage("Member recruited successfully")
                    _scannedUser.value = null
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun clearScannedUser() {
        _scannedUser.value = null
    }
"""

if "fetchScannedUser" not in content:
    content = content.replace("class VioraTaskViewModel(application: Application) : AndroidViewModel(application) {", "class VioraTaskViewModel(application: Application) : AndroidViewModel(application) {\n" + qr_props + "\n" + qr_funcs)

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
    f.write(content)
