package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.R
import com.example.ui.utils.Quotes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

const val ACTION_UPDATE_QUOTE_SCHEDULED = "com.example.widget.ACTION_UPDATE_QUOTE_SCHEDULED"

class QuoteWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val quoteText = prefs[stringPreferencesKey("quote_text")] ?: "Either you run the day, or the day runs you."
                val quoteAuthor = prefs[stringPreferencesKey("quote_author")] ?: "Jim Rohn"

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF1C1C1E))
                        .padding(20.dp)
                        .cornerRadius(24.dp)
                        .clickable(actionRunCallback<RefreshQuoteAction>()),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_format_quote),
                                contentDescription = null,
                                modifier = GlanceModifier.size(28.dp)
                            )
                            Spacer(modifier = GlanceModifier.height(12.dp))
                            Text(
                                text = quoteText,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Text(
                            text = "— $quoteAuthor",
                            style = TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.5f)),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class RefreshQuoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val quote = Quotes.getRandom()
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[stringPreferencesKey("quote_text")] = quote.first
                this[stringPreferencesKey("quote_author")] = quote.second
            }
        }
        QuoteWidget().update(context, glanceId)
    }
}

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextQuoteAlarm(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        scheduleNextQuoteAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_QUOTE_SCHEDULED || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (intent.action == ACTION_UPDATE_QUOTE_SCHEDULED) {
                        val quote = Quotes.getRandom()
                        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(QuoteWidget::class.java)
                        glanceIds.forEach { glanceId ->
                            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                                prefs.toMutablePreferences().apply {
                                    this[stringPreferencesKey("quote_text")] = quote.first
                                    this[stringPreferencesKey("quote_author")] = quote.second
                                }
                            }
                            QuoteWidget().update(context, glanceId)
                        }
                    }
                    scheduleNextQuoteAlarm(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        fun scheduleNextQuoteAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, QuoteWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_QUOTE_SCHEDULED
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            val cal6am = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val cal6pm = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val nextAlarmTime: Long = when {
                now < cal6am.timeInMillis -> cal6am.timeInMillis
                now < cal6pm.timeInMillis -> cal6pm.timeInMillis
                else -> {
                    cal6am.add(Calendar.DAY_OF_YEAR, 1)
                    cal6am.timeInMillis
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent)
            }
        }
    }
}
