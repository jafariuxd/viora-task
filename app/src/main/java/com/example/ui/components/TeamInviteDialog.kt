package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamInviteDialog(
    teamId: String,
    teamName: String,
    inviterName: String? = null,
    onAccept: () -> Unit,
    onDecline: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF161616),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(VioraNeonLime.copy(alpha = 0.15f))
                    .border(1.dp, VioraNeonLime.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Group,
                    contentDescription = null,
                    tint = VioraNeonLime,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Team Invitation",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (!inviterName.isNullOrBlank()) "Invited by @$inviterName to collaborate in" else "You've been invited to collaborate with team",
                color = Color(0xFFAAAAAA),
                fontSize = 15.sp,
                fontFamily = SFProDisplayFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Surface(
                color = Color(0xFF222222),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = teamName,
                    color = VioraNeonLime,
                    fontSize = 18.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Accept Button
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Join Team",
                    fontSize = 17.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Decline / Dismiss Button
            TextButton(
                onClick = {
                    if (onDecline != null) onDecline() else onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Decline",
                    color = Color(0xFF8E8E93),
                    fontSize = 16.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
