import re

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "r") as f:
    content = f.read()

old_handle = """    fun handleAuthorizationResult(token: String?) {
        if (token != null) {
            currentAccessToken = token
            fetchEvents(token)
        } else {
            _error.value = "Authorization cancelled or failed."
        }
    }"""

new_handle = """    fun handleAuthorizationResult(token: String?, errorMsg: String? = null) {
        if (token != null) {
            currentAccessToken = token
            fetchEvents(token)
        } else {
            _error.value = "Authorization failed: ${errorMsg ?: "Unknown"}"
        }
    }"""
content = content.replace(old_handle, new_handle)
with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "w") as f:
    f.write(content)
