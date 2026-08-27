package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import com.example.ui.utils.animateEnter
import com.example.ui.utils.shimmerEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.AgendaViewModel


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AgendaScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel()
) {
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackProgress,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "agendaSpringBack"
    )

    PredictiveBackHandler { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                predictiveBackProgress = backEvent.progress * 0.75f
            }
            onClose()
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            predictiveBackProgress = 0f
        }
    }

    val rawContext = LocalContext.current
    val context = rawContext as Activity
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authIntent by viewModel.authIntent.collectAsState()
    val isFetchingMore by viewModel.isFetchingMore.collectAsState()
    val paginationError by viewModel.paginationError.collectAsState()
    val listState = rememberLazyListState()
    
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0 && lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }
    
    LaunchedEffect(isAtBottom) {
        if (isAtBottom && viewModel.hasMore) {
            viewModel.loadMore(context)
        }
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleAuthorizationResult(context, result.data)
        } else {
            viewModel.setError("Google Sign-In failed or was canceled. (Code: ${result.resultCode})\nPlease make sure you have added your app's SHA-1 to your Google Cloud Console / Firebase project.")
        }
    }

    LaunchedEffect(authIntent) {
        if (authIntent != null) {
            launcher.launch(authIntent!!)
            viewModel.clearAuthIntent()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.authorizeAndFetch(context)
    }
    
    var isCalendarView by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val progress = animatedBackProgress
                if (progress > 0f) {
                    val scale = 1f - (progress * 0.12f)
                    scaleX = scale
                    scaleY = scale
                    translationY = progress * 70.dp.toPx()
                    alpha = 1f - (progress * 0.35f)
                    shape = RoundedCornerShape((progress * 32).dp)
                    clip = true
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF333333), CircleShape)
                        .background(Color.Transparent)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF333333), CircleShape)
                        .background(Color.Transparent)
                        .clickable { isCalendarView = !isCalendarView },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCalendarView) Icons.Rounded.ViewAgenda else Icons.Rounded.CalendarToday,
                        contentDescription = "Toggle View",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (error != null && events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Error", color = Color.Red, fontSize = 16.sp)
                }
            } else if (events.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No events scheduled.", color = Color.Gray, fontSize = 16.sp)
                }
            } else if (events.isEmpty() && isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VioraNeonLime)
                }
            } else if (isCalendarView) {
                CalendarView(events = events)
            } else {
                Text(
                    text = "Schedule",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var hasScrolledToUpcoming by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            val groupedEvents = remember(events) {
                events.groupBy {  
                    try {
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it.originalDateTime.take(10))
                        java.text.SimpleDateFormat("MMMM", java.util.Locale.US).format(date!!)
                    } catch(e: Exception) { "Unknown" }
                }
            }

            // Scroll to next event
            androidx.compose.runtime.LaunchedEffect(groupedEvents) {
                if (events.isNotEmpty() && !hasScrolledToUpcoming) {
                    var targetIndex = 0
                    var found = false
                    for ((_, monthEvents) in groupedEvents) {
                        targetIndex++ // For the sticky header
                        for (event in monthEvents) {
                            if (!event.isPast) {
                                found = true
                                break
                            }
                            targetIndex++
                        }
                        if (found) break
                    }
                    if (found) {
                        // Scroll slightly above so we can see the header if it's the first item of the month
                        val scrollIndex = targetIndex
                        listState.animateScrollToItem(scrollIndex, -290)
                        hasScrolledToUpcoming = true
                    } else if (viewModel.hasMore && !isFetchingMore) {
                        viewModel.loadMore(context)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedEvents.forEach { (month, monthEvents) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = month,
                                color = Color.Gray.copy(alpha = 0.5f),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    itemsIndexed(monthEvents, key = { _, it -> it.id }) { index, item ->
                        val staggerDelay = if (index < 5) 250 + (index * 40) else 50
                        Box(modifier = Modifier.animateEnter(delayMillis = staggerDelay)) {
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
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                if (isFetchingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VioraNeonLime)
                        }
                    }
                }
                if (paginationError != null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { viewModel.clearPaginationError(); viewModel.loadMore(context) }, contentAlignment = Alignment.Center) {
                            Text(text = "Error: $paginationError (Tap to retry)", color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            }
        } // closes if-else

    } // closes Column
    
    // FAB inside BoxScope
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp)
            .size(64.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(VioraNeonLime)
            .clickable {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_INSERT)
                    intent.data = android.provider.CalendarContract.Events.CONTENT_URI
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
    }
} // closes Box
} // closes AgendaScreen

@Composable
fun AgendaItem(item: AgendaItemData, onClick: () -> Unit = {}) {
    val alpha = if (item.isPast) 0.5f else 1f
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !item.isPast) { onClick() }.alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rotated Text
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.day,
                style = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 84.sp,
                    fontFamily = com.example.ui.theme.SFProDisplayFontFamily,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    lineHeight = 60.sp,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                        alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Proportional,
                        trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                    )
                ),
                modifier = Modifier
                    .requiredWidth(120.dp)
                    .rotate(-90f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Vertical separator
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(120.dp)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (item.isOnline) VioraNeonLime else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.type,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.time,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.title,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = VioraNeonLime,
            modifier = Modifier.size(32.dp)
        )
    }
}
