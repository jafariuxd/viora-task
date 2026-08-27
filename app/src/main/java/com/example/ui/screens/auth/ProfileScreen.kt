package com.example.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.AuthTopBar
import com.example.ui.components.AuthTopRightButton
import com.example.ui.components.VioraAuthBackground
import com.example.ui.components.VioraAuthBorder
import com.example.ui.components.VioraAuthGrayText
import com.example.ui.components.VioraAuthText
import com.example.ui.theme.VioraNeonLime

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val avatarUri by viewModel.avatarUri.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val smallBase64 = com.example.util.ImageUtil.toSmallBase64(context, uri.toString())
            val localUri = com.example.util.ImageUtil.copyUriToInternalStorage(context, uri.toString())
            viewModel.updateAvatarUri(smallBase64 ?: localUri)
        } else {
            viewModel.updateAvatarUri(null)
        }
    }

    val isFormValid = fullName.isNotBlank() && username.isNotBlank() && password.isNotBlank()

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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Complete your profile",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = VioraAuthText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Shape your profile identity. Complete your profile to continue.",
                fontSize = 16.sp,
                color = VioraAuthGrayText,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { viewModel.updateFullName(it) },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioraNeonLime,
                    focusedLabelColor = VioraAuthGrayText,
                    unfocusedBorderColor = VioraAuthBorder,
                    unfocusedLabelColor = VioraAuthGrayText,
                    cursorColor = VioraNeonLime,
                    unfocusedTextColor = VioraAuthText,
                    focusedTextColor = VioraAuthText
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) },
                placeholder = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioraNeonLime,
                    unfocusedBorderColor = VioraAuthBorder,
                    unfocusedPlaceholderColor = VioraAuthGrayText,
                    cursorColor = VioraNeonLime,
                    unfocusedTextColor = VioraAuthText,
                    focusedTextColor = VioraAuthText
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                placeholder = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioraNeonLime,
                    unfocusedBorderColor = VioraAuthBorder,
                    unfocusedPlaceholderColor = VioraAuthGrayText,
                    cursorColor = VioraNeonLime,
                    unfocusedTextColor = VioraAuthText,
                    focusedTextColor = VioraAuthText
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Profile photo (optional):",
                fontSize = 16.sp,
                color = VioraAuthGrayText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ui.components.UserAvatar(
                        userId = username.ifBlank { fullName.ifBlank { "User" } },
                        avatarUri = avatarUri,
                        size = 100.dp
                    )
                }
            }
        }
    }
}
