package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.UserProfile
import com.example.model.UserProfileStats
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadMockProfile()
    }

    private fun loadMockProfile() {
        _userProfile.value = UserProfile(
            id = "1",
            name = "Muhammad Mahdi Jafari",
            username = "@mehranamarbini",
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
