package com.example.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dynamicBottomSheetShape(sheetState: SheetState, defaultRadius: Dp = 28.dp): Shape {
    val density = LocalDensity.current
    val radius by remember(sheetState) {
        derivedStateOf {
            try {
                val offset = sheetState.requireOffset()
                val thresholdPx = with(density) { 48.dp.toPx() }
                val fraction = (offset / thresholdPx).coerceIn(0f, 1f)
                defaultRadius * fraction
            } catch (e: Exception) {
                defaultRadius
            }
        }
    }
    return RoundedCornerShape(topStart = radius, topEnd = radius)
}
