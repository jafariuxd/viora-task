with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

new_launcher = """    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleAuthorizationResult(context, result.data)
        } else {
            viewModel.setError("Google Sign-In failed or was canceled. (Code: ${result.resultCode})\\nPlease make sure you have added your app's SHA-1 to your Google Cloud Console / Firebase project.")
        }
    }"""

import re
content = re.sub(r"    val launcher = rememberLauncherForActivityResult[\s\S]*?    }", new_launcher, content)

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "r") as f:
    vm_content = f.read()

vm_content = vm_content.replace("fun clearAuthIntent() { _authIntent.value = null }", "fun clearAuthIntent() { _authIntent.value = null }\n    fun setError(msg: String) { _error.value = msg }")

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "w") as f:
    f.write(vm_content)
