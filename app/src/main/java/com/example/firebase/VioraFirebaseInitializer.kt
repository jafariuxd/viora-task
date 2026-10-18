package com.example.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object VioraFirebaseInitializer {
    private const val TAG = "VioraFirebase"

    @Volatile
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                val apps = FirebaseApp.getApps(context)
                if (apps.isEmpty()) {
                    val app = FirebaseApp.initializeApp(context)
                    if (app == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:290843474560:android:511cc555da00a8b6180465")
                            .setApiKey("AIzaSyAs-NsIKSjf0HtM4-Qxl5RNWAXx-jsG20I")
                            .setProjectId("viora-task")
                            .setStorageBucket("viora-task.firebasestorage.app")
                            .setGcmSenderId("290843474560")
                            .build()
                        FirebaseApp.initializeApp(context.applicationContext, options)
                    }
                }
                isInitialized = true
                Log.i(TAG, "Firebase successfully initialized.")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firebase: ${e.message}")
            }
        }
    }
}
