package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

val VioraAuthBackground = Color(0xFF000000)
val VioraAuthSurface = Color(0xFF1C1C1E)
val VioraAuthText = Color(0xFFFFFFFF)
val VioraAuthTextSecondary = Color(0xFFD1D5DB)
val VioraAuthGrayText = Color(0xFF8E8E93)
val VioraAuthBorder = Color(0xFF2C2C2E)
val VioraAuthBorderFocused = Color(0xFFB4FF00)
val VioraAuthCardBg = Color(0xFF1C1C1E)

/**
 * Modern Brand Anchor Pill with Viora geometric icon
 */
@Composable
fun VioraBrandBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFF1C1C1E), RoundedCornerShape(100.dp))
            .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(100.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(VioraNeonLime, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "VIORA",
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = Color.White
        )
    }
}

/**
 * Step Progress Indicator for Auth Onboarding Flows
 */
@Composable
fun AuthStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isActive = i <= currentStep
            val isCurrent = i == currentStep
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(if (isCurrent) 22.dp else 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isCurrent -> VioraNeonLime
                            isActive -> Color.White
                            else -> Color(0xFF2C2C2E)
                        }
                    )
            )
        }
    }
}

@Composable
fun AuthTopBar(
    onBackOrClose: () -> Unit,
    isClose: Boolean = false,
    currentStep: Int? = null,
    totalSteps: Int? = null,
    showBrandBadge: Boolean = false,
    showLeftButton: Boolean = true,
    rightAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLeftButton) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 22.dp),
                        onClick = onBackOrClose
                    ),
                shape = CircleShape,
                color = Color(0xFF1C1C1E),
                border = BorderStroke(1.dp, Color(0xFF2C2C2E))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isClose) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isClose) "Close" else "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }

        if (currentStep != null && totalSteps != null) {
            AuthStepIndicator(
                currentStep = currentStep,
                totalSteps = totalSteps
            )
        } else if (showBrandBadge) {
            VioraBrandBadge()
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
        
        Box(
            modifier = Modifier.size(width = if (rightAction != null) 90.dp else 44.dp, height = 44.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            rightAction?.invoke()
        }
    }
}

@Composable
fun AuthTopRightButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled && !isLoading) VioraNeonLime else Color(0xFF1C1C1E),
            contentColor = if (enabled && !isLoading) Color.Black else Color(0xFF5E6064),
            disabledContainerColor = Color(0xFF1C1C1E),
            disabledContentColor = Color(0xFF5E6064)
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(38.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = SFProDisplayFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isLoading) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled && !isLoading) 8.dp else 0.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0x66B4FF00),
                ambientColor = Color(0x33B4FF00)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled && !isLoading) VioraNeonLime else Color(0xFF1C1C1E),
            contentColor = if (enabled && !isLoading) Color.Black else Color(0xFF5E6064),
            disabledContainerColor = Color(0xFF1C1C1E),
            disabledContentColor = Color(0xFF5E6064)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                fontFamily = SFProDisplayFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
            if (!isLoading && trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF1C1C1E),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            fontFamily = SFProDisplayFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -10 }),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF241214),
            border = BorderStroke(1.dp, Color(0xFF4A1F24))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF4A1F24), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    fontFamily = SFProDisplayFontFamily,
                    color = Color(0xFFFFB4B4),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun AuthInfoBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -10 }),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F2618),
            border = BorderStroke(1.dp, Color(0xFF1E4D30))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF1E4D30), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    fontFamily = SFProDisplayFontFamily,
                    color = Color(0xFF86EFAC),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun VioraAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = SFProDisplayFontFamily,
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        ),
        label = {
            Text(
                text = label,
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.5.sp
            )
        },
        placeholder = if (placeholder != null) {
            {
                Text(
                    text = placeholder,
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 15.5.sp,
                    color = Color(0xFF5E6064)
                )
            }
        } else null,
        leadingIcon = if (leadingIcon != null) {
            {
                Box(modifier = Modifier.padding(start = 10.dp)) {
                    leadingIcon()
                }
            }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            {
                Box(modifier = Modifier.padding(end = 10.dp)) {
                    trailingIcon()
                }
            }
        } else null,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp),
        shape = RoundedCornerShape(100.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1C1C1E),
            unfocusedContainerColor = Color(0xFF1C1C1E),
            focusedBorderColor = VioraNeonLime,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = VioraNeonLime,
            unfocusedLabelColor = Color(0xFF8E8E93),
            cursorColor = VioraNeonLime,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedPlaceholderColor = Color(0xFF5E6064)
        )
    )
}

@Composable
fun VioraAuthIdentityCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = "Edit",
    icon: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .clickable { onClick() },
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        icon()
                    } else {
                        Text(
                            text = value.take(1).uppercase(),
                            fontFamily = SFProDisplayFontFamily,
                            color = VioraNeonLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            Surface(
                color = Color(0xFF2C2C2E),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = actionText,
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VioraNeonLime,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}


