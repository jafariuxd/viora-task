package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.example.ui.components.AuthErrorBanner
import com.example.ui.components.AuthInfoBanner
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthSurface
import com.example.ui.components.VioraAuthText
import com.example.ui.components.VioraAuthIdentityCard
import com.example.ui.components.VioraAuthTextField
import com.example.ui.components.VioraAuthTextSecondary
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.util.autofill
import com.example.util.AutofillUtil
import androidx.compose.ui.platform.LocalContext
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
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
    val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            AutofillUtil.commitAutofill(context)
            delay(800)
            onLoginSuccess()
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(28.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF2C2C2E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = VioraNeonLime,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Reset Password",
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "We'll send a 6-digit one-time code and instructions to verify your account and set a new password.",
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2C2C2E),
                        border = BorderStroke(1.dp, Color(0xFF3C3C3E))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = email,
                                fontFamily = SFProDisplayFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPasswordDialog = false
                        viewModel.requestPasswordReset()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioraNeonLime,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Send Reset Code",
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false }
                ) {
                    Text(
                        "Cancel",
                        fontFamily = SFProDisplayFontFamily,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraAuthBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AuthTopBar(
            onBackOrClose = onBack,
            showLeftButton = false
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
                text = "Welcome back",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.8).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your password to unlock your workspace tasks and team lists.",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User Email Interactive Identity Pill
            VioraAuthIdentityCard(
                label = "Account",
                value = email,
                actionText = "Change",
                onClick = onBack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hidden text field for Autofill
            TextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                modifier = Modifier
                    .size(1.dp)
                    .alpha(0.01f)
                    .autofill(
                        autofillTypes = listOf(AutofillType.EmailAddress, AutofillType.Username),
                        onFill = { viewModel.updateEmail(it) }
                    )
            )

            VioraAuthTextField(
                value = password,
                onValueChange = { 
                    viewModel.updatePassword(it)
                    viewModel.clearError() 
                },
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (password.isNotEmpty()) VioraNeonLime else Color(0xFF5E6064),
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (password.isNotBlank() && !isLoading) {
                            viewModel.login()
                        }
                    }
                ),
                modifier = Modifier.autofill(
                    autofillTypes = listOf(AutofillType.Password),
                    onFill = { viewModel.updatePassword(it) }
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Forgot Password Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showForgotPasswordDialog = true },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Forgot password?",
                        fontFamily = SFProDisplayFontFamily,
                        color = Color(0xFFA1A1AA),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (infoMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AuthInfoBanner(message = infoMessage!!)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
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
                text = if (isLoading) "Signing in..." else "Sign In",
                onClick = { viewModel.login() },
                enabled = password.isNotBlank() && !isLoading,
                isLoading = isLoading
            )
        }
    }
}

