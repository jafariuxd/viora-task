package com.example.model

import androidx.annotation.DrawableRes
import com.example.model.viora.UserNotificationResponseDto

enum class NotificationType {
    TASK_ASSIGNED,
    TASK_DONE,
    TEAM_INVITE,
    DEADLINE_FOCUS_TODAY,
    DEADLINE_OVERDUE,
    REMINDER,
    DEADLINE,
    CONNECTION,
    SOFT_UPDATE,
    MAINTENANCE_MODE,
    OTHER
}

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val timeGroup: String, // "Today", "Yesterday", "Earlier"
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val taskId: String? = null,
    val teamId: String? = null,
    val createdAt: String? = null,
    @DrawableRes val avatarRes: Int? = null,
    val data: Map<String, String>? = null
) {
    companion object {
        fun fromDto(dto: UserNotificationResponseDto, timeGroup: String): Notification {
            val mappedType = when {
                dto.type == "team_invite" || dto.type == "team_invitation" -> NotificationType.TEAM_INVITE
                dto.type == "task_assignment" -> NotificationType.TASK_ASSIGNED
                dto.type == "task_done" -> NotificationType.TASK_DONE
                dto.type == "deadline_focus_today" -> NotificationType.DEADLINE_FOCUS_TODAY
                dto.type == "deadline_overdue" -> NotificationType.DEADLINE_OVERDUE
                dto.type == "soft_update" -> NotificationType.SOFT_UPDATE
                dto.type == "maintenance_mode" -> NotificationType.MAINTENANCE_MODE
                dto.type.contains("team") || dto.type.contains("invite") -> NotificationType.TEAM_INVITE
                else -> NotificationType.OTHER
            }

            val extractedTaskId = dto.data?.get("taskId") ?: run {
                if (dto.actionUrl?.startsWith("viora://tasks/") == true) {
                    dto.actionUrl.substringAfter("viora://tasks/").substringBefore("?")
                } else null
            }

            val extractedTeamId = dto.data?.get("teamId") ?: run {
                if (dto.actionUrl?.startsWith("viora://teams/") == true) {
                    dto.actionUrl.substringAfter("viora://teams/").substringBefore("?")
                } else null
            }

            return Notification(
                id = dto.id,
                type = mappedType,
                title = dto.title,
                description = dto.body,
                timeGroup = timeGroup,
                isRead = dto.isRead,
                actionUrl = dto.actionUrl,
                taskId = extractedTaskId,
                teamId = extractedTeamId,
                createdAt = dto.createdAt,
                data = dto.data
            )
        }
    }
}
