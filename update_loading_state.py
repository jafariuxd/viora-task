with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    content = f.read()

target = """    // Weather Info state"""
replacement = """    private val _isDashboardLoading = MutableStateFlow(true)
    val isDashboardLoading: StateFlow<Boolean> = _isDashboardLoading.asStateFlow()

    init {
        androidx.lifecycle.viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            _isDashboardLoading.value = false
        }
    }

    // Weather Info state"""

if "val isDashboardLoading" not in content:
    content = content.replace(target, replacement)

    with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
        f.write(content)
    print("Added isDashboardLoading to VioraTaskViewModel.kt")
else:
    print("isDashboardLoading already exists")
