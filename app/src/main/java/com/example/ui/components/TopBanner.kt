package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.R

@Composable
fun TopBanner(
    event: com.example.model.MessageEvent?,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(event?.id) {
        if (event != null && event.message.isNotEmpty()) {
            isVisible = true
            delay(1500)
            isVisible = false
            delay(300) // wait for animation
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF262626))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isError = event?.isError == true
                val icon = if (isError) Icons.Rounded.Error else Icons.Rounded.CheckCircle
                val iconColor = if (isError) Color(0xFFFF4B4B) else Color(0xFFB4FF00)

                Icon(
                    imageVector = icon,
                    contentDescription = if (isError) "Error" else "Success",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = event?.message ?: "",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.sf_pro_display_medium)),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

