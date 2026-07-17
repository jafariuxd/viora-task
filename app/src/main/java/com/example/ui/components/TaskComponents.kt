package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

@Composable
fun VioraRadioButton(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val outerColor = if (selected) VioraNeonLime else Color(0xFF4A4A4A)
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, outerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(VioraNeonLime)
            )
        }
    }
}

@Composable
fun TeamListItem(
    teamName: String,
    isSelected: Boolean,
    onRowClick: () -> Unit,
    onRadioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(vertical = 20.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = teamName,
            color = if (isSelected) VioraNeonLime else Color.White,
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            letterSpacing = (-0.5).sp
        )
        Box(modifier = Modifier.clickable { onRadioClick() }.padding(8.dp)) {
            VioraRadioButton(selected = isSelected)
        }
    }
}

@Composable
fun StatusButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) VioraNeonLime else Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetailBadge(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(23.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}
