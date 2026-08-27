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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
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
        if (result.resultCode == Activity.RESULT_OK) {
            agendaViewModel.handleAuthorizationResult(context, result.data)
        } else {
            agendaViewModel.setError("Google Sign-In failed or was canceled.")
        }
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
                .fillMaxWidth()
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
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showCityDialog) {
        Dialog(onDismissRequest = { showCityDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Select City",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(presetCities) { city ->
                            val isSelected = city.name == manualCityVal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) VioraNeonLime.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        taskViewModel.setWeatherSettings(false, city.name, city.latitude, city.longitude)
                                        showCityDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = city.name,
                                    color = if (isSelected) VioraNeonLime else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = SFProDisplayFontFamily
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Selected",
                                        tint = VioraNeonLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
