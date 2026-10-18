package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firebase.AppStatus
import com.example.firebase.VioraRemoteConfigManager
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraDarkPill
import com.example.ui.theme.VioraDarkText
import com.example.ui.theme.VioraGrayText
import com.example.ui.theme.VioraNeonLime

@Composable
fun AppGateOverlay(
    modifier: Modifier = Modifier
) {
    val appStatus by VioraRemoteConfigManager.appStatus.collectAsState()
    val context = LocalContext.current
    var isCheckingAgain by remember { mutableStateOf(false) }

    when (val status = appStatus) {
        is AppStatus.Maintenance -> {
            // Full screen overlay blocking access completely
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(VioraBackground)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(VioraDarkPill),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Maintenance",
                            tint = VioraNeonLime,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "در حال به‌روزرسانی زیرساخت",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = status.message,
                        color = VioraGrayText,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick = {
                            isCheckingAgain = true
                            VioraRemoteConfigManager.fetchAndActivate {
                                isCheckingAgain = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = VioraDarkText
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(54.dp)
                    ) {
                        if (isCheckingAgain) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = VioraDarkText,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تلاش مجدد",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        is AppStatus.ForceUpdateRequired -> {
            // Non-dismissible Dialog for mandatory update
            Dialog(
                onDismissRequest = { /* Cannot be dismissed */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(VioraDarkPill),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "به‌روزرسانی اجباری",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "برای ادامه استفاده از امکانات و همگام‌سازی ابری ویورا، باید نسخه اپلیکیشن خود را به‌روزرسانی نمایید.",
                            color = Color(0xFFB0B0B5),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        if (status.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = Color(0xFF242426),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "تغییرات نسخه جدید:\n${status.releaseNotes}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(14.dp),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.updateUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.util.Log.e("AppGateOverlay", "Failed to open update URL: ${e.message}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VioraNeonLime,
                                contentColor = VioraDarkText
                            ),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "به‌روزرسانی برنامه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        is AppStatus.OptionalUpdateAvailable -> {
            // Dismissible soft update dialog
            Dialog(
                onDismissRequest = { VioraRemoteConfigManager.dismissOptionalUpdate() },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VioraDarkPill),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = VioraNeonLime,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "نسخه جدید ویورا در دسترس است",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "امکانات و بهبودهای جدیدی در این نسخه اضافه شده است.",
                            color = Color(0xFFB0B0B5),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        if (status.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFF242426),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = status.releaseNotes,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { VioraRemoteConfigManager.dismissOptionalUpdate() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB0B0B5)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A3A3C)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("بعداً", fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.updateUrl)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                        VioraRemoteConfigManager.dismissOptionalUpdate()
                                    } catch (e: Exception) {
                                        android.util.Log.e("AppGateOverlay", "Failed to open update URL: ${e.message}")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VioraNeonLime,
                                    contentColor = VioraDarkText
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "به‌روزرسانی",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        is AppStatus.Normal -> {
            // Nothing rendered
        }
    }
}
