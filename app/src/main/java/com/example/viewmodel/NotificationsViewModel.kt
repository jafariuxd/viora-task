package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.Notification
import com.example.model.NotificationType
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        loadMockNotifications()
    }

    private fun loadMockNotifications() {
        _notifications.value = listOf(
            Notification(
                id = "1",
                type = NotificationType.TASK_ASSIGNED,
                title = "New task assigned: \"Client Presentation\"",
                description = "Tala assigned you a new task due tomorrow.",
                timeGroup = "Today",
                avatarRes = R.drawable.img_avatar_sara_1783672418392 // Use Sara's avatar as placeholder
            ),
            Notification(
                id = "2",
                type = NotificationType.REMINDER,
                title = "Reminder \u2013 Team Sync at 3 PM",
                description = "Don't miss today's weekly sync meeting",
                timeGroup = "Today"
            ),
            Notification(
                id = "3",
                type = NotificationType.DEADLINE,
                title = "Deadline approaching: \"Budget Report\"",
                description = "Due in 2 hours \u2013 mark it as done or reassign",
                timeGroup = "Today"
            ),
            Notification(
                id = "4",
                type = NotificationType.CONNECTION,
                title = "Connection request from Sara",
                description = "Sara wants to connect \u2013 view profile or ignore",
                timeGroup = "Today",
                avatarRes = R.drawable.img_avatar_sara_1783672418392
            ),
            Notification(
                id = "5",
                type = NotificationType.REMINDER,
                title = "Reminder \u2013 Team Sync at 3 PM",
                description = "Don't miss today's weekly sync meeting",
                timeGroup = "Yesterday"
            ),
            Notification(
                id = "6",
                type = NotificationType.TASK_ASSIGNED,
                title = "New task assigned: \"Client\"",
                description = "Tala assigned you a new task due tomorrow.",
                timeGroup = "Yesterday",
                avatarRes = R.drawable.img_avatar_sara_1783672418392
            )
        )
    }
}
