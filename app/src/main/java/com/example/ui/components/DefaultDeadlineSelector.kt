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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VioraNeonLime

@Composable
fun DefaultDeadlineSelector(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    customDays: Int,
    onCustomDaysChanged: (Int) -> Unit,
    textColor: Color = Color.White,
    unselectedTextColor: Color = Color(0xFFCCCCCC),
    borderColor: Color = Color(0xFF333333),
    selectedBackgroundColor: Color = VioraNeonLime,
    selectedItemTextColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    val options = listOf("Daily", "Weekly", "Monthly", "Custom")

    Column(modifier = modifier) {
        // Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, borderColor, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (isSelected) selectedBackgroundColor else Color.Transparent)
                        .clickable { onOptionSelected(option) }
                        .then(
                            if (index != options.size - 1 && !isSelected && selectedOption != options[index + 1])
                                Modifier.border(0.5.dp, borderColor) // Right border if not last and not next to selected
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = selectedItemTextColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = option,
                            color = if (isSelected) selectedItemTextColor else unselectedTextColor,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (selectedOption == "Custom") {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Your unscheduled tasks will due in",
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemHeight = 64.dp
                Box(modifier = Modifier.height(itemHeight * 5)) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = listState),
                        contentPadding = PaddingValues(vertical = itemHeight * 2),
                        modifier = Modifier.width(60.dp)
                    ) {
                        items(14) { index ->
                            val day = index + 1
                            val distance = kotlin.math.abs(centerIndex - index)

                            val fontSize = when (distance) {
                                0 -> 48.sp
                                1 -> 32.sp
                                else -> 24.sp
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
                                    fontSize = fontSize,
                                    color = color,
                                    fontWeight = fontWeight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Days",
                    fontSize = 24.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
