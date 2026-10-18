package com.example.firebase

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object VioraCrashlytics {
    private const val TAG = "VioraCrashlytics"

    private fun getCrashlytics(): FirebaseCrashlytics? {
        return try {
            FirebaseCrashlytics.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    fun setUserId(userId: String) {
        try {
            getCrashlytics()?.setUserId(userId)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to set userId: ${e.message}")
        }
    }

    fun setCustomKey(key: String, value: String) {
        try {
            getCrashlytics()?.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to set custom key: ${e.message}")
        }
    }

    fun setCustomKey(key: String, value: Boolean) {
        try {
            getCrashlytics()?.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to set custom key: ${e.message}")
        }
    }

    fun setCustomKey(key: String, value: Int) {
        try {
            getCrashlytics()?.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to set custom key: ${e.message}")
        }
    }

    fun log(message: String) {
        try {
            getCrashlytics()?.log(message)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to log message: ${e.message}")
        }
    }

    /**
     * Record non-fatal exception with business context tags
     */
    fun recordException(throwable: Throwable, contextTag: String? = null) {
        try {
            val crashlytics = getCrashlytics() ?: return
            contextTag?.let {
                crashlytics.setCustomKey("error_context", it)
            }
            crashlytics.recordException(throwable)
            Log.e(TAG, "Recorded non-fatal exception: [${contextTag ?: "Unknown"}] ${throwable.message}")
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to record non-fatal exception: ${e.message}")
        }
    }
}

