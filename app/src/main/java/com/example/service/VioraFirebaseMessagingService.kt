package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.viora.RegisterDeviceTokenDto
import com.example.network.viora.VioraNetworkModule
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class VioraFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "viora_notifications_channel"
        private const val SYSTEM_CHANNEL_ID = "viora_system_channel"
        private const val TAG = "VioraFCM"

        private val _messageEvents = MutableSharedFlow<RemoteMessage>(extraBufferCapacity = 10)
        val messageEvents: SharedFlow<RemoteMessage> = _messageEvents.asSharedFlow()

        /**
         * Safely registers the current FCM device token if Google Play Services is available.
         * Gracefully handles emulators or devices without Play Services / FCM registration.
         */
        fun initializeAndSubscribe(context: Context) {
            try {
                // Clear any leftover queued topic operations to prevent background TopicsSyncTask errors
                try {
                    val appidPrefs = context.getSharedPreferences("com.google.android.gms.appid", Context.MODE_PRIVATE)
                    appidPrefs.edit().remove("topic_operation_queue").apply()
                } catch (_: Exception) {}

                // Verify Google Play Services is available
                val gApi = GoogleApiAvailability.getInstance()
                val resultCode = gApi.isGooglePlayServicesAvailable(context)
                if (resultCode != ConnectionResult.SUCCESS) {
                    android.util.Log.i(TAG, "Google Play Services not available for FCM (code=$resultCode). Skipping token registration.")
                    return
                }

                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        if (!token.isNullOrBlank()) {
                            android.util.Log.d(TAG, "FCM token retrieved successfully: $token")
                            sendTokenToServer(context, token)
                        }
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.w(TAG, "FCM token retrieval not available: ${e.message}")
                    }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "FCM initialization skipped: ${e.message}")
            }
        }

        fun sendTokenToServer(context: Context, token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tokenManager = VioraNetworkModule.getTokenManager()
                    if (tokenManager?.getAccessToken().isNullOrBlank()) {
                        android.util.Log.d("VioraFCM", "User not logged in yet, skipping token registration")
                        return@launch
                    }
                    val api = VioraNetworkModule.api
                    val res = api.registerDeviceToken(
                        RegisterDeviceTokenDto(
                            token = token,
                            platform = "android"
                        )
                    )
                    android.util.Log.d("VioraFCM", "FCM token registered successfully: ${res.success}")
                } catch (e: Exception) {
                    android.util.Log.e("VioraFCM", "Failed to register FCM token: ${e.message}")
                }
            }
        }

        fun unregisterTokenFromServer(context: Context, onComplete: (() -> Unit)? = null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val gApi = GoogleApiAvailability.getInstance()
                    if (gApi.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                        onComplete?.invoke()
                        return@launch
                    }
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful && !task.result.isNullOrBlank()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val api = VioraNetworkModule.api
                                    api.deleteDeviceToken(task.result)
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Error deleting device token: ${e.message}")
                                } finally {
                                    onComplete?.invoke()
                                }
                            }
                        } else {
                            onComplete?.invoke()
                        }
                    }
                } catch (e: Exception) {
                    onComplete?.invoke()
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("VioraFCM", "FCM token refreshed: $token")
        sendTokenToServer(this, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        android.util.Log.d("VioraFCM", "Message received: ${remoteMessage.data}")

        _messageEvents.tryEmit(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Viora"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val actionUrl = remoteMessage.data["actionUrl"]

        showNotification(title, body, actionUrl, remoteMessage.data)
    }

    private fun showNotification(title: String, body: String, actionUrl: String?, dataPayload: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Viora Tasks & Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task assignments, team invites, and reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!actionUrl.isNullOrBlank()) {
                data = Uri.parse(actionUrl)
                putExtra("actionUrl", actionUrl)
            }
            // Pass full FCM notification data payload to support rich deep linking
            dataPayload.forEach { (key, value) ->
                putExtra(key, value)
            }
            putExtra("notification_title", title)
            putExtra("notification_body", body)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.viora)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFB4FF00.toInt()) // VioraNeonLime
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
