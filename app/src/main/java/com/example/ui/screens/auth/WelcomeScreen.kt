package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthErrorBanner
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthBorderFocused
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthSurface
import com.example.ui.components.VioraAuthText
import com.example.ui.components.VioraAuthTextField
import com.example.ui.components.VioraAuthTextSecondary
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.autofill
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
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
        if (errorMessage?.contains("User already exists", ignoreCase = true) == true) {
            viewModel.clearError()
            onNavigateToLogin()
        }
    }

    // UX email pattern check
    val emailPattern = remember { Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") }
    val isEmailFormatValid = remember(email) { email.isNotBlank() && emailPattern.matches(email.trim()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraAuthBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AuthTopBar(
            onBackOrClose = onClose,
            showLeftButton = false,
            currentStep = 1,
            totalSteps = 4
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome 👋",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF8E8E93),
                letterSpacing = (-0.5).sp
            )
            
            Text(
                text = "Let's get started",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.8).sp,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter your work or personal email to create an account or sign in to your workspace seamlessly.",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Custom Styled TextField
            VioraAuthTextField(
                value = email,
                onValueChange = { 
                    viewModel.updateEmail(it)
                    viewModel.clearError() 
                },
                label = "Work or Personal Email",
                placeholder = "name@example.com",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = if (isEmailFormatValid) VioraNeonLime else Color(0xFF5E6064),
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.updateEmail("")
                                viewModel.clearError()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear email",
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (isEmailFormatValid && !isLoading) {
                            viewModel.submitEmail()
                        }
                    }
                ),
                modifier = Modifier.autofill(
                    autofillTypes = listOf(AutofillType.EmailAddress, AutofillType.Username),
                    onFill = {
                        viewModel.updateEmail(it)
                        viewModel.clearError()
                    }
                )
            )

            if (errorMessage != null && !errorMessage!!.contains("User already exists", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(16.dp))
                AuthErrorBanner(message = errorMessage!!)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            AuthPrimaryButton(
                text = if (isLoading) "Checking account..." else "Continue with Email",
                onClick = { viewModel.submitEmail() },
                enabled = isEmailFormatValid && !isLoading,
                isLoading = isLoading
            )
        }
    }
}

