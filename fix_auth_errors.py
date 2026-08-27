import re

with open('app/src/main/java/com/example/viewmodel/AuthViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('_errorMessage.value = e.message ?: "Failed to request OTP"', '_errorMessage.value = com.example.util.ErrorUtil.getErrorMessage(e)')
content = content.replace('_errorMessage.value = "Login failed: ${e.message}"', '_errorMessage.value = "Login failed: ${com.example.util.ErrorUtil.getErrorMessage(e)}"')
content = content.replace('_errorMessage.value = "Verification failed: ${e.message}"', '_errorMessage.value = "Verification failed: ${com.example.util.ErrorUtil.getErrorMessage(e)}"')

with open('app/src/main/java/com/example/viewmodel/AuthViewModel.kt', 'w') as f:
    f.write(content)
