import re

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

old_launcher = """    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val authResult = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(result.data)
            viewModel.handleAuthorizationResult(authResult.accessToken)
        } else {
            viewModel.handleAuthorizationResult(null)
        }
    }"""

new_launcher = """    val launcher = rememberLauncherForActivityResult(
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
    }"""
content = content.replace(old_launcher, new_launcher)
with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)
