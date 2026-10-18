package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val TAG = "SyncManager"
    private const val SYNC_WORK_NAME = "VioraSyncWork"

    private fun getWorkManager(context: Context): WorkManager? {
        return try {
            if (!WorkManager.isInitialized()) {
                val config = Configuration.Builder()
                    .setMinimumLoggingLevel(Log.INFO)
                    .build()
                WorkManager.initialize(context.applicationContext, config)
            }
            WorkManager.getInstance(context)
        } catch (e: Exception) {
            try {
                WorkManager.getInstance(context)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to get WorkManager instance: ${e2.message}")
                null
            }
        }
    }

    fun startPeriodicSync(context: Context) {
        try {
            val workManager = getWorkManager(context) ?: return
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error starting periodic sync: ${e.message}")
        }
    }

    fun triggerImmediateSync(context: Context) {
        try {
            val workManager = getWorkManager(context) ?: return
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            workManager.enqueue(syncRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering immediate sync: ${e.message}")
        }
    }
}

