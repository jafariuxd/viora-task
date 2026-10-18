package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Alignment as RiveAlignment
import app.rive.runtime.kotlin.core.Fit
import com.example.R
import com.example.ui.theme.VioraNeonLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Creative, non-cliché Rive celebration dialog displayed when a task is completed.
 * Instead of a generic checkmark, the task literally launches into orbit like a cosmic rocket
 * with high-energy animated thrusters, interstellar speed-trails, and futuristic HUD rewards.
 */
@Composable
fun RiveTaskDoneDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    taskTitle: String? = null,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val context = LocalContext.current
    val rocketOffsetY = remember { Animatable(40f) }
    val rocketScale = remember { Animatable(0.7f) }
    val portalPulse = remember { Animatable(0.85f) }

    // Subtle vibration haptic pulse upon blastoff
    LaunchedEffect(Unit) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
            }
        } catch (_: Exception) {}

        // Launch physics: slight dip then powerful cosmic liftoff
        launch {
            rocketScale.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
            )
        }
        launch {
            rocketOffsetY.animateTo(
                targetValue = -12f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 250f)
            )
        }
        launch {
            portalPulse.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
            portalPulse.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }

        // Auto dismiss after the launch experience plays
        delay(2600)
        onDismiss()
    }

    // Infinite cosmic stars pulse
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starGlow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Cosmic starfield backdrop particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val starPositions = listOf(
                    Offset(cx - 140f, cy - 220f),
                    Offset(cx + 150f, cy - 200f),
                    Offset(cx - 180f, cy - 80f),
                    Offset(cx + 170f, cy - 60f),
                    Offset(cx - 130f, cy + 180f),
                    Offset(cx + 140f, cy + 190f),
                    Offset(cx - 60f, cy - 260f),
                    Offset(cx + 80f, cy - 270f)
                )
                for (pos in starPositions) {
                    drawCircle(
                        color = VioraNeonLime.copy(alpha = 0.4f * starGlow),
                        radius = 3.dp.toPx(),
                        center = pos
                    )
                }
            }

            // Confetti / party burst in the background
            Box(
                modifier = Modifier
                    .size(420.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        RiveAnimationView(ctx).apply {
                            try {
                                setRiveResource(
                                    resId = R.raw.party_popper,
                                    artboardName = null,
                                    animationName = "Party_popper",
                                    autoplay = true,
                                    fit = Fit.COVER,
                                    alignment = RiveAlignment.CENTER
                                )
                            } catch (_: Exception) {
                                // Fallback to secondary confetti if popper artboard differs
                                try {
                                    setRiveResource(
                                        resId = R.raw.confetti,
                                        artboardName = "Main",
                                        stateMachineName = "State Machine",
                                        autoplay = true,
                                        fit = Fit.COVER,
                                        alignment = RiveAlignment.CENTER
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                    }
                )
            }

            // Central Cosmic Launch Card
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF16161A).copy(alpha = 0.96f),
                                Color(0xFF0C0C0E).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                VioraNeonLime.copy(alpha = 0.8f),
                                VioraNeonLime.copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(vertical = 30.dp, horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top Mission Tag
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(VioraNeonLime.copy(alpha = 0.14f))
                        .border(1.dp, VioraNeonLime.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = VioraNeonLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MISSION ACCOMPLISHED",
                        color = VioraNeonLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rive Rocket Portal
                Box(
                    modifier = Modifier
                        .size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer neon glowing aura
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(portalPulse.value)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        VioraNeonLime.copy(alpha = 0.22f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Inner space portal container
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070709))
                            .border(1.5.dp, VioraNeonLime.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dynamic Rive Rocket Animation View
                        AndroidView(
                            modifier = Modifier
                                .size(130.dp)
                                .offset { IntOffset(0, rocketOffsetY.value.roundToInt()) }
                                .scale(rocketScale.value),
                            factory = { ctx ->
                                RiveAnimationView(ctx).apply {
                                    try {
                                        setRiveResource(
                                            resId = R.raw.rocket_launch,
                                            artboardName = "Rocket",
                                            animationName = "rocket",
                                            autoplay = true,
                                            fit = Fit.CONTAIN,
                                            alignment = RiveAlignment.CENTER
                                        )
                                    } catch (e: Exception) {
                                        // Fallback if specific artboard not matched
                                        try {
                                            setRiveResource(
                                                resId = R.raw.rocket_launch,
                                                autoplay = true,
                                                fit = Fit.CONTAIN,
                                                alignment = RiveAlignment.CENTER
                                            )
                                        } catch (e2: Exception) {
                                            e2.printStackTrace()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Launch Title
                Text(
                    text = "Launched into Orbit!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (!taskTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = taskTitle.take(40) + if (taskTitle.length > 40) "..." else "",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Orbital Rewards Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E24))
                        .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = VioraNeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+50 XP  •  Focus Flow",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
