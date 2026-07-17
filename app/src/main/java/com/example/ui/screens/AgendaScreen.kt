package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

@Composable
fun AgendaScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VioraNeonLime)
                    .clickable { /* Add meeting */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black
                )
            }
        }

        Text(
            text = "Schedule",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                AgendaMonthSection(
                    month = "November",
                    items = listOf(
                        AgendaItemData("19", "Online meeting", true, "11:00 am - 11:30 am", "Weekly Leadership"),
                        AgendaItemData("23", "In-person meeting", false, "11:00 am - 11:30 am", "Weekly Leadership"),
                        AgendaItemData("28", "Online meeting", true, "11:00 am - 11:30 am", "Weekly Leadership")
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                AgendaMonthSection(
                    month = "December",
                    items = listOf(
                        AgendaItemData("01", "Online meeting", true, "11:00 am - 11:30 am", "Weekly Leadership")
                    )
                )
            }
        }
    }
}

data class AgendaItemData(
    val day: String,
    val type: String,
    val isOnline: Boolean,
    val time: String,
    val title: String
)

@Composable
fun AgendaMonthSection(month: String, items: List<AgendaItemData>) {
    Column {
        Text(
            text = month,
            color = Color.Gray.copy(alpha = 0.5f),
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        items.forEach { item ->
            AgendaItem(item)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AgendaItem(item: AgendaItemData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rotated Text
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.day,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 84.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Black,
                    lineHeight = 60.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .requiredWidth(120.dp)
                    .rotate(-90f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Vertical separator
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(100.dp)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (item.isOnline) VioraNeonLime else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.type,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.time,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.title,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = VioraNeonLime,
            modifier = Modifier.size(32.dp)
        )
    }
}
