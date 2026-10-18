package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.network.inspector.ApiInspectorManager
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import android.net.Uri
import android.content.Intent
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Description
import com.example.firebase.VioraRemoteConfigManager
import com.example.viewmodel.AgendaViewModel
import com.example.viewmodel.VioraTaskViewModel

data class PresetCity(val name: String, val latitude: Double, val longitude: Double)

val presetCities = listOf(
    PresetCity("Tehran", 35.6944, 51.4215),
    PresetCity("Mashhad", 36.2970, 59.6062),
    PresetCity("Isfahan", 32.6546, 51.6680),
    PresetCity("Shiraz", 29.5918, 52.5837),
    PresetCity("Tabriz", 38.0962, 46.2738),
    PresetCity("Karaj", 35.8327, 50.9915),
    PresetCity("Ahvaz", 31.3183, 48.6706),
    PresetCity("Yazd", 31.8974, 54.3569),
    PresetCity("Rasht", 37.2808, 49.5831),
    PresetCity("Kerman", 30.2839, 57.0834),
    PresetCity("Qom", 34.6416, 50.8746),
    PresetCity("Sanandaj", 35.3113, 46.9961),
    PresetCity("Zahedan", 29.4963, 60.8629),
    PresetCity("Sari", 36.5659, 53.0581),
    PresetCity("Kermanshah", 34.3142, 47.0650),
    PresetCity("Urmia", 37.5527, 45.0761)
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToArchives: () -> Unit,
    agendaViewModel: AgendaViewModel = viewModel(),
    taskViewModel: VioraTaskViewModel = viewModel()
) {
    val rawContext = LocalContext.current
    val context = rawContext as Activity
    val isAuthorized by agendaViewModel.isAuthorized.collectAsState()
    val authIntent by agendaViewModel.authIntent.collectAsState()

    var showDisconnectDialog by remember { mutableStateOf(false) }

    val isAutoDetect by taskViewModel.weatherAutoDetect.collectAsState()
    val manualCityVal by taskViewModel.manualCity.collectAsState()
    val manualLatVal by taskViewModel.manualLatitude.collectAsState()
    val manualLonVal by taskViewModel.manualLongitude.collectAsState()
    var showCityDialog by remember { mutableStateOf(false) }

    val isAutoShowNetworkLogs by ApiInspectorManager.isAutoShowEnabled.collectAsState()
    val networkLogs by ApiInspectorManager.logs.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        agendaViewModel.handleAuthorizationResult(context, result.data, result.resultCode)
    }

    LaunchedEffect(authIntent) {
        if (authIntent != null) {
            launcher.launch(authIntent!!)
            agendaViewModel.clearAuthIntent()
        }
    }

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "settingsSpringBack"
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
    ) {
        // Top Bar
        com.example.ui.components.VioraTopAppBar(
            navigationIcon = {
                com.example.ui.components.VioraHeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
            },
            title = {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFProDisplayFontFamily
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings List
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Integrations",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )

            // Google Calendar Setting
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (isAuthorized) {
                            showDisconnectDialog = true
                        } else {
                            agendaViewModel.authorizeAndFetch(context, silent = false)
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Google Calendar",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = if (isAuthorized) "Connected" else "Not connected",
                                color = if (isAuthorized) VioraNeonLime else Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Manage",
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notifications Status & Settings Card
            val isNotificationsEnabled = remember(context) {
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent().apply {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                } else {
                                    action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isNotificationsEnabled) VioraNeonLime.copy(alpha = 0.1f)
                                    else Color.Red.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (isNotificationsEnabled) VioraNeonLime else Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Push Notifications",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = if (isNotificationsEnabled) "Active (Channels configured)" else "Blocked in System Settings — Tap to allow",
                                color = if (isNotificationsEnabled) VioraNeonLime else Color(0xFFFF5252),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Manage Notifications",
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Teams & Organization",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )

            // Archives Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToArchives() },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Archive,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Archives",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = "Restore teams, lists, and tasks",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Archives",
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Weather",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )

            // Auto Detect Location Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Auto-detect location",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = "Detect your city via IP address",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    
                    Switch(
                        checked = isAutoDetect,
                        onCheckedChange = { checked ->
                            taskViewModel.setWeatherSettings(checked, manualCityVal, manualLatVal, manualLonVal)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VioraNeonLime,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray,
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (!isAutoDetect) {
                Spacer(modifier = Modifier.height(12.dp))

                // Manual City Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCityDialog = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Selected City",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = SFProDisplayFontFamily
                                )
                                Text(
                                    text = manualCityVal,
                                    color = VioraNeonLime,
                                    fontSize = 13.sp,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select City",
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Developer & Diagnostics",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )

            // View Network Logs Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { ApiInspectorManager.openInspector() },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Network API Logs",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = "${networkLogs.size} request logs stored",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Logs",
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-popup Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Auto-popup on requests",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = "Show bottom sheet instantly per request",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }

                    Switch(
                        checked = isAutoShowNetworkLogs,
                        onCheckedChange = { checked ->
                            ApiInspectorManager.setAutoShowEnabled(checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VioraNeonLime,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray,
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            val remoteConfig by VioraRemoteConfigManager.configState.collectAsState()

            Text(
                text = "Support & Legal (Remote)",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )

            // Support Link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(remoteConfig.supportUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsScreen", "Failed to open support URL: ${e.message}")
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Contact Support",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = remoteConfig.supportUrl,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy Policy Link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(remoteConfig.privacyUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsScreen", "Failed to open privacy URL: ${e.message}")
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Policy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Privacy Policy",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = remoteConfig.privacyUrl,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Terms of Service Link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(remoteConfig.termsUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsScreen", "Failed to open terms URL: ${e.message}")
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Terms of Service",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = remoteConfig.termsUrl,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    com.example.ui.components.WeatherCityBottomSheet(
        visible = showCityDialog,
        onDismiss = { showCityDialog = false },
        taskViewModel = taskViewModel
    )

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = {
                Text("Disconnect Calendar", color = Color.White, fontFamily = SFProDisplayFontFamily)
            },
            text = {
                Text(
                    "Are you sure you want to disconnect your Google Calendar? You will need to sign in again to view your agenda.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = SFProDisplayFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        agendaViewModel.disconnectCalendar(context)
                        showDisconnectDialog = false
                    }
                ) {
                    Text("Disconnect", color = Color(0xFFFF5252), fontFamily = SFProDisplayFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDisconnectDialog = false }
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f), fontFamily = SFProDisplayFontFamily)
                }
            },
            containerColor = Color(0xFF2C2C2E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
