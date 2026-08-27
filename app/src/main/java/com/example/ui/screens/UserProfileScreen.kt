package com.example.ui.screens
import androidx.compose.ui.text.style.TextAlign
import com.example.viewmodel.VioraTaskViewModel

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.theme.VioraNeonLime
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    taskViewModel: VioraTaskViewModel,
    onBack: () -> Unit,
    onNavigateToEditProfile: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onAvatarChanged: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "userProfileSpringBack"
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


    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val teams by taskViewModel.teams.collectAsStateWithLifecycle()
    
    val assignedTasks = tasks.size
    val completedTasks = tasks.count { it.status == com.example.model.TaskStatus.DONE }
    val overdueTasks = tasks.count { it.daysLeft < 0 && it.status != com.example.model.TaskStatus.DONE }
    val activeTeams = teams.size

    var expandedMenu by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showVioraPassBottomSheet by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileAvatar(uri.toString())
            onAvatarChanged()
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
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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
                Box {
                    com.example.ui.components.VioraHeaderIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "More",
                        onClick = { expandedMenu = true }
                    )

                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme.copy(
                            surface = Color.White,
                            surfaceContainer = Color.White,
                            surfaceContainerHigh = Color.White,
                            surfaceContainerLow = Color.White,
                            surfaceContainerLowest = Color.White,
                            surfaceContainerHighest = Color.White,
                            onSurface = Color.Black
                        )
                    ) {
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },
                            shape = RoundedCornerShape(24.dp),
                            containerColor = Color.White,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Share Profile",
                                            color = Color.Black,
                                            fontFamily = SFProDisplayFontFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.width(132.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    expandedMenu = false
                                    showVioraPassBottomSheet = true
                                },
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Settings",
                                            color = Color.Black,
                                            fontFamily = SFProDisplayFontFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.width(132.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    expandedMenu = false
                                    onNavigateToSettings()
                                },
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.NotificationsNone,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Notification Settings",
                                            color = Color.Black,
                                            fontFamily = SFProDisplayFontFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.width(132.dp)
                                        )
                                    }
                                },
                                onClick = { expandedMenu = false },
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Logout",
                                            color = Color.Red,
                                            fontFamily = SFProDisplayFontFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.width(132.dp)
                                        )
                                    }
                                },
                                onClick = { 
                                    expandedMenu = false
                                    showLogoutConfirm = true
                                },
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
                            )
                        }
                    }
                }
            }
        )

        // Avatar Profile
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
        ) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") }
                ) {
                    com.example.ui.components.UserAvatar(
                        userId = profile?.username ?: profile?.name ?: "User",
                        avatarUri = profile?.profileImageUri,
                        size = 125.dp
                    )
                }
                
                // Camera Badge
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

        profile?.let { userProfile ->
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userProfile.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = userProfile.username,
                fontSize = 16.sp,
                color = VioraNeonLime
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateToEditProfile(false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(text = "Edit profile", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Activities Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Activities",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActivityCard(
                        title = "Assigned Tasks",
                        count = assignedTasks.toString(),
                        unit = "Tasks",
                        pillColor = Color(0xFFD3E3FC), // Light Blue
                        modifier = Modifier.weight(1f)
                    )
                    ActivityCard(
                        title = "Tasks Completed",
                        count = completedTasks.toString(),
                        unit = "Tasks",
                        pillColor = Color(0xFFCEF4AC), // Light Green
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActivityCard(
                        title = "Overdue",
                        count = overdueTasks.toString(),
                        unit = "Tasks",
                        pillColor = Color(0xFFFFD9D9), // Light Red
                        modifier = Modifier.weight(1f)
                    )
                    ActivityCard(
                        title = "Active Teams",
                        count = activeTeams.toString(),
                        unit = "Teams",
                        pillColor = Color(0xFFFDE293), // Light Yellow
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Default Deadline Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .height(23.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F3F4))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Default Deadline",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userProfile.stats.defaultDeadlineDays.toString(),
                            fontSize = 36.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Days",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F4))
                                .clickable { onNavigateToEditProfile(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Deadline",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Member since ${userProfile.joinDate}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showLogoutConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = Color(0xFF2A2A2A),
            title = {
                Text(
                    text = "Are you sure you want to log out?",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "You will need to enter your credentials to log back in.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {},
            dismissButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            showLogoutConfirm = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Logout", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { showLogoutConfirm = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        )
    }

    if (showVioraPassBottomSheet) {
        VioraPassBottomSheet(
            viewModel = taskViewModel,
            onDismissRequest = { showVioraPassBottomSheet = false },
            onNavigateToScanner = {
                showVioraPassBottomSheet = false
                onNavigateToScanner()
            }
        )
    }
}

@Composable
fun ActivityCard(
    title: String,
    count: String,
    unit: String,
    pillColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(101.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .height(23.dp)
                .clip(CircleShape)
                .background(pillColor)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = count,
                fontSize = 40.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unit,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}
