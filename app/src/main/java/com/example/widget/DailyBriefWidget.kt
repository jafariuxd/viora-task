package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback

const val ACTION_UPDATE_DAILY_BRIEF_SCHEDULED = "com.example.widget.ACTION_UPDATE_DAILY_BRIEF_SCHEDULED"

class OpenDailyBriefAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_OPEN_DAILY_BRIEF"
            putExtra("OPEN_DAILY_BRIEF", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}

class DailyBriefWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyBriefWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMidnightUpdateAlarm(context)
        updateWidget(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        scheduleMidnightUpdateAlarm(context)
        updateWidget(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_UPDATE_DAILY_BRIEF_SCHEDULED ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_USER_PRESENT
        ) {
            updateWidget(context)
            scheduleMidnightUpdateAlarm(context)
        }
    }

    companion object {
        fun updateWidget(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(DailyBriefWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        DailyBriefWidget().update(context, glanceId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun scheduleMidnightUpdateAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DailyBriefWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_DAILY_BRIEF_SCHEDULED
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 2)
                set(Calendar.MILLISECOND, 0)
            }

            val nextMidnightTime = calendar.timeInMillis

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMidnightTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextMidnightTime, pendingIntent)
            }
        }
    }
}

class DailyBriefWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
            val currentDate = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C1E))
                    .cornerRadius(24.dp)
                    .clickable(actionRunCallback<OpenDailyBriefAction>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Viora",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFB4FF00)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = currentDay,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = currentDate,
                        style = TextStyle(
                            color = ColorProvider(Color.Gray),
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    Text(
                        text = "Tap to view brief",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFB4FF00)),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
