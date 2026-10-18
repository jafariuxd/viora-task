package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Sync
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
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleCalendarConfigBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSyncDeviceCalendar: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isSha1Copied by remember { mutableStateOf(false) }

    val sha1 = "21:90:96:94:A1:EE:91:C2:B7:C0:E5:56:9F:2B:DB:1A:42:11:87:19"
    val packageName = "com.viora.task"

    LaunchedEffect(isSha1Copied) {
        if (isSha1Copied) {
            delay(2000)
            isSha1Copied = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
        containerColor = Color(0xFF1C1C1E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = "Google Calendar Sync",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Error 10 occurs because Google Play Services requires registering this app's SHA-1 key in Firebase/Google Cloud. You can sync directly from your device calendar or add the key below.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Option: Sync Device Calendar (No setup required!)
            Button(
                onClick = {
                    onDismiss()
                    onSyncDeviceCalendar()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sync Device Calendar",
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider / Section label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                Text(
                    text = "OR CONFIGURE IN FIREBASE",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SHA-1 Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHA-1 Certificate Fingerprint",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Viora SHA-1", sha1)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "SHA-1 copied to clipboard", Toast.LENGTH_SHORT).show()
                            isSha1Copied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSha1Copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = "Copy SHA-1",
                            tint = if (isSha1Copied) VioraNeonLime else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = sha1,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Package Name Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Package Name",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                    Text(
                        text = packageName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Package Name", packageName)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Package name copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy Package Name",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Helpful note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = VioraNeonLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "In Firebase Console > Project Settings > Your apps, click 'Add fingerprint' and paste this SHA-1.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = SFProDisplayFontFamily,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
