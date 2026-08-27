package com.example.ui.screens
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    focusDeadline: Boolean = false,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "editProfileSpringBack"
    )

    PredictiveBackHandler { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                predictiveBackProgress = backEvent.progress * 0.75f
            }
            onBack()
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            predictiveBackProgress = 0f
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("viora_auth_prefs", android.content.Context.MODE_PRIVATE) }
    val isRegistered = remember { authPrefs.getBoolean("is_registered", false) }

    val initialName = if (isRegistered) authPrefs.getString("user_name", "User") ?: "User" else ""
    val initialUsername = if (isRegistered) authPrefs.getString("user_username", "username") ?: "username" else "username"
    val initialEmail = if (isRegistered) authPrefs.getString("user_email", "user@example.com") ?: "user@example.com" else "user@example.com"
    val initialDeadline = if (isRegistered) authPrefs.getString("user_default_deadline", "Weekly") ?: "Weekly" else "Weekly"
    val initialCustomDays = if (isRegistered) authPrefs.getInt("user_custom_days", 14) else 14
    val initialAvatarUri = if (isRegistered) authPrefs.getString("user_avatar_uri", null) else null

    var fullName by remember { mutableStateOf(initialName) }
    var username by remember { mutableStateOf(initialUsername) }
    var email by remember { mutableStateOf(initialEmail) }
    var defaultDeadline by remember { mutableStateOf(initialDeadline) }
    var customDays by remember { mutableStateOf(initialCustomDays) }
    var avatarUri by remember { mutableStateOf(initialAvatarUri) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var deadlineSectionY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(focusDeadline) {
        if (focusDeadline) {
            kotlinx.coroutines.delay(300)
            scrollState.animateScrollTo(deadlineSectionY.toInt())
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = com.example.util.ImageUtil.copyUriToInternalStorage(context, uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val progress = animatedBackProgress
                if (progress > 0f) {
                    val scale = 1f - (progress * 0.12f)
                    scaleX = scale
                    scaleY = scale
                    translationY = progress * 70.dp.toPx()
                    alpha = 1f - (progress * 0.35f)
                    shape = RoundedCornerShape((progress * 32).dp)
                    clip = true
                }
            }
            .background(VioraBackground)
            .navigationBarsPadding()
            .imePadding()
            .padding(top = 48.dp) // Status bar padding
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        com.example.ui.components.VioraTopAppBar(
            navigationIcon = {
                com.example.ui.components.VioraHeaderIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onBack
                )
            },
            actions = {
                Button(
                    onClick = {
                        val savedAvatar = com.example.util.ImageUtil.toSmallBase64(context, avatarUri)

                        authPrefs.edit()
                            .putBoolean("is_registered", true)
                            .putString("user_name", fullName)
                            .putString("user_username", username)
                            .putString("user_email", email)
                            .putString("user_avatar_uri", savedAvatar ?: avatarUri)
                            .putString("user_default_deadline", defaultDeadline)
                            .putInt("user_custom_days", customDays)
                            .apply()

                        coroutineScope.launch {
                            try {
                                val req = com.example.model.viora.UpdateUserDto(
                                    fullName = fullName.ifEmpty { null },
                                    avatar = savedAvatar ?: avatarUri,
                                    days = customDays
                                )
                                com.example.network.viora.VioraNetworkModule.api.updateCurrentUser(req)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        onSave()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Save",
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Avatar Preview
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") }
            ) {
                com.example.ui.components.UserAvatar(
                    userId = username,
                    avatarUri = avatarUri,
                    size = 120.dp
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(VioraNeonLime)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Text Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileTextField(
                label = "Full name",
                value = fullName,
                onValueChange = { fullName = it },
                isFocusedMock = true
            )
            
            ProfileTextField(
                label = "Username",
                value = username,
                onValueChange = { username = it },
                prefix = "@"
            )
            
            ProfileTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Default Deadline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .onGloballyPositioned { deadlineSectionY = it.positionInParent().y }
        ) {
            Text(
                text = "Default Deadline",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            com.example.ui.components.DefaultDeadlineSelector(
                selectedOption = defaultDeadline,
                onOptionSelected = { defaultDeadline = it },
                customDays = customDays,
                onCustomDaysChanged = { customDays = it },
                textColor = Color.White,
                unselectedTextColor = Color.White,
                borderColor = Color.White,
                selectedBackgroundColor = VioraNeonLime,
                selectedItemTextColor = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "* Used when no deadline is defined on task, list or team level.",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String? = null,
    isFocusedMock: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = SFProDisplayFontFamily) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VioraNeonLime,
            unfocusedBorderColor = if (isFocusedMock) VioraNeonLime else Color.White,
            focusedLabelColor = Color.White.copy(alpha = 0.5f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = VioraNeonLime,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(32.dp),
        singleLine = true,
        leadingIcon = if (prefix != null) {
            {
                Text(
                    text = prefix,
                    color = VioraNeonLime,
                    fontSize = 18.sp,
                    fontFamily = SFProDisplayFontFamily,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else null,
        textStyle = TextStyle(fontSize = 18.sp, fontFamily = SFProDisplayFontFamily)
    )
}
