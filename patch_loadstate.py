import re

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

rep = """    fun loadState() {
        val tm = VioraNetworkModule.getTokenManager()
        if (tm == null || tm.getAccessToken().isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            try {"""

content = content.replace('    fun loadState() {\n        viewModelScope.launch {\n            try {', rep, 1)

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
