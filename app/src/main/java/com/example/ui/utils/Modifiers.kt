package com.example.ui.utils
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}


fun Modifier.animateEnter(
    index: Int = 0,
    delayMillis: Int = 0, // Base delay, can be overridden directly
    staggerMillis: Int = 40, // Amount of stagger per index
    initialOffsetY: Float = 50f
): Modifier = composed {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.9f) }
    val offsetAnim = remember { Animatable(initialOffsetY) }

    LaunchedEffect(Unit) {
        val totalDelay = delayMillis + (index * staggerMillis)
        if (totalDelay > 0) {
            kotlinx.coroutines.delay(totalDelay.toLong())
        }
        
        launch {
            alphaAnim.animateTo(1f, animationSpec = tween(durationMillis = 400))
        }
        launch {
            scaleAnim.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            offsetAnim.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    graphicsLayer {
        this.alpha = alphaAnim.value
        this.scaleX = scaleAnim.value
        this.scaleY = scaleAnim.value
        this.translationY = offsetAnim.value
    }
}
