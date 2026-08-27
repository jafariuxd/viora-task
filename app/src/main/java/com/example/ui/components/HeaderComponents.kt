package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val VIORA_TOP_APP_BAR_HEIGHT: Dp = 88.dp
val VIORA_HEADER_BUTTON_BORDER: Color = Color.White.copy(alpha = 0.2f)

@Composable
fun VioraHeaderIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    iconSize: Dp = 22.dp,
    badge: @Composable (BoxScope.() -> Unit)? = null
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, VIORA_HEADER_BUTTON_BORDER, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
            if (badge != null) {
                badge()
            }
        }
    }
}

@Composable
fun VioraHeaderCustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, VIORA_HEADER_BUTTON_BORDER, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun VioraTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(VIORA_TOP_APP_BAR_HEIGHT)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (navigationIcon != null) {
            navigationIcon()
        }

        if (title != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (navigationIcon != null || actions != null) 12.dp else 0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                title()
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                actions()
            }
        }
    }
}
