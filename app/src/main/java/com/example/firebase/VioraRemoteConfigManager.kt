package com.example.firebase

import android.content.Context
import com.example.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RemoteAppConfig(
    val minVersionCode: Long = 1L,
    val latestVersionCode: Long = 1L,
    val updateUrl: String = "https://play.google.com/store/apps/details?id=com.viora.task",
    val releaseNotes: String = "",
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "ما در حال بهبود زیرساخت‌های سرویس ویورا هستیم. لطفاً دقایقی دیگر مجدداً تلاش کنید.",
    val supportUrl: String = "https://t.me/vioratask_support",
    val termsUrl: String = "https://viora.app/legal/terms",
    val privacyUrl: String = "https://viora.app/legal/privacy",
    val contactEmail: String = "support@viora.app"
)

sealed class AppStatus {
    object Normal : AppStatus()
    data class Maintenance(val message: String) : AppStatus()
    data class ForceUpdateRequired(
        val minVersionCode: Long,
        val latestVersionCode: Long,
        val updateUrl: String,
        val releaseNotes: String
    ) : AppStatus()
    data class OptionalUpdateAvailable(
        val latestVersionCode: Long,
        val updateUrl: String,
        val releaseNotes: String
    ) : AppStatus()
}

object VioraRemoteConfigManager {

    private const val TAG = "VioraRemoteConfig"

    // Remote config keys matching standard architecture
    const val KEY_MIN_VERSION_CODE = "min_version_code"
    const val KEY_LATEST_VERSION_CODE = "latest_version_code"
    const val KEY_UPDATE_URL = "update_url"
    const val KEY_RELEASE_NOTES = "release_notes"
    const val KEY_IS_MAINTENANCE_MODE = "is_maintenance_mode"
    const val KEY_MAINTENANCE_MESSAGE = "maintenance_message"
    const val KEY_SUPPORT_URL = "support_url"
    const val KEY_TERMS_URL = "terms_url"
    const val KEY_PRIVACY_URL = "privacy_url"

    private val _configState = MutableStateFlow(RemoteAppConfig())
    val configState: StateFlow<RemoteAppConfig> = _configState.asStateFlow()

    private val _appStatus = MutableStateFlow<AppStatus>(AppStatus.Normal)
    val appStatus: StateFlow<AppStatus> = _appStatus.asStateFlow()

    private var remoteConfig: FirebaseRemoteConfig? = null

