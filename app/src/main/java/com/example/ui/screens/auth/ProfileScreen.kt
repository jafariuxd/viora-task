package com.example.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.components.AuthTopBar
import com.example.ui.components.AuthTopRightButton
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthSurface
import com.example.ui.components.VioraAuthText
import com.example.ui.components.VioraAuthTextField
import com.example.ui.components.VioraAuthTextSecondary
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.util.autofill
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BackHandler { onBack() }

    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val avatarUri by viewModel.avatarUri.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()

    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localUri = com.example.util.ImageUtil.copyUriToInternalStorage(context, uri.toString())
            viewModel.updateAvatarUri(localUri ?: uri.toString())
        } else {
            viewModel.updateAvatarUri(null)
        }
    }

    val isPasswordValid = password.length >= 6
    val isUsernameValid = username.trim().length >= 2
    val isFormValid = fullName.isNotBlank() && isUsernameValid && isPasswordValid

    // Password strength score: 0 to 3
    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> 0
            password.length < 6 -> 1
            password.length >= 8 && password.any { it.isDigit() || !it.isLetterOrDigit() } -> 3
            else -> 2
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
            currentStep = 3,
            totalSteps = 4,
            rightAction = {
                AuthTopRightButton(
                    text = "Next",
                    onClick = onNext,
                    enabled = isFormValid
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
                text = "Your Profile",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.8).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set up your public name and credentials for seamless collaboration.",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Selector with Camera Badge
            val pickPhoto = {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .clickable { pickPhoto() }
                ) {
                    com.example.ui.components.UserAvatar(
                        userId = username.ifBlank { fullName.ifBlank { "User" } },
                        avatarUri = avatarUri,
                        size = 108.dp
                    )
                    
                    // Camera icon badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp),
                        shape = CircleShape,
                        color = VioraNeonLime,
                        border = BorderStroke(2.5.dp, Color.Black),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Upload avatar",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to choose photo (optional)",
                fontFamily = SFProDisplayFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8E8E93),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(28.dp))

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

            // Full Name Input
            VioraAuthTextField(
                value = fullName,
                onValueChange = { viewModel.updateFullName(it) },
                label = "Full Name",
                placeholder = "e.g. Alex Morgan",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = if (fullName.isNotBlank()) VioraNeonLime else Color(0xFF5E6064),
                        modifier = Modifier.size(22.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.autofill(
                    autofillTypes = listOf(AutofillType.PersonFullName),
                    onFill = { viewModel.updateFullName(it) }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username Input
            VioraAuthTextField(
                value = username,
                onValueChange = { 
                    viewModel.updateUsername(it.replace(" ", "").lowercase()) 
                },
                label = "Username",
                placeholder = "alexmorgan",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AlternateEmail,
                        contentDescription = null,
                        tint = if (isUsernameValid) VioraNeonLime else Color(0xFF5E6064),
                        modifier = Modifier.size(22.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.autofill(
                    autofillTypes = listOf(AutofillType.Username),
                    onFill = { viewModel.updateUsername(it) }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Input
            VioraAuthTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = "Create Password",
                placeholder = "Minimum 6 characters",
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
                        if (isFormValid) {
                            onNext()
                        }
                    }
                ),
                modifier = Modifier.autofill(
                    autofillTypes = listOf(AutofillType.NewPassword, AutofillType.Password),
                    onFill = { viewModel.updatePassword(it) }
                )
            )

            // Password Strength Indicator Bar
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..3) {
                            val active = passwordStrength >= i
                            val barColor = when {
                                !active -> Color(0xFF2C2C2E)
                                passwordStrength == 1 -> Color(0xFFEF4444)
                                passwordStrength == 2 -> Color(0xFFF59E0B)
                                else -> VioraNeonLime
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(barColor)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPasswordValid) "At least 6 characters" else "Password too short",
                            fontFamily = SFProDisplayFontFamily,
                            fontSize = 12.sp,
                            color = if (isPasswordValid) VioraNeonLime else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when (passwordStrength) {
                                1 -> "Weak"
                                2 -> "Medium"
                                3 -> "Strong"
                                else -> ""
                            },
                            fontFamily = SFProDisplayFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (passwordStrength) {
                                1 -> Color(0xFFEF4444)
                                2 -> Color(0xFFF59E0B)
                                3 -> VioraNeonLime
                                else -> Color.Transparent
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            AuthPrimaryButton(
                text = "Continue to Preferences",
                onClick = onNext,
                enabled = isFormValid
            )
        }
    }
}

