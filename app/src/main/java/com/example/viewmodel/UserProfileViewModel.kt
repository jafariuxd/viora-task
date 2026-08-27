package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.UserProfile
import com.example.model.UserProfileStats
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val isRegistered = authPrefs.getBoolean("is_registered", false)
        if (isRegistered) {
            val name = authPrefs.getString("user_name", "User") ?: "User"
            val username = authPrefs.getString("user_username", "user") ?: "user"
            val rawAvatarUri = authPrefs.getString("user_avatar_uri", null)
            val avatarUri = if (com.example.util.ImageUtil.isLocalFileOrUriValid(rawAvatarUri)) rawAvatarUri else null
            val defaultDeadlineStr = authPrefs.getString("user_default_deadline", "Weekly") ?: "Weekly"
            val deadlineDays = when (defaultDeadlineStr) {
                "Daily" -> 1
                "Weekly" -> 7
                "Monthly" -> 30
                else -> authPrefs.getInt("user_custom_days", 7)
            }
            
            _userProfile.value = UserProfile(
                id = "user1",
                name = name,
                username = if (username.startsWith("@")) username else "@$username",
                profileImageRes = if (avatarUri == null) R.drawable.img_profile_mohammad_1783672402325 else null,
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
        } else {
            _userProfile.value = UserProfile(
                id = "1",
                name = "Muhammad Mahdi Jafari",
                username = "@username",
                profileImageRes = R.drawable.img_profile_mohammad_1783672402325,
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

    fun updateProfileAvatar(uri: String?) {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val isRegistered = authPrefs.getBoolean("is_registered", false)
        val editor = authPrefs.edit()
        
        val smallBase64 = com.example.util.ImageUtil.toSmallBase64(getApplication(), uri)
        val savedAvatar = smallBase64 ?: uri

        if (!isRegistered) {
            // First time editing photo, also store default profile data so it doesn't get wiped
            editor.putBoolean("is_registered", true)
            editor.putString("user_name", "Muhammad Mahdi Jafari")
            editor.putString("user_username", "username")
            editor.putString("user_email", "user@example.com")
            editor.putString("user_default_deadline", "Weekly")
            editor.putInt("user_custom_days", 14)
        }
        
        editor.putString("user_avatar_uri", savedAvatar).apply()
        loadProfile()

        // Sync with backend server
        viewModelScope.launch {
            try {
                if (savedAvatar != null) {
                    val req = com.example.model.viora.UpdateUserDto(avatar = savedAvatar)
                    com.example.network.viora.VioraNetworkModule.api.updateCurrentUser(req)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