    fun init(context: Context) {
        try {
            VioraFirebaseInitializer.init(context)
            remoteConfig = FirebaseRemoteConfig.getInstance().apply {
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0L else 3600L)
                    .build()
                setConfigSettingsAsync(configSettings)

                // Define local fallbacks/defaults
                val defaults = mapOf(
                    KEY_MIN_VERSION_CODE to 1L,
                    KEY_LATEST_VERSION_CODE to 1L,
                    KEY_UPDATE_URL to "https://play.google.com/store/apps/details?id=com.viora.task",
                    KEY_RELEASE_NOTES to "بهبود کارایی و رفع باگ‌های جزئی",
                    KEY_IS_MAINTENANCE_MODE to false,
                    KEY_MAINTENANCE_MESSAGE to "ما در حال بهبود زیرساخت‌های سرویس ویورا هستیم. لطفاً دقایقی دیگر مجدداً تلاش کنید.",
                    KEY_SUPPORT_URL to "https://t.me/vioratask_support",
                    KEY_TERMS_URL to "https://viora.app/legal/terms",
                    KEY_PRIVACY_URL to "https://viora.app/legal/privacy"
                )
                setDefaultsAsync(defaults)
            }

            fetchAndActivate()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error initializing FirebaseRemoteConfig: ${e.message}")
            VioraCrashlytics.recordException(e, "RemoteConfigInit")
        }
    }

    fun fetchAndActivate(onComplete: ((Boolean) -> Unit)? = null) {
        val rc = remoteConfig ?: run {
            onComplete?.invoke(false)
            return
        }

        rc.fetchAndActivate()
            .addOnCompleteListener { task ->
                val isSuccessful = task.isSuccessful
                if (isSuccessful) {
                    android.util.Log.d(TAG, "RemoteConfig fetched and activated successfully")
                    applyRemoteConfigValues(rc)
                } else {
                    android.util.Log.w(TAG, "RemoteConfig fetch failed: ${task.exception?.message}")
                    applyRemoteConfigValues(rc) // Fallback to cached/defaults
                }
                onComplete?.invoke(isSuccessful)
            }
    }

    private fun applyRemoteConfigValues(rc: FirebaseRemoteConfig) {
        try {
            val minVersion = rc.getLong(KEY_MIN_VERSION_CODE)
            val latestVersion = rc.getLong(KEY_LATEST_VERSION_CODE)
            val updateUrl = rc.getString(KEY_UPDATE_URL).ifBlank { "https://play.google.com/store/apps/details?id=com.viora.task" }
            val releaseNotes = rc.getString(KEY_RELEASE_NOTES)
            val isMaintenance = rc.getBoolean(KEY_IS_MAINTENANCE_MODE)
            val maintenanceMessage = rc.getString(KEY_MAINTENANCE_MESSAGE).ifBlank {
                "ما در حال به‌روزرسانی زیرساخت هستیم. لطفاً کمی بعد دوباره تلاش کنید."
            }
            val supportUrl = rc.getString(KEY_SUPPORT_URL).ifBlank { "https://t.me/vioratask_support" }
            val termsUrl = rc.getString(KEY_TERMS_URL).ifBlank { "https://viora.app/legal/terms" }
            val privacyUrl = rc.getString(KEY_PRIVACY_URL).ifBlank { "https://viora.app/legal/privacy" }

            val newConfig = RemoteAppConfig(
                minVersionCode = minVersion,
                latestVersionCode = latestVersion,
                updateUrl = updateUrl,
                releaseNotes = releaseNotes,
                isMaintenanceMode = isMaintenance,
                maintenanceMessage = maintenanceMessage,
                supportUrl = supportUrl,
                termsUrl = termsUrl,
                privacyUrl = privacyUrl
            )

            _configState.value = newConfig

            // Evaluate current app status
            val currentAppVersion = BuildConfig.VERSION_CODE.toLong()

            when {
                isMaintenance -> {
                    _appStatus.value = AppStatus.Maintenance(maintenanceMessage)
                    VioraAnalytics.logMaintenanceEncountered()
                }
                currentAppVersion < minVersion -> {
                    _appStatus.value = AppStatus.ForceUpdateRequired(
                        minVersionCode = minVersion,
                        latestVersionCode = latestVersion,
                        updateUrl = updateUrl,
                        releaseNotes = releaseNotes
                    )
                    VioraAnalytics.logForceUpdateEncountered(currentAppVersion, minVersion)
                }
                currentAppVersion < latestVersion -> {
                    _appStatus.value = AppStatus.OptionalUpdateAvailable(
                        latestVersionCode = latestVersion,
                        updateUrl = updateUrl,
                        releaseNotes = releaseNotes
                    )
                    VioraAnalytics.logSoftUpdatePrompted(currentAppVersion, latestVersion)
                }
                else -> {
                    _appStatus.value = AppStatus.Normal
                }
            }

            android.util.Log.d(TAG, "Config evaluated: maintenance=$isMaintenance, minVer=$minVersion, latestVer=$latestVersion, currentVer=$currentAppVersion")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error applying RemoteConfig: ${e.message}")
            VioraCrashlytics.recordException(e, "ApplyRemoteConfig")
        }
    }

    fun dismissOptionalUpdate() {
        if (_appStatus.value is AppStatus.OptionalUpdateAvailable) {
            _appStatus.value = AppStatus.Normal
        }
    }
}
