package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Viora Task Colors
val VioraBackground = Color(0xFF000000)
val VioraWeatherCard = Color(0xFFD3E2FF)
val VioraCalendarCard = Color(0xFFCBEA97)
val VioraUnplannedCard = Color(0xFFFBE38A)
val VioraTaskCard = Color(0xFFFFFFFF)
val VioraDarkPill = Color(0xFF2A2B2D)
val VioraGrayText = Color(0xFF5E6064)
val VioraLightBlueBadge = Color(0xFFE5F1FF)
val VioraLightBlueText = Color(0xFF386B99)
val VioraDarkText = Color(0xFF1E2022)
val VioraNeonLime = Color(0xFFB4FF00)

// Status & Palette Color Pairs (Container + Content)
data class VioraColorPair(
    val container: Color,
    val content: Color
)

object VioraColors {
    // Red (To-Do / Urgent)
    val LightRed = VioraColorPair(
        container = Color(0xFFFFD8D8),
        content = Color(0xFF8A0000)
    )
    
    // Blue (In Progress)
    val Blue = VioraColorPair(
        container = Color(0xFFD6E3FF),
        content = Color(0xFF001B3E)
    )
    
    // Green (Done)
    val Green = VioraColorPair(
        container = Color(0xFFD4EFA5),
        content = Color(0xFF234B00)
    )
    
    // Yellow (Unplanned / Warning)
    val Yellow = VioraColorPair(
        container = Color(0xFFFBE38A),
        content = Color(0xFF5C4300)
    )

    fun forStatus(status: com.example.model.TaskStatus): VioraColorPair {
        return when (status) {
            com.example.model.TaskStatus.TODO -> LightRed
            com.example.model.TaskStatus.IN_PROGRESS -> Blue
            com.example.model.TaskStatus.DONE -> Green
        }
    }
}
