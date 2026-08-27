package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthText
import com.example.ui.theme.VioraNeonLime

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuthViewModel

@Composable
fun OtpScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val otpValue by viewModel.otp.collectAsStateWithLifecycle()
    val otpLength = 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraAuthBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AuthTopBar(
            onBackOrClose = onBack,
            isClose = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Check your email",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = VioraAuthText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "By deleting this task, it will be permanently unavailable. If you think you may need it later.",
                fontSize = 16.sp,
                color = VioraAuthGrayText,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            BasicTextField(
                value = otpValue,
                onValueChange = {
                    if (it.length <= otpLength && it.all { char -> char.isDigit() }) {
                        viewModel.updateOtp(it)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(otpLength) { index ->
                            val char = when {
                                index >= otpValue.length -> ""
                                else -> otpValue[index].toString()
                            }
                            val isFocused = index == otpValue.length
                            
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (isFocused || char.isNotEmpty()) VioraNeonLime else VioraAuthBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    color = VioraAuthText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive the email?",
                    color = VioraAuthGrayText,
                    fontSize = 14.sp
                )
                TextButton(onClick = { /* Resend */ }) {
                    Text(
                        text = "Resend it!",
                        color = VioraNeonLime,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val isOtpCorrect = otpValue.length == 6
            AuthPrimaryButton(
                text = "Next",
                onClick = onNext,
                enabled = isOtpCorrect
            )
        }
    }
}
