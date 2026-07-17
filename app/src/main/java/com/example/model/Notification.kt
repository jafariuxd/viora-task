package com.example.model

import androidx.annotation.DrawableRes

enum class NotificationType {
    TASK_ASSIGNED,
    REMINDER,
    DEADLINE,
    CONNECTION
}

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val timeGroup: String, // "Today", "Yesterday"
    @DrawableRes val avatarRes: Int? = null
)
