package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.UserProfile
import com.example.model.UserProfileStats
import com.example.model.viora.UpdateUserDto
import com.example.network.viora.VioraNetworkModule
import com.example.util.ImageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadProfile()
    }

    fun refreshProfile(taskViewModel: VioraTaskViewModel? = null) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            taskViewModel?.refreshDashboard()
            loadProfile()
            kotlinx.coroutines.delay(800)
            _isRefreshing.value = false
        }
    }

    fun loadProfile() {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val isRegistered = authPrefs.getBoolean("is_registered", false)
        if (isRegistered) {
            val name = authPrefs.getString("user_name", "User") ?: "User"
            val username = authPrefs.getString("user_username", "user") ?: "user"
            val rawAvatarUri = authPrefs.getString("user_avatar_uri", null)
            val avatarUri = ImageUtil.resolveAvatarUrl(rawAvatarUri)
            val defaultDeadlineStr = authPrefs.getString("user_default_deadline", "Weekly") ?: "Weekly"
            val deadlineDays = when (defaultDeadlineStr) {
                "Daily" -> 1
                "Weekly" -> 7
                "Monthly" -> 30
                else -> authPrefs.getInt("user_custom_days", 7)
            }
            
            _userProfile.value = UserProfile(
                id = authPrefs.getString("user_id", "user1") ?: "user1",
                name = name,
                username = if (username.startsWith("@")) username else "@$username",
                profileImageRes = null,
                profileImageUri = avatarUri,
                joinDate = "July 2026",
                stats = UserProfileStats(
                    assignedTasks = 8,
                    completedTasks = 3,
                    overdueTasks = 0,
                    activeTeams = 2,
                    defaultDeadlineDays = deadlineDays
                )
            )

            // Asynchronously refresh stats from backend report
            viewModelScope.launch {
                try {
                    val reportRes = VioraNetworkModule.api.getCurrentUserReport()
                    if (reportRes.success && reportRes.data != null) {
                        val report = reportRes.data
                        val current = _userProfile.value
                        if (current != null) {
                            _userProfile.value = current.copy(
                                name = report.fullName,
                                username = if (report.username.startsWith("@")) report.username else "@${report.username}",
                                stats = UserProfileStats(
                                    assignedTasks = report.taskSummary?.totalPersonalTasks ?: current.stats.assignedTasks,
                                    completedTasks = report.taskSummary?.statusBreakdown?.done ?: current.stats.completedTasks,
                                    overdueTasks = 0,
                                    activeTeams = report.teamSummary?.totalTeams ?: current.stats.activeTeams,
                                    defaultDeadlineDays = report.remainingDays.coerceAtLeast(1)
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore offline/network errors
                }
            }
        } else {
            val rawAvatarUri = authPrefs.getString("user_avatar_uri", null)
            val avatarUri = ImageUtil.resolveAvatarUrl(rawAvatarUri)
            _userProfile.value = UserProfile(
                id = "1",
                name = "Muhammad Mahdi Jafari",
                username = "@username",
                profileImageRes = null,
                profileImageUri = avatarUri,
                joinDate = "June 2026",
                stats = UserProfileStats(
                    assignedTasks = 42,
                    completedTasks = 18,
                    overdueTasks = 3,
                    activeTeams = 5,
                    defaultDeadlineDays = 14
                )
            )
        }
    }

    fun updateProfileAvatar(uriString: String?) {
        if (uriString.isNullOrEmpty()) return
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val isRegistered = authPrefs.getBoolean("is_registered", false)
        val editor = authPrefs.edit()

        if (!isRegistered) {
            editor.putBoolean("is_registered", true)
            editor.putString("user_name", "Muhammad Mahdi Jafari")
            editor.putString("user_username", "username")
            editor.putString("user_email", "user@example.com")
            editor.putString("user_default_deadline", "Weekly")
            editor.putInt("user_custom_days", 14)
        }
        
        // Optimistically show the local selected image
        editor.putString("user_avatar_uri", uriString).apply()
        loadProfile()

        // Upload to backend and sync
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            _uploadError.value = null
            try {
                val uploadResult = ImageUtil.uploadImageUri(getApplication(), Uri.parse(uriString))
                uploadResult.onSuccess { remoteUrl ->
                    val fullUrl = ImageUtil.resolveAvatarUrl(remoteUrl) ?: remoteUrl
                    editor.putString("user_avatar_uri", fullUrl).apply()
                    com.example.util.UserAvatarRegistry.setCurrentUser(null, null, fullUrl)
                    
                    val req = UpdateUserDto(avatar = fullUrl)
                    VioraNetworkModule.api.updateCurrentUser(req)
                    loadProfile()
                }.onFailure { error ->
                    _uploadError.value = error.message ?: "Failed to upload avatar"
                    error.printStackTrace()
                }
            } catch (e: Exception) {
                _uploadError.value = e.message ?: "Network error during avatar upload"
                e.printStackTrace()
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }
}
