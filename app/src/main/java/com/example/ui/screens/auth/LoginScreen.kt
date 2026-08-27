package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthText
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    BackHandler { onBack() }
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraAuthBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AuthTopBar(onBackOrClose = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = VioraAuthText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = email,
                fontSize = 16.sp,
                color = VioraAuthGrayText
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
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

            if (errorMessage != null) {
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
            AuthPrimaryButton(
                text = if (isLoading) "Logging in..." else "Log In",
                onClick = { viewModel.login() },
                enabled = password.isNotBlank() && !isLoading
            )
        }
    }
}
