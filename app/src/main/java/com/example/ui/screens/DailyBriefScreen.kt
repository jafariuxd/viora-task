package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.VioraTaskViewModel
import com.example.viewmodel.AgendaViewModel
import com.example.ui.utils.shimmerEffect
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BriefSection {
    SUMMARY,
    UP_NEXT,
    SCHEDULE,
    TASKS,
    QUOTE,
    FOCUS_TIP
}

data class BriefTheme(
    val badgeText: String,
    val badgeColor: Color,
    val badgeContentColor: Color,
    val cardBorder: Color,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyBriefScreen(
    taskViewModel: VioraTaskViewModel,
    agendaViewModel: AgendaViewModel,
    onClose: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val events by agendaViewModel.events.collectAsState()
    val isAuthorized by agendaViewModel.isAuthorized.collectAsState()
    
    val context = LocalContext.current
    
    var visible by remember { mutableStateOf(false) }
    
    var aiGreeting by remember { mutableStateOf("") }
    var aiSummary by remember { mutableStateOf("") }
    var aiInsight by remember { mutableStateOf("") }
    var aiFocusTip by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(true) }
    
    var activeTheme by remember { 
        mutableStateOf(
            BriefTheme(
                badgeText = "VIORA BRIEFING",
                badgeColor = VioraNeonLime,
                badgeContentColor = Color.Black,
                cardBorder = Color(0xFF38383E),
                gradientColors = listOf(Color(0xFF28282C), Color(0xFF1E1E22))
            )
        ) 
    }
    
    var sectionOrder by remember { mutableStateOf<List<BriefSection>>(emptyList()) }

    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    
    val timeOfDay = when (hour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
    
    val currentDateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    
    val todayTasks = tasks.filter { it.daysLeft == 0 && it.status != com.example.model.TaskStatus.DONE }
    val overdueTasks = tasks.filter { it.daysLeft < 0 && it.status != com.example.model.TaskStatus.DONE }
    
    val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val todayEvents = events.filter { it.originalDateTime.startsWith(todayDateString) && !it.isPast }

    LaunchedEffect(Unit) {
        // Delay to simulate dynamic AI calculation
        kotlinx.coroutines.delay(1000)
        
        // Random Theme per entry
        val themePresets = listOf(
            BriefTheme(
                badgeText = "VIORA BRIEFING",
                badgeColor = VioraNeonLime,
                badgeContentColor = Color.Black,
                cardBorder = Color(0xFF38383E),
                gradientColors = listOf(Color(0xFF28282C), Color(0xFF1E1E22))
            ),
            BriefTheme(
                badgeText = "AI FOCUS MATRIX",
                badgeColor = Color(0xFF0A84FF),
                badgeContentColor = Color.White,
                cardBorder = Color(0xFF1F2F45),
                gradientColors = listOf(Color(0xFF152233), Color(0xFF101826))
            ),
            BriefTheme(
                badgeText = "DAILY STRATEGY",
                badgeColor = Color(0xFF30D158),
                badgeContentColor = Color.Black,
                cardBorder = Color(0xFF1F3A28),
                gradientColors = listOf(Color(0xFF142B1E), Color(0xFF0F1E15))
            ),
            BriefTheme(
                badgeText = "POWER OVERVIEW",
                badgeColor = Color(0xFFFF9F0A),
                badgeContentColor = Color.Black,
                cardBorder = Color(0xFF3D2D1B),
                gradientColors = listOf(Color(0xFF2A1E14), Color(0xFF1C140D))
            )
        )
        activeTheme = themePresets.random()

        // Dynamic Greetings
        val greetingsMorning = listOf(
            "Rise and shine!", 
            "Good morning, let's make today count.", 
            "A fresh start today!", 
            "Morning! Ready to tackle the day?",
            "Great morning! Time to excel.",
            "Morning inspiration incoming."
        )
        val greetingsAfternoon = listOf(
            "Good afternoon!", 
            "Halfway there!", 
            "Hope your day is going great.", 
            "Afternoon check-in!",
            "Keep up the momentum!",
            "Mid-day focus boost!"
        )
        val greetingsEvening = listOf(
            "Good evening!", 
            "Winding down?", 
            "Reflect on a productive day.", 
            "Evening check-in!",
            "Wrapping up today's wins."
        )
        
        aiGreeting = when (timeOfDay) {
            "Morning" -> greetingsMorning.random()
            "Afternoon" -> greetingsAfternoon.random()
            else -> greetingsEvening.random()
        }
        
        // Dynamic AI Summary
        val summaryPrefixes = listOf(
            "Here is your real-time briefing for today:",
            "Your personalized productivity outline:",
            "Today's strategic overview:",
            "Here is your AI-analyzed daily roadmap:"
        )
        
        val summaryStr = buildString {
            append(summaryPrefixes.random())
            append(" ")
            
            if (todayTasks.isEmpty() && todayEvents.isEmpty()) {
                val clearDayStrs = listOf(
                    "Your schedule is completely clear today! A perfect time to rest, read, or plan ahead.",
                    "No pending tasks or meetings found. Enjoy a well-deserved breather!",
                    "It looks like a quiet day. Take time to focus on strategic long-term ideas."
                )
                append(clearDayStrs.random())
            } else {
                if (todayTasks.isNotEmpty() && todayEvents.isNotEmpty()) {
                    val busyStrs = listOf(
                        "You have ${todayTasks.size} key tasks on your plate and ${todayEvents.size} scheduled meetings. Let's get to work!",
                        "A dynamic day ahead with ${todayEvents.size} events and ${todayTasks.size} focus tasks ready for execution.",
                        "Balancing ${todayTasks.size} tasks and ${todayEvents.size} meetings today. Pace yourself and stay focused!"
                    )
                    append(busyStrs.random())
                } else if (todayTasks.isNotEmpty()) {
                    val tasksStrs = listOf(
                        "Focus mode on! You have ${todayTasks.size} tasks to complete today.",
                        "No meetings today, giving you uninterrupted time to knock out your ${todayTasks.size} tasks.",
                        "A solid day for deep work with ${todayTasks.size} tasks waiting for you."
                    )
                    append(tasksStrs.random())
                } else {
                    val eventsStrs = listOf(
                        "Your day is driven by your schedule: ${todayEvents.size} events to attend.",
                        "No standalone tasks, but ${todayEvents.size} events are on your calendar.",
                        "Get ready for ${todayEvents.size} meetings today. Make every conversation count!"
                    )
                    append(eventsStrs.random())
                }
                
                if (overdueTasks.isNotEmpty()) {
                    val overdueStrs = listOf(
                        " Just a heads up, you have ${overdueTasks.size} overdue tasks to catch up on.",
                        " Don't forget the ${overdueTasks.size} tasks lingering from previous days.",
                        " Also, ${overdueTasks.size} older items need your attention when you find a moment."
                    )
                    append(overdueStrs.random())
                }
            }
        }
        
        aiSummary = summaryStr
        
        // Dynamic Quotes & Mindset Pool
        val quotes = listOf(
            "“The secret of getting ahead is getting started.” – Mark Twain",
            "“Focus on being productive instead of busy.” – Tim Ferriss",
            "“Amateurs sit and wait for inspiration, the rest of us just get up and go to work.” – Stephen King",
            "“It’s not always that we need to do more but rather that we need to focus on less.” – Nathan W. Morris",
            "“You don't have to see the whole staircase, just take the first step.” – Martin Luther King Jr.",
            "“Action is the foundational key to all success.” – Pablo Picasso",
            "“Do the hard jobs first. The easy jobs will take care of themselves.” – Dale Carnegie",
            "“The key is in not spending time, but in investing it.” – Stephen R. Covey",
            "“Simplicity boils down to two steps: Identify the essential. Eliminate the rest.” – Leo Babauta",
            "“Either you run the day or the day runs you.” – Jim Rohn",
            "“Small daily improvements over time lead to stunning results.” – Robin Sharma"
        )
        aiInsight = quotes.random()

        // Dynamic Focus Tips
        val focusTips = listOf(
            "Block out 45 minutes of uninterrupted deep focus before checking messages today.",
            "Tackle your hardest priority early in the day when your mental bandwidth is highest.",
            "Take a 5-minute breather between meetings to reset your clarity.",
            "Batch small administrative tasks into a single 25-minute sprint.",
            "Review your progress at mid-day and adjust non-essential deadlines.",
            "Protect your focus time: keep meetings strictly focused on actionable outcomes."
        )
        aiFocusTip = focusTips.random()

        // Dynamic Section Layout Ordering per entry
        val availableSections = mutableListOf<BriefSection>()
        availableSections.add(BriefSection.SUMMARY)
        
        if (todayEvents.isNotEmpty()) {
            availableSections.add(BriefSection.UP_NEXT)
        }
        if (todayEvents.isNotEmpty()) {
            availableSections.add(BriefSection.SCHEDULE)
        }
        if (todayTasks.isNotEmpty() || overdueTasks.isNotEmpty()) {
            availableSections.add(BriefSection.TASKS)
        }
        availableSections.add(BriefSection.QUOTE)
        availableSections.add(BriefSection.FOCUS_TIP)

        // Keep SUMMARY first, then shuffle the remaining sections for layout variety
        val remainingShuffled = availableSections.filter { it != BriefSection.SUMMARY }.shuffled()
        sectionOrder = listOf(BriefSection.SUMMARY) + remainingShuffled
        
        isGenerating = false
        visible = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Daily Brief", 
                        fontFamily = SFProDisplayFontFamily, 
                        fontWeight = FontWeight.Medium, 
                        fontSize = 16.sp, 
                        color = Color.Gray
                    ) 
                },
                navigationIcon = {
                    com.example.ui.components.VioraHeaderIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = onClose
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // Header Greeting
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { 40 })
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmerEffect()
                        )
                    } else {
                        Text(
                            text = aiGreeting,
                            fontFamily = SFProDisplayFontFamily,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 42.sp,
                            letterSpacing = (-1).sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDateStr,
                        fontFamily = SFProDisplayFontFamily,
                        fontSize = 18.sp,
                        color = activeTheme.badgeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Render Sections in Dynamic Random Order
            if (isGenerating) {
                // Loading Shimmer Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(colors = activeTheme.gradientColors))
                        .border(1.dp, activeTheme.cardBorder, RoundedCornerShape(28.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = activeTheme.badgeColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Daily Insights...", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            CircularProgressIndicator(color = activeTheme.badgeColor, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth(0.85f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Box(modifier = Modifier.fillMaxWidth(0.65f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                }
            } else {
                sectionOrder.forEachIndexed { index, section ->
                    val delayMs = 100 + (index * 120)
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500, delayMillis = delayMs)) + slideInVertically(tween(500, delayMillis = delayMs), initialOffsetY = { 40 })
                    ) {
                        when (section) {
                            BriefSection.SUMMARY -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(Brush.linearGradient(colors = activeTheme.gradientColors))
                                        .border(1.dp, activeTheme.cardBorder, RoundedCornerShape(28.dp))
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(activeTheme.badgeColor)
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = activeTheme.badgeText,
                                                    color = activeTheme.badgeContentColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = aiSummary,
                                            color = Color(0xFFE0E0E0),
                                            fontSize = 16.sp,
                                            fontFamily = SFProDisplayFontFamily,
                                            lineHeight = 24.sp
                                        )
                                    }
                                }
                            }

                            BriefSection.UP_NEXT -> {
                                if (todayEvents.isNotEmpty()) {
                                    val nextEvent = todayEvents.first()
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Up Next",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFProDisplayFontFamily
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0A84FF))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Bolt,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        "MEETING FOCUS",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = SFProDisplayFontFamily,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (nextEvent.htmlLink.isNotEmpty()) {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(nextEvent.htmlLink)))
                                                    }
                                                },
                                            shape = RoundedCornerShape(28.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF0A84FF).copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Event,
                                                        contentDescription = null,
                                                        tint = Color(0xFF0A84FF),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "UPCOMING MEETING",
                                                        color = Color(0xFF0A84FF),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = SFProDisplayFontFamily,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = nextEvent.title,
                                                        color = Color.Black,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = SFProDisplayFontFamily,
                                                        maxLines = 2,
                                                        lineHeight = 22.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFFF1F3F5))
                                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Schedule,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFF0A84FF),
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Text(
                                                                    text = nextEvent.time,
                                                                    color = Color(0xFF0A84FF),
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontFamily = SFProDisplayFontFamily
                                                                )
                                                            }
                                                        }
                                                        if (nextEvent.isOnline) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color(0xFFE8F5E9))
                                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Google Meet",
                                                                    color = Color(0xFF2E7D32),
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    fontFamily = SFProDisplayFontFamily
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Icon(
                                                    imageVector = Icons.Rounded.ChevronRight,
                                                    contentDescription = "Open",
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            BriefSection.SCHEDULE -> {
                                if (todayEvents.isNotEmpty()) {
                                    Column {
                                        Text(
                                            "Today's Schedule",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily,
                                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 8.dp)
                                        )
                                        
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(28.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                                todayEvents.forEachIndexed { idx, event ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (event.htmlLink.isNotEmpty()) {
                                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.htmlLink)))
                                                                }
                                                            }
                                                            .padding(horizontal = 20.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = event.time.substringBefore(" -").substringBefore("AM").substringBefore("PM").trim(),
                                                            color = Color.Gray,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.width(50.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .width(4.dp)
                                                                .height(32.dp)
                                                                .clip(RoundedCornerShape(2.dp))
                                                                .background(Color(0xFF0A84FF))
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(event.title, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                            if (event.isOnline) {
                                                                Text("Online Meeting", color = Color.Gray, fontSize = 13.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            BriefSection.TASKS -> {
                                if (todayTasks.isNotEmpty() || overdueTasks.isNotEmpty()) {
                                    Column {
                                        Text(
                                            "Pending Tasks",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily,
                                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 8.dp)
                                        )
                                        
                                        val displayTasks = (overdueTasks + todayTasks)
                                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                            displayTasks.forEach { task ->
                                                TaskListItemCard(
                                                    task = task,
                                                    onStatusClick = {},
                                                    onCardClick = {}
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            BriefSection.QUOTE -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(Brush.sweepGradient(
                                            colors = listOf(Color(0xFF323236), Color(0xFF242428), Color(0xFF323236))
                                        ))
                                        .border(1.dp, Color(0xFF3F3F44), RoundedCornerShape(28.dp))
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.FormatQuote,
                                                contentDescription = null,
                                                tint = activeTheme.badgeColor,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Text(
                                                text = "DAILY MINDSET",
                                                color = activeTheme.badgeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFProDisplayFontFamily,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = aiInsight,
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontFamily = SFProDisplayFontFamily,
                                            lineHeight = 25.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            BriefSection.FOCUS_TIP -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(Color(0xFF1E1E22))
                                        .border(1.dp, Color(0xFF323238), RoundedCornerShape(28.dp))
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(activeTheme.badgeColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.TipsAndUpdates,
                                                contentDescription = null,
                                                tint = activeTheme.badgeColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "PRODUCTIVITY TIP",
                                                color = activeTheme.badgeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFProDisplayFontFamily,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = aiFocusTip,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontFamily = SFProDisplayFontFamily,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
