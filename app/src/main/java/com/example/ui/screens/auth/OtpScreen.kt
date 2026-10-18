package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.components.VioraAuthTextSecondary
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.AuthViewModel

@Composable
fun OtpScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BackHandler { onBack() }

    val email by viewModel.email.collectAsStateWithLifecycle()
    val otpValue by viewModel.otp.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()
    val otpLength = 6

    // Resend countdown timer: 45 seconds
    var secondsLeft by remember { mutableIntStateOf(45) }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
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
            onBackOrClose = onBack,
            isClose = false,
            currentStep = 2,
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
                text = "Verification Code",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.8).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter the 6-digit confirmation code we just sent to your email address.",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            // Email Display Interactive Identity Pill
            VioraAuthIdentityCard(
                label = "Sent to",
                value = email,
                actionText = "Edit",
                onClick = onBack,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = VioraNeonLime,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // High-fidelity OTP Input Grid
            BasicTextField(
                value = otpValue,
                onValueChange = {
                    if (it.length <= otpLength && it.all { char -> char.isDigit() }) {
                        viewModel.updateOtp(it)
                        viewModel.clearError()
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(otpLength) { index ->
                            val char = when {
                                index >= otpValue.length -> ""
                                else -> otpValue[index].toString()
                            }
                            val isCurrent = index == otpValue.length
                            val isFilled = char.isNotEmpty()
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(62.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFF2C2C2E)
                                            isFilled -> Color(0xFF1C1C1E)
                                            else -> Color(0xFF1C1C1E)
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = when {
                                            isCurrent -> VioraNeonLime
                                            isFilled -> Color.White
                                            else -> Color(0xFF2C2C2E)
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (char.isNotEmpty()) {
                                    Text(
                                        text = char,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                } else if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(20.dp)
                                            .background(VioraNeonLime, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Resend Code Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive code?",
                    fontFamily = SFProDisplayFontFamily,
                    color = Color(0xFF8E8E93),
                    fontSize = 14.sp
                )
                if (secondsLeft > 0) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color(0xFF1C1C1E),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E))
                    ) {
                        Text(
                            text = "Resend in ${secondsLeft}s",
                            fontFamily = SFProDisplayFontFamily,
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            viewModel.resendOtp()
                            secondsLeft = 45
                        },
                        enabled = !isLoading,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Resend Code",
                                fontFamily = SFProDisplayFontFamily,
                                color = VioraNeonLime,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (infoMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                AuthInfoBanner(message = infoMessage!!)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                AuthErrorBanner(message = errorMessage!!)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            val isOtpComplete = otpValue.length == 6
            AuthPrimaryButton(
                text = if (isLoading) "Verifying..." else "Verify & Continue",
                onClick = onNext,
                enabled = isOtpComplete && !isLoading,
                isLoading = isLoading
            )
        }
    }
}

