import re

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "r") as f:
    content = f.read()

import_str = """import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.OAuthCredential"""

content = content.replace(import_str, "")

old_auth = """    private val _error = MutableStateFlow<String?>(null)
    private val _authIntent = MutableStateFlow<android.content.IntentSender?>(null)
    val authIntent: StateFlow<android.content.IntentSender?> = _authIntent.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentAccessToken: String? = null

    fun clearAuthIntent() { _authIntent.value = null }

    fun authorizeAndFetch(activity: Activity) {
        if (currentAccessToken != null) {
            fetchEvents(currentAccessToken!!)
            return
        }

        try {
            val provider = OAuthProvider.newBuilder("google.com")
            provider.addCustomParameter("prompt", "consent")
            provider.setScopes(listOf("https://www.googleapis.com/auth/calendar.readonly"))

            val auth = FirebaseAuth.getInstance()
            
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { authResult ->
                    val credential = authResult.credential as? OAuthCredential
                    val token = credential?.accessToken
                    if (token != null) {
                        currentAccessToken = token
                        fetchEvents(token)
                    } else {
                        _error.value = "Failed to get access token from Firebase"
                    }
                }
                .addOnFailureListener { e ->
                    _error.value = "Firebase Auth Error: ${e.message}"
                }
        } catch (e: Exception) {
            _error.value = "Auth Init Error: ${e.message}"
        }
    }"""

new_auth = """    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _authIntent = MutableStateFlow<android.content.IntentSender?>(null)
    val authIntent: StateFlow<android.content.IntentSender?> = _authIntent.asStateFlow()

    private var currentAccessToken: String? = null

    fun clearAuthIntent() { _authIntent.value = null }

    fun authorizeAndFetch(activity: Activity) {
        if (currentAccessToken != null) {
            fetchEvents(currentAccessToken!!)
            return
        }

        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope("https://www.googleapis.com/auth/calendar.readonly")))
            .build()

        Identity.getAuthorizationClient(activity)
            .authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        _authIntent.value = pendingIntent.intentSender
                    }
                } else {
                    val token = result.accessToken
                    if (token != null) {
                        currentAccessToken = token
                        fetchEvents(token)
                    } else {
                        _error.value = "Failed to get access token."
                    }
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Authorization failed: ${e.message}"
            }
    }"""

content = content.replace(old_auth, new_auth)

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "w") as f:
    f.write(content)
