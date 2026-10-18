package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.firebase.VioraCrashlytics
import com.google.android.play.core.review.ReviewManagerFactory

object VioraInAppReviewManager {
    private const val TAG = "VioraInAppReview"
    private const val PREFS_NAME = "viora_review_prefs"
    private const val KEY_ACTIONS_COUNT = "key_actions_count"
    private const val KEY_LAST_PROMPT_MILLIS = "key_last_prompt_millis"

    // Prompt threshold: e.g. after completing 5 tasks or significant actions
    private const val ACTIONS_THRESHOLD = 5
    // Minimum 7 days between prompt attempts
    private const val MIN_INTERVAL_MILLIS = 7L * 24 * 60 * 60 * 1000

    fun recordSignificantAction(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_ACTIONS_COUNT, 0) + 1
        prefs.edit().putInt(KEY_ACTIONS_COUNT, count).apply()
    }

    fun maybePromptReview(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_ACTIONS_COUNT, 0)
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT_MILLIS, 0L)
        val now = System.currentTimeMillis()

        if (count >= ACTIONS_THRESHOLD && (now - lastPrompt) > MIN_INTERVAL_MILLIS) {
            launchInAppReview(activity) {
                // Reset count and record prompt timestamp
                prefs.edit()
                    .putInt(KEY_ACTIONS_COUNT, 0)
                    .putLong(KEY_LAST_PROMPT_MILLIS, now)
                    .apply()
            }
        }
    }

    fun launchInAppReview(activity: Activity, onComplete: (() -> Unit)? = null) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        Log.d(TAG, "In-App Review flow finished.")
                        onComplete?.invoke()
                    }
                } else {
                    Log.w(TAG, "In-App Review request failed: ${task.exception?.message}")
                    task.exception?.let { VioraCrashlytics.recordException(it) }
                    onComplete?.invoke()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching review: ${e.message}", e)
            VioraCrashlytics.recordException(e)
            onComplete?.invoke()
        }
    }
}
