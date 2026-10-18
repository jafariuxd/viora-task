package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.model.Notification
import com.example.model.NotificationType
import com.example.network.viora.VioraNetworkModule
import com.example.service.VioraFirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class NotificationFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    TASKS("Tasks"),
    DEADLINES("Deadlines"),
    TEAMS("Teams")
}

data class SystemNotificationAlert(
    val type: String,
    val title: String,
    val message: String,
    val minVersion: String? = null,
    val lastVersion: String? = null
)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("viora_notifications_prefs", Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentFilter = MutableStateFlow(NotificationFilter.ALL)
    val currentFilter: StateFlow<NotificationFilter> = _currentFilter.asStateFlow()

    private val _systemAlert = MutableStateFlow<SystemNotificationAlert?>(null)
    val systemAlert: StateFlow<SystemNotificationAlert?> = _systemAlert.asStateFlow()

    init {
        // Load initial state
        loadNotifications()
        loadUnreadCount()
        listenToFCM()
    }

    private fun getPersistedReadIds(): Set<String> {
        return prefs.getStringSet("read_notification_ids", emptySet()) ?: emptySet()
    }

    private fun persistReadId(id: String) {
        val current = prefs.getStringSet("read_notification_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id)
        prefs.edit().putStringSet("read_notification_ids", current).apply()
    }

    private fun persistAllReadIds(ids: List<String>) {
        val current = prefs.getStringSet("read_notification_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.addAll(ids)
        prefs.edit().putStringSet("read_notification_ids", current).apply()
    }

    private fun listenToFCM() {
        viewModelScope.launch {
            VioraFirebaseMessagingService.messageEvents.collect { remoteMessage ->
                val type = remoteMessage.data["type"]
                if (type == "maintenance_mode") {
                    _systemAlert.value = SystemNotificationAlert(
                        type = "maintenance_mode",
                        title = remoteMessage.notification?.title ?: "Server Maintenance",
                        message = remoteMessage.notification?.body ?: "Viora is undergoing scheduled maintenance."
                    )
                } else if (type == "soft_update") {
                    _systemAlert.value = SystemNotificationAlert(
                        type = "soft_update",
                        title = remoteMessage.notification?.title ?: "Update Available",
                        message = remoteMessage.notification?.body ?: "A new version of Viora is available.",
                        minVersion = remoteMessage.data["min-version"],
                        lastVersion = remoteMessage.data["last-version"]
                    )
                }
                // Refresh list & unread count upon push notification
                loadNotifications(isRefresh = true)
                loadUnreadCount()
            }
        }
    }

    fun dismissSystemAlert() {
        _systemAlert.value = null
    }

    fun setFilter(filter: NotificationFilter) {
        _currentFilter.value = filter
        loadNotifications()
    }

    fun refresh() {
        loadNotifications(isRefresh = true)
        loadUnreadCount()
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val token = VioraNetworkModule.getTokenManager()?.getAccessToken()
                if (token.isNullOrBlank()) return@launch

                val response = withContext(Dispatchers.IO) {
                    VioraNetworkModule.api.getUnreadNotificationsCount()
                }
                if (response.success && response.data != null) {
                    val readIds = getPersistedReadIds()
                    val locallyReadCount = _notifications.value.count { readIds.contains(it.id) && !it.isRead }
                    _unreadCount.value = (response.data.unreadCount - locallyReadCount).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                // Silently ignore if offline
            }
        }
    }

    fun loadNotifications(page: Int = 1, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else if (_notifications.value.isEmpty()) {
                _isLoading.value = true
            }

            try {
                val token = VioraNetworkModule.getTokenManager()?.getAccessToken()
                if (token.isNullOrBlank()) {
                    if (_notifications.value.isEmpty() || isRefresh) {
                        loadFallbackNotifications()
                    }
                    return@launch
                }

                val filter = _currentFilter.value
                val isReadParam = when (filter) {
                    NotificationFilter.UNREAD -> false
                    else -> null
                }
                val typeParam = when (filter) {
                    NotificationFilter.TASKS -> "task_assignment"
                    NotificationFilter.DEADLINES -> "deadline_overdue"
                    NotificationFilter.TEAMS -> "team_invite"
                    else -> null
                }

                val response = withContext(Dispatchers.IO) {
                    VioraNetworkModule.api.getNotificationsInbox(
                        page = page,
                        perPage = 50,
                        isRead = isReadParam,
                        type = typeParam
                    )
                }

                if (response.success && response.data != null) {
                    val readIds = getPersistedReadIds()
                    val rawItems = response.data.items

                    val domainList = rawItems.map { dto ->
                        val timeGroup = computeTimeGroup(dto.createdAt)
                        val isLocallyRead = readIds.contains(dto.id)
                        Notification.fromDto(dto, timeGroup).copy(isRead = dto.isRead || isLocallyRead)
                    }

                    _notifications.value = domainList
                    _unreadCount.value = domainList.count { !it.isRead }
                } else if (_notifications.value.isEmpty()) {
                    loadFallbackNotifications()
                }
            } catch (e: Exception) {
                if (_notifications.value.isEmpty()) {
                    loadFallbackNotifications()
                }
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun markAsRead(id: String, onComplete: (() -> Unit)? = null) {
        persistReadId(id)

        // Optimistic UI update
        val currentList = _notifications.value
        val target = currentList.find { it.id == id }
        if (target != null && !target.isRead) {
            _notifications.value = currentList.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            if (_unreadCount.value > 0) {
                _unreadCount.value = _unreadCount.value - 1
            }
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    VioraNetworkModule.api.markNotificationAsRead(id)
                }
            } catch (e: Exception) {
                // Silently ignore network failures for read status
            } finally {
                onComplete?.invoke()
            }
        }
    }

    fun markAllAsRead() {
        val allIds = _notifications.value.map { it.id }
        persistAllReadIds(allIds)

        // Optimistic UI update
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    VioraNetworkModule.api.markAllNotificationsAsRead()
                }
            } catch (e: Exception) {
                // Retry or ignore
            }
        }
    }

    private fun computeTimeGroup(createdAtIso: String?): String {
        if (createdAtIso.isNullOrBlank()) return "Today"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val cleanDateStr = if (createdAtIso.length >= 19) createdAtIso.substring(0, 19) else createdAtIso
            val date = sdf.parse(cleanDateStr) ?: return "Today"

            val itemCal = Calendar.getInstance().apply { time = date }
            val nowCal = Calendar.getInstance()

            val itemYear = itemCal.get(Calendar.YEAR)
            val nowYear = nowCal.get(Calendar.YEAR)
            val itemDay = itemCal.get(Calendar.DAY_OF_YEAR)
            val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)

            if (itemYear == nowYear && itemDay == nowDay) {
                "Today"
            } else if (itemYear == nowYear && nowDay - itemDay == 1) {
                "Yesterday"
            } else {
                "Earlier"
            }
        } catch (e: Exception) {
            "Today"
        }
    }

    private fun loadFallbackNotifications() {
        val readIds = getPersistedReadIds()
        val fallback = listOf(
            Notification(
                id = "demo-1",
                type = NotificationType.DEADLINE_OVERDUE,
                title = "Overdue tasks waiting",
                description = "You have tasks waiting for your attention.",
                timeGroup = "Today",
                isRead = readIds.contains("demo-1"),
                actionUrl = "viora://focus/today"
            ),
            Notification(
                id = "demo-2",
                type = NotificationType.TASK_ASSIGNED,
                title = "New task assigned: \"Client Presentation\"",
                description = "Tala assigned you a new task due tomorrow.",
                timeGroup = "Today",
                isRead = readIds.contains("demo-2"),
                avatarRes = R.drawable.img_avatar_sara_1783672418392
            ),
            Notification(
                id = "demo-3",
                type = NotificationType.DEADLINE_FOCUS_TODAY,
                title = "Daily Focus",
                description = "Focus on 3 key priorities scheduled for today.",
                timeGroup = "Today",
                isRead = true,
                actionUrl = "viora://focus/today"
            ),
            Notification(
                id = "demo-4",
                type = NotificationType.TASK_DONE,
                title = "Task completed: \"Design System Viora\"",
                description = "Sara marked this task as completed.",
                timeGroup = "Yesterday",
                isRead = true,
                avatarRes = R.drawable.img_avatar_sara_1783672418392
            )
        )
        _notifications.value = fallback
        _unreadCount.value = fallback.count { !it.isRead }
    }
}
