package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import com.example.R

import androidx.compose.ui.text.style.TextDirection

val SFProDisplayFontFamily = FontFamily(
    Font(resId = R.font.sf_pro_display_light, weight = FontWeight.Light),
    Font(resId = R.font.sf_pro_display_regular, weight = FontWeight.Normal),
    Font(resId = R.font.sf_pro_display_medium, weight = FontWeight.Medium),
    Font(resId = R.font.sf_pro_display_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.sf_pro_display_bold, weight = FontWeight.Bold),
    

)

private val defaultTypography = Typography()

// Set of Material typography styles to start with
private val baseTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = SFProDisplayFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = SFProDisplayFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = SFProDisplayFontFamily),
    headlineLarge = TextStyle(
        fontFamily = SFProDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SFProDisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = SFProDisplayFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = SFProDisplayFontFamily),
    titleMedium = TextStyle(
        fontFamily = SFProDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = SFProDisplayFontFamily),
    bodyLarge = TextStyle(
        fontFamily = SFProDisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SFProDisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = SFProDisplayFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = SFProDisplayFontFamily),
    labelMedium = TextStyle(
        fontFamily = SFProDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = SFProDisplayFontFamily)
)

val Typography = Typography(
    displayLarge = baseTypography.displayLarge.copy(textDirection = TextDirection.Content),
    displayMedium = baseTypography.displayMedium.copy(textDirection = TextDirection.Content),
    displaySmall = baseTypography.displaySmall.copy(textDirection = TextDirection.Content),
    headlineLarge = baseTypography.headlineLarge.copy(textDirection = TextDirection.Content),
    headlineMedium = baseTypography.headlineMedium.copy(textDirection = TextDirection.Content),
    headlineSmall = baseTypography.headlineSmall.copy(textDirection = TextDirection.Content),
    titleLarge = baseTypography.titleLarge.copy(textDirection = TextDirection.Content),
    titleMedium = baseTypography.titleMedium.copy(textDirection = TextDirection.Content),
    titleSmall = baseTypography.titleSmall.copy(textDirection = TextDirection.Content),
    bodyLarge = baseTypography.bodyLarge.copy(textDirection = TextDirection.Content),
    bodyMedium = baseTypography.bodyMedium.copy(textDirection = TextDirection.Content),
    bodySmall = baseTypography.bodySmall.copy(textDirection = TextDirection.Content),
    labelLarge = baseTypography.labelLarge.copy(textDirection = TextDirection.Content),
    labelMedium = baseTypography.labelMedium.copy(textDirection = TextDirection.Content),
    labelSmall = baseTypography.labelSmall.copy(textDirection = TextDirection.Content)
)

