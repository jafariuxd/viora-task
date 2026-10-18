package com.example.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object VioraAnalytics {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        } catch (e: Exception) {
            android.util.Log.e("VioraAnalytics", "Failed to initialize FirebaseAnalytics: ${e.message}")
        }
    }

    fun setUserId(userId: String?) {
        try {
            firebaseAnalytics?.setUserId(userId)
        } catch (e: Exception) {
            android.util.Log.e("VioraAnalytics", "Error setting userId: ${e.message}")
        }
    }

    fun setUserProperty(name: String, value: String) {
        try {
            firebaseAnalytics?.setUserProperty(name, value)
        } catch (e: Exception) {
            android.util.Log.e("VioraAnalytics", "Error setting user property: ${e.message}")
        }
    }

    fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            firebaseAnalytics?.logEvent(eventName, params)
            android.util.Log.d("VioraAnalytics", "Event logged: $eventName")
        } catch (e: Exception) {
            android.util.Log.e("VioraAnalytics", "Error logging event $eventName: ${e.message}")
        }
    }

    // Pre-defined key business events
    fun logTaskCreated(taskId: String, hasDueDate: Boolean, hasAssignee: Boolean, category: String? = null) {
        val bundle = Bundle().apply {
            putString("task_id", taskId)
            putBoolean("has_due_date", hasDueDate)
            putBoolean("has_assignee", hasAssignee)
            category?.let { putString("category", it) }
        }
        logEvent("task_created", bundle)
    }

    fun logTaskCompleted(taskId: String) {
        val bundle = Bundle().apply {
            putString("task_id", taskId)
        }
        logEvent("task_completed", bundle)
    }

    fun logTaskDeleted(taskId: String) {
        val bundle = Bundle().apply {
            putString("task_id", taskId)
        }
        logEvent("task_deleted", bundle)
    }

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logQuickAddTriggered() {
        logEvent("quick_add_triggered")
    }

    fun logDailyBriefViewed() {
        logEvent("daily_brief_viewed")
    }

    fun logMaintenanceEncountered() {
        logEvent("maintenance_mode_encountered")
    }

    fun logForceUpdateEncountered(currentVersion: Long, minVersion: Long) {
        val bundle = Bundle().apply {
            putLong("current_version", currentVersion)
            putLong("min_version", minVersion)
        }
        logEvent("force_update_prompted", bundle)
    }

    fun logSoftUpdatePrompted(currentVersion: Long, latestVersion: Long) {
        val bundle = Bundle().apply {
            putLong("current_version", currentVersion)
            putLong("latest_version", latestVersion)
        }
        logEvent("soft_update_prompted", bundle)
    }
}
