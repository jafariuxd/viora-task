package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Alarm
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
import com.example.model.Notification
import com.example.model.NotificationType
import com.example.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val groupedNotifications = notifications.groupBy { it.timeGroup }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable { /* More actions */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Notifications",
            fontSize = 44.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 7.dp, end = 7.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            groupedNotifications.forEach { (timeGroup, notifs) ->
                item {
                    Text(
                        text = timeGroup,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(
                            start = 13.dp,
                            bottom = 12.dp,
                            top = if (timeGroup == "Today") 8.dp else 16.dp
                        )
                    )
                }
                
                items(notifs) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White)
            .padding(start = 13.dp, top = 13.dp, end = 22.dp, bottom = 13.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon or Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when (notification.type) {
                        NotificationType.REMINDER -> Color(0xFFD4EFA5) // Greenish
                        NotificationType.DEADLINE -> Color(0xFFFFE0D6) // Reddish
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (notification.avatarRes != null) {
                Image(
                    painter = painterResource(id = notification.avatarRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val icon = when (notification.type) {
                    NotificationType.REMINDER -> Icons.Rounded.Alarm
                    NotificationType.DEADLINE -> Icons.Rounded.AccessTimeFilled
                    else -> Icons.Rounded.Alarm
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black.copy(alpha = 0.6f),
                lineHeight = 24.sp
            )
        }
    }
}
