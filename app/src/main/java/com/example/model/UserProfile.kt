package com.example.model

data class UserProfileStats(
    val assignedTasks: Int,
    val completedTasks: Int,
    val overdueTasks: Int,
    val activeTeams: Int,
    val defaultDeadlineDays: Int
)

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val profileImageRes: Int? = null,
    val joinDate: String,
    val stats: UserProfileStats,
    val profileImageUri: String? = null
)
