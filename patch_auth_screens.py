import re

# Patch DeadlineScreen
with open('app/src/main/java/com/example/ui/screens/auth/DeadlineScreen.kt', 'r') as f:
    deadline_content = f.read()

deadline_obs = """    val selectedOption by viewModel.defaultDeadline.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            viewModel.resetSuccess()
            onNext()
        }
    }"""
deadline_content = re.sub(r'    val selectedOption by viewModel\.defaultDeadline\.collectAsStateWithLifecycle\(\)', deadline_obs, deadline_content, count=1)

deadline_btn = """            rightAction = {
                AuthTopRightButton(text = if (isLoading) "Loading..." else "Let's Go!", onClick = { viewModel.verifyOtpAndRegister() })
            }"""
deadline_content = re.sub(r'            rightAction = \{.*?\n            \}', deadline_btn, deadline_content, flags=re.DOTALL)

deadline_err = """            com.example.ui.components.DefaultDeadlineSelector(
                selectedOption = selectedOption,
                onOptionSelected = { viewModel.updateDefaultDeadline(it) },
                customDays = customDays,
                onCustomDaysChanged = { viewModel.updateCustomDays(it) },
                textColor = VioraAuthText,
                unselectedTextColor = VioraAuthGrayText,
                borderColor = VioraAuthBorder,
                selectedBackgroundColor = VioraNeonLime,
                selectedItemTextColor = VioraAuthText,
                modifier = Modifier.fillMaxWidth()
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }"""
deadline_content = re.sub(r'            com\.example\.ui\.components\.DefaultDeadlineSelector\(.*?\)', deadline_err, deadline_content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/auth/DeadlineScreen.kt', 'w') as f:
    f.write(deadline_content)

# Patch OtpScreen
with open('app/src/main/java/com/example/ui/screens/auth/OtpScreen.kt', 'r') as f:
    otp_content = f.read()

otp_content = otp_content.replace('val otpLength = 5', 'val otpLength = 6')
otp_content = otp_content.replace('val isOtpCorrect = otpValue == "11111"', 'val isOtpCorrect = otpValue.length == 6')

with open('app/src/main/java/com/example/ui/screens/auth/OtpScreen.kt', 'w') as f:
    f.write(otp_content)

# Patch NavGraph for DeadlineScreen
with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    nav_content = f.read()

nav_rep = """                onNext = { 
                    viewModel.loadState()
                    userProfileViewModel.loadProfile()
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }"""
nav_content = re.sub(r'                onNext = \{.*?\}\n                \}', nav_rep, nav_content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(nav_content)

