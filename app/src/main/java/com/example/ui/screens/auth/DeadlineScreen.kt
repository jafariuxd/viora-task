package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthErrorBanner
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.AuthTopRightButton
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthText
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun DeadlineScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BackHandler { onBack() }
    
    val selectedOption by viewModel.defaultDeadline.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            com.example.util.AutofillUtil.commitAutofill(context)
            delay(800)
            viewModel.resetSuccess()
            onNext()
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
            currentStep = 4,
            totalSteps = 4,
            rightAction = {
                AuthTopRightButton(
                    text = "Finish",
                    onClick = { viewModel.verifyOtp() },
                    isLoading = isLoading,
                    enabled = !isLoading
                )
            }
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
                text = "Default Deadline",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.8).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set the default timeline automatically assigned to new tasks to keep your workflow in rhythm.",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            val customDays by viewModel.customDays.collectAsStateWithLifecycle()

            com.example.ui.components.DefaultDeadlineSelector(
                selectedOption = selectedOption,
                onOptionSelected = { viewModel.updateDefaultDeadline(it) },
                customDays = customDays,
                onCustomDaysChanged = { viewModel.updateCustomDays(it) },
                textColor = Color.White,
                unselectedTextColor = Color(0xFF8E8E93),
                borderColor = Color(0xFF2C2C2E),
                selectedBackgroundColor = VioraNeonLime,
                selectedItemTextColor = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                AuthErrorBanner(message = errorMessage!!)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            AuthPrimaryButton(
                text = if (isLoading) "Setting up workspace..." else "Complete Setup & Launch",
                onClick = { viewModel.verifyOtp() },
                enabled = !isLoading,
                isLoading = isLoading
            )
        }
    }
}

