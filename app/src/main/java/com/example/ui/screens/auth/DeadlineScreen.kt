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
import com.example.ui.components.AuthTopBar
import com.example.ui.components.AuthTopRightButton
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthText
import com.example.ui.theme.VioraNeonLime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuthViewModel

@Composable
fun DeadlineScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BackHandler {
        onBack()
    }
    
    val selectedOption by viewModel.defaultDeadline.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            viewModel.resetSuccess()
            onNext()
        }
    }
    
    val options = listOf("Daily", "Weekly", "Monthly", "Custom")

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
            rightAction = {
                AuthTopRightButton(text = if (isLoading) "Loading..." else "Let's Go!", onClick = { viewModel.verifyOtp() })
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Choose default deadline",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = VioraAuthText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "We assign this deadline to your unscheduled tasks to ensure none are missed.",
                fontSize = 16.sp,
                color = VioraAuthGrayText,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val customDays by viewModel.customDays.collectAsStateWithLifecycle()

            com.example.ui.components.DefaultDeadlineSelector(
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
            }
        }
    }
}
