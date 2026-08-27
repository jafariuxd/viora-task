import re

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

old_auth = """    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {"""

new_auth = """    val error by viewModel.error.collectAsState()
    val authIntent by viewModel.authIntent.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val authResult = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(result.data)
                viewModel.handleAuthorizationResult(authResult.accessToken, null)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                viewModel.handleAuthorizationResult(null, "API Error: ${e.statusCode} - ${e.message}")
            } catch (e: Exception) {
                viewModel.handleAuthorizationResult(null, "Error: ${e.message}")
            }
        } else {
            viewModel.handleAuthorizationResult(null, "Result code was: ${result.resultCode}")
        }
    }

    LaunchedEffect(authIntent) {
        if (authIntent != null) {
            launcher.launch(IntentSenderRequest.Builder(authIntent!!).build())
            viewModel.clearAuthIntent()
        }
    }

    LaunchedEffect(Unit) {"""

content = content.replace(old_auth, new_auth)

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)
