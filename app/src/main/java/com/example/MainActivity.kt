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
import com.example.ui.components.AppGateOverlay
import com.example.firebase.VioraAnalytics
import com.example.firebase.VioraCrashlytics
import com.example.firebase.VioraRemoteConfigManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
  private val viewModel: VioraTaskViewModel by viewModels()

  private val requestPermissionLauncher = registerForActivityResult(
      androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
  ) { isGranted ->
      if (isGranted) {
          android.util.Log.d("MainActivity", "Notification permission granted")
      } else {
          android.util.Log.w("MainActivity", "Notification permission denied")
      }
  }

  fun checkNotificationPermission() {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
          if (androidx.core.content.ContextCompat.checkSelfPermission(
                  this,
                  android.Manifest.permission.POST_NOTIFICATIONS
              ) != android.content.pm.PackageManager.PERMISSION_GRANTED
          ) {
              requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
          }
      }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
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

    try {
        SyncManager.startPeriodicSync(this)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error starting periodic sync: ${e.message}")
    }

    try {
        com.example.network.viora.VioraNetworkModule.init(this)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error initializing VioraNetworkModule: ${e.message}")
    }
    
    // Initialize Firebase Services
    try {
        com.example.firebase.VioraFirebaseInitializer.init(this)
        VioraAnalytics.init(this)
        VioraRemoteConfigManager.init(this)
        VioraCrashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        VioraCrashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        com.example.service.VioraFirebaseMessagingService.initializeAndSubscribe(this)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error initializing Firebase services: ${e.message}")
    }

    // Only prompt for notification permission on launch if user is already logged in
    val authPrefs = getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
    if (authPrefs.getBoolean("is_registered", false)) {
        checkNotificationPermission()
    }
    
    val openDailyBrief = isDailyBriefIntent(intent)
    try {
        handleIntent(intent)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error handling intent: ${e.message}")
    }

    enableEdgeToEdge(
        statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    )

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
            AppGateOverlay()
        }

      }
    }
  }

  override fun onResume() {
    super.onResume()
    try {
        viewModel.loadState()
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error in viewModel.loadState: ${e.message}")
    }
    try {
        com.example.widget.DailyBriefWidgetReceiver.updateWidget(this)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error updating widget: ${e.message}")
    }
    try {
        com.example.util.VioraInAppReviewManager.maybePromptReview(this)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error in maybePromptReview: ${e.message}")
    }
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

    // Handle incoming Universal App Links (viora.group) or custom scheme (viora://)
    val dataString = intent.dataString ?: intent.getStringExtra("actionUrl")
    if (!dataString.isNullOrBlank()) {
        val parsedDeepLink = com.example.util.VioraDeepLinkHandler.parse(dataString)
        if (parsedDeepLink !is com.example.util.VioraDeepLink.Unknown) {
            val enrichedLink = if (parsedDeepLink is com.example.util.VioraDeepLink.TeamInvite) {
                val extraTeamName = intent.getStringExtra("teamName")
                    ?: intent.getStringExtra("notification_title")?.removePrefix("Invited to ")?.trim()
                val extraInviteId = intent.getStringExtra("inviteId")
                val extraInviter = intent.getStringExtra("invitedBy") ?: intent.getStringExtra("inviter")
                parsedDeepLink.copy(
                    teamName = parsedDeepLink.teamName ?: extraTeamName,
                    inviteId = parsedDeepLink.inviteId ?: extraInviteId,
                    inviterName = parsedDeepLink.inviterName ?: extraInviter
                )
            } else parsedDeepLink

            viewModel.handleDeepLink(enrichedLink)
        }
    } else {
        // Fallback: check if raw team invite data was passed directly via FCM extras
        val teamId = intent.getStringExtra("teamId")
        if (!teamId.isNullOrBlank()) {
            val teamName = intent.getStringExtra("teamName")
                ?: intent.getStringExtra("notification_title")?.removePrefix("Invited to ")?.trim()
            val inviteId = intent.getStringExtra("inviteId")
            val inviter = intent.getStringExtra("invitedBy") ?: intent.getStringExtra("inviter")
            viewModel.handleDeepLink(
                com.example.util.VioraDeepLink.TeamInvite(
                    teamId = teamId,
                    teamName = teamName,
                    inviteId = inviteId,
                    inviterName = inviter
                )
            )
        } else {
            val taskId = intent.getStringExtra("taskId")
            if (!taskId.isNullOrBlank()) {
                viewModel.handleDeepLink(com.example.util.VioraDeepLink.TaskDetail(taskId = taskId))
            }
        }
    }
  }
}
