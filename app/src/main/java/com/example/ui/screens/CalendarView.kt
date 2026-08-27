package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VioraNeonLime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalendarView(
    events: List<AgendaItemData>,
    modifier: Modifier = Modifier
) {
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Update selectedDate if we want today
    val goToToday = {
        selectedDate = Calendar.getInstance()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Schedule",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val monthOffset = page - initialPage
            val monthCalendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = SimpleDateFormat("MMMM", Locale.US).format(monthCalendar.time),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, VioraNeonLime, CircleShape)
                            .clickable { goToToday() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Today",
                            tint = VioraNeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Days of week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
                ) {
                    val days = listOf("Sa", "Su", "Mo", "Tu", "We", "Th", "Fr")
                    days.forEach { day ->
                        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            Text(text = day, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calendar grid
                val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) // Sun = 1, Mon = 2, ... Sat = 7
                // We want Sa = 0, Su = 1, Mo = 2, Tu = 3, We = 4, Th = 5, Fr = 6
                val startOffset = firstDayOfWeek % 7
                
                val totalCells = startOffset + daysInMonth
                val rows = (totalCells + 6) / 7
                
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
                    ) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val dayNumber = index - startOffset + 1
                            Box(
                                modifier = Modifier.size(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = monthCalendar.clone() as Calendar
                                    cellDate.set(Calendar.DAY_OF_MONTH, dayNumber)
                                    
                                    val isSelected = cellDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                                            cellDate.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
                                            
                                    val isToday = cellDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                                            cellDate.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                                            
                                    val isFriday = col == 6
                                    
                                    val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cellDate.time)
                                    val hasEvents = events.any { it.originalDateTime.startsWith(dateString) }
                                    
                                    val bgColor = when {
                                        isSelected -> VioraNeonLime
                                        isFriday -> Color(0xFFFF453A)
                                        else -> Color(0xFF2C2C2E)
                                    }
                                    
                                    val textColor = when {
                                        isSelected || isFriday -> Color.Black
                                        else -> Color.White
                                    }
                                    
                                    val outline = if (isToday && !isSelected) Modifier.border(1.dp, VioraNeonLime, CircleShape) else Modifier
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .then(outline)
                                            .clickable { selectedDate = cellDate },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = dayNumber.toString(), color = textColor, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                                            if (hasEvents) {
                                                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp).size(4.dp).clip(CircleShape).background(if(isSelected || isFriday) Color.Black else VioraNeonLime))
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Events", color = Color.Gray, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
        
        val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
        val selectedEvents = events.filter { it.originalDateTime.startsWith(selectedDateStr) }
        
        if (selectedEvents.isEmpty()) {
            Text(text = "No events for this date.", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(selectedEvents, key = { it.id }) { item ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    AgendaItem(item = item, onClick = {
                        if (item.htmlLink.isNotEmpty()) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.htmlLink))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
