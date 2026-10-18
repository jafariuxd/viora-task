package com.example

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.firebase.VioraFirebaseInitializer

class VioraApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Firebase services
        try {
            VioraFirebaseInitializer.init(this)
        } catch (e: Exception) {
            Log.e("VioraApplication", "Firebase init failure: ${e.message}")
        }

        // 2. Initialize WorkManager
        try {
            if (!WorkManager.isInitialized()) {
                WorkManager.initialize(this, workManagerConfiguration)
            }
        } catch (e: Exception) {
            Log.e("VioraApplication", "WorkManager init failure: ${e.message}")
        }

        // 3. Initialize Rive runtime
        try {
            app.rive.runtime.kotlin.core.Rive.init(this)
        } catch (e: Exception) {
            Log.e("VioraApplication", "Rive init failure: ${e.message}")
        }

        // 4. Initialize UserAvatarRegistry
        try {
            com.example.util.UserAvatarRegistry.init(this)
        } catch (e: Exception) {
            Log.e("VioraApplication", "UserAvatarRegistry init failure: ${e.message}")
        }

        // 5. Create Notification Channels immediately on startup so notifications are enabled in Android system settings
        initNotificationChannels()
    }

    private fun initNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

                val taskChannel = android.app.NotificationChannel(
                    "viora_notifications_channel",
                    "Viora Tasks & Updates",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Task assignments, team invites, and reminders"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.parseColor("#B4FF00")
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                val systemChannel = android.app.NotificationChannel(
                    "viora_system_channel",
                    "Viora System & Reminders",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "System notifications and deadline alerts"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.parseColor("#B4FF00")
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                notificationManager.createNotificationChannels(listOf(taskChannel, systemChannel))
                Log.d("VioraApplication", "Notification channels initialized successfully")
            } catch (e: Exception) {
                Log.e("VioraApplication", "Failed to create notification channels: ${e.message}")
            }
        }
    }
}
