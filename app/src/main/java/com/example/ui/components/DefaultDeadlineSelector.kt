package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

@Composable
fun DefaultDeadlineSelector(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    customDays: Int,
    onCustomDaysChanged: (Int) -> Unit,
    textColor: Color = Color.White,
    unselectedTextColor: Color = Color(0xFF8E8E93),
    borderColor: Color = Color(0xFF2C2C2E),
    selectedBackgroundColor: Color = VioraNeonLime,
    selectedItemTextColor: Color = Color.Black,
    containerColor: Color = Color(0xFF1C1C1E),
    modifier: Modifier = Modifier
) {
    val options = listOf("Daily", "Weekly", "Monthly", "Custom")

    Column(modifier = modifier) {
        // Modern Segmented Control
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(22.dp),
            color = containerColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) selectedBackgroundColor else Color.Transparent)
                            .clickable { onOptionSelected(option) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = selectedItemTextColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = option,
                                fontFamily = SFProDisplayFontFamily,
                                color = if (isSelected) selectedItemTextColor else unselectedTextColor,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (selectedOption == "Custom") {
            Spacer(modifier = Modifier.height(28.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = containerColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tasks will default to due in",
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = unselectedTextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (customDays > 0) customDays - 1 else 2)

                    val centerIndex by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
                            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - viewportCenter) }
                            centerItem?.index ?: 0
                        }
                    }

                    LaunchedEffect(centerIndex) {
                        onCustomDaysChanged(centerIndex + 1)
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val itemHeight = 56.dp
                        Box(modifier = Modifier.height(itemHeight * 3)) {
                            LazyColumn(
                                state = listState,
                                flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = listState),
                                contentPadding = PaddingValues(vertical = itemHeight),
                                modifier = Modifier.width(72.dp)
                            ) {
                                items(30) { index ->
                                    val day = index + 1
                                    val distance = kotlin.math.abs(centerIndex - index)

                                    val fontSize = when (distance) {
                                        0 -> 40.sp
                                        1 -> 24.sp
                                        else -> 18.sp
                                    }
                                    val color = when (distance) {
                                        0 -> textColor
                                        1 -> unselectedTextColor
                                        else -> borderColor
                                    }
                                    val fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(itemHeight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontFamily = SFProDisplayFontFamily,
                                            fontSize = fontSize,
                                            color = color,
                                            fontWeight = fontWeight
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Days",
                            fontFamily = SFProDisplayFontFamily,
                            fontSize = 22.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


