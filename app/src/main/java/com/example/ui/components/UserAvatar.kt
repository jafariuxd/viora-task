package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.util.BoringAvatarUtil
import com.example.util.ImageUtil

@Composable
fun UserAvatar(
    userId: String,
    avatarUri: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val context = LocalContext.current
    val avatarModel = remember(userId, avatarUri) {
        val seed = when {
            userId.isNotBlank() && userId != "User" -> userId
            !avatarUri.isNullOrEmpty() -> avatarUri
            else -> "user"
        }
        val url = BoringAvatarUtil.getAvatarUrl(seed, "beam")
        BoringAvatarUtil.getCoilRequest(context, url)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = avatarModel,
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

