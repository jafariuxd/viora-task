package com.example


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

import android.os.Bundle
import com.example.sync.SyncManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VioraTaskViewModel
import com.example.ui.components.ApiInspectorModalOverlay
import com.example.ui.components.TopBanner
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
  private val viewModel: VioraTaskViewModel by viewModels()


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
        SyncManager.startPeriodicSync(this)
    com.example.network.viora.VioraNetworkModule.init(this)
    
    val openDailyBrief = isDailyBriefIntent(intent)
    handleIntent(intent)
    
    val prefs: SharedPreferences = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
    val lastCrash = prefs.getString("last_crash", null)
    
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        prefs.edit().putString("last_crash", sw.toString()).commit()
        defaultHandler?.uncaughtException(thread, exception)
    }

    enableEdgeToEdge()

    setContent {
      val crashText = remember { mutableStateOf(lastCrash) }
      if (crashText.value != null) {
          AlertDialog(
              onDismissRequest = { crashText.value = null; prefs.edit().remove("last_crash").apply() },
              title = { Text("App Crashed Last Time") },
              text = { Text(crashText.value ?: "") },
              confirmButton = {
                  TextButton(onClick = { crashText.value = null; prefs.edit().remove("last_crash").apply() }) {
                      Text("Dismiss")
                  }
              }
          )
      }
      MyApplicationTheme {
        val focusManager = LocalFocusManager.current

        Box(modifier = Modifier.fillMaxSize()) {
            AppNavGraph(
              startDestinationOverride = if (openDailyBrief) "daily_brief" else null,
              viewModel = viewModel,
              modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
            )

            val currentMessage by viewModel.messages.collectAsState(initial = null)
            var activeEvent by remember { mutableStateOf<com.example.model.MessageEvent?>(null) }

            LaunchedEffect(currentMessage) {
                currentMessage?.let {
                    activeEvent = it
                }
            }

            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                TopBanner(
                    event = activeEvent,
                    onDismiss = { activeEvent = null }
                )
            }

            ApiInspectorModalOverlay()
        }

      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadState()
    com.example.widget.DailyBriefWidgetReceiver.updateWidget(this)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun isDailyBriefIntent(intent: Intent?): Boolean {
    if (intent == null) return false
    return intent.getBooleanExtra("OPEN_DAILY_BRIEF", false) ||
            intent.action == "com.example.ACTION_OPEN_DAILY_BRIEF"
  }

  private fun handleIntent(intent: Intent?) {
    if (intent == null) return
    if (intent.getBooleanExtra("QUICK_ADD", false) || intent.action == "com.example.ACTION_QUICK_ADD") {
        viewModel.triggerQuickAdd()
        intent.removeExtra("QUICK_ADD")
    }
    if (isDailyBriefIntent(intent)) {
        viewModel.triggerDailyBrief()
        intent.removeExtra("OPEN_DAILY_BRIEF")
    }
  }
}
