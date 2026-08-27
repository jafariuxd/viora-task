package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthText
import com.example.ui.theme.VioraNeonLime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel,
    onClose: () -> Unit,
    onNavigateToOtp: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    BackHandler { onClose() }
    val email by viewModel.email.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetSuccess()
            onNavigateToOtp()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage?.contains("User already exists") == true) {
            viewModel.clearError()
            onNavigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraAuthBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AuthTopBar(
            onBackOrClose = onClose,
            isClose = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome \uD83D\uDC4B to", // 👋
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = VioraAuthText
            )
            Text(
                text = "VioraTasks",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = VioraAuthText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter your email to get started. If you have an account, you will be asked to log in. Otherwise, we'll send you an OTP to register.",
                fontSize = 16.sp,
                color = VioraAuthGrayText,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it); viewModel.clearError() },
                label = { Text("Your email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioraNeonLime,
                    focusedLabelColor = VioraAuthGrayText,
                    cursorColor = VioraNeonLime,
                    unfocusedTextColor = VioraAuthText,
                    focusedTextColor = VioraAuthText
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                singleLine = true
            )

            if (errorMessage != null && !errorMessage!!.contains("User already exists")) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val isEmailValid = email.isNotBlank() && email.contains("@")
            AuthPrimaryButton(
                text = if (isLoading) "Loading..." else "Next",
                onClick = { viewModel.submitEmail() },
                enabled = isEmailValid && !isLoading
            )
        }
    }
}
