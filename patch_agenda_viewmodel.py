import re

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "r") as f:
    content = f.read()

old_auth = """                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        try {
                            activity.startIntentSenderForResult(
                                pendingIntent.intentSender,
                                1001,
                                null, 0, 0, 0, null
                            )
                        } catch (e: Exception) {
                            _error.value = "Failed to launch authorization: ${e.message}"
                        }
                    }"""

new_auth = """                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        _authIntent.value = pendingIntent.intentSender
                    }"""

content = content.replace("    private val _error = MutableStateFlow<String?>(null)", "    private val _error = MutableStateFlow<String?>(null)\n    private val _authIntent = MutableStateFlow<android.content.IntentSender?>(null)\n    val authIntent: StateFlow<android.content.IntentSender?> = _authIntent.asStateFlow()")
content = content.replace("    fun authorizeAndFetch(activity: Activity)", "    fun clearAuthIntent() { _authIntent.value = null }\n\n    fun authorizeAndFetch(activity: Activity)")
content = content.replace(old_auth, new_auth)

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "w") as f:
    f.write(content)
