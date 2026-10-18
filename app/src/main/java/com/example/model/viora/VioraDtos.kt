package com.example.model.viora

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenericResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T?,
    val timestamp: String = ""
)

@JsonClass(generateAdapter = true)
data class MessageResponseDto(val message: String)

@JsonClass(generateAdapter = true)
data class RequestOtpDto(val email: String)

@JsonClass(generateAdapter = true)
data class OtpResponseDto(val email: String, val expiresIn: Int)

@JsonClass(generateAdapter = true)
data class VerifyOtpDto(
    val email: String,
    val otp: String,
    val fullName: String,
    val username: String,
    val deadline: Int,
    val password: String,
    val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginDto(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class RefreshTokenDto(val refreshToken: String)

@JsonClass(generateAdapter = true)
data class TokensResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int
)

@JsonClass(generateAdapter = true)
data class UserResponseDto(
    val id: String,
    val email: String,
    val fullName: String,
    val username: String,
    val deadline: Int,
    val avatar: String? = null,
    val isEmailVerified: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    val user: UserResponseDto,
    val tokens: TokensResponseDto
)

@JsonClass(generateAdapter = true)
data class UserReportListSummaryDto(
    val totalTeamLists: Int,
    val totalPersonalLists: Int
)

@JsonClass(generateAdapter = true)
data class UserReportTaskSummaryDto(
    val totalTeamTasks: Int,
    val totalPersonalTasks: Int,
    val statusBreakdown: StatusBreakdownDto
)

@JsonClass(generateAdapter = true)
data class StatusBreakdownDto(
    val todo: Int,
    @com.squareup.moshi.Json(name = "in_progress") val inProgress: Int,
    @com.squareup.moshi.Json(name = "in_review") val inReview: Int,
    val done: Int
)

@JsonClass(generateAdapter = true)
data class UserReportResponseDto(
    val userId: String,
    val fullName: String,
    val username: String,
    val email: String,
    val userDeadline: String,
    val remainingDays: Int,
    val teamSummary: TeamSummaryDto?,
    val listSummary: UserReportListSummaryDto?,
    val taskSummary: UserReportTaskSummaryDto?
)

@JsonClass(generateAdapter = true)
data class UpdateUserDeadlineDto(val days: Int)

@JsonClass(generateAdapter = true)
data class UpdateUserDto(
    val fullName: String? = null,
    val avatar: String? = null,
    val days: Int? = null
)

// Teams
@JsonClass(generateAdapter = true)
data class CreateTeamDto(
    val name: String,
    val description: String? = null,
    val deadlineDays: Int? = null
)

@JsonClass(generateAdapter = true)
data class UpdateTeamDto(
    val name: String? = null,
    val description: String? = null,
    val deadlineDays: Int? = null,
    val addUsernames: List<String>? = null,
    val removeUsernames: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class TeamResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val deadlineAt: String? = null,
    val remainingDays: Int? = null,
    val ownerId: String? = null,
    val owner: TeamMemberDto? = null,
    val members: List<TeamMemberDto>? = null,
    val isArchived: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TeamMemberDto(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class UserSearchResultDto(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class UserSearchResponseDto(
    val items: List<UserSearchResultDto> = emptyList(),
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class TeamInviteResponseDto(
    val id: String,
    val teamId: String,
    val teamName: String,
    val invitedBy: TeamMemberDto? = null,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TeamSummaryDto(val totalTeams: Int)

@JsonClass(generateAdapter = true)
data class PaginatedTeamsResponseDto(
    val items: List<TeamResponseDto>,
    val total: Int,
    val page: Int,
    val perPage: Int,
    val totalPages: Int
)

// Lists
@JsonClass(generateAdapter = true)
data class CreateListDto(
    val name: String,
    val description: String? = null,
    val deadlineDays: Int? = null,
    val teamId: String,
    val userNamesToAdd: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class UpdateListDto(
    val name: String? = null,
    val description: String? = null,
    val deadlineDays: Int? = null,
    val userNamesToAdd: List<String>? = null,
    val userNamesToRemove: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePersonalListDto(
    val name: String? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ListSummaryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val deadlineInfo: DeadlineInfoDto? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val membersCount: Int,
    val tasksCount: Int,
    val isArchived: Boolean,
    val isPersonal: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class DeadlineInfoDto(
    val actualDeadline: String,
    val remainingDays: Int,
    val source: String
)

@JsonClass(generateAdapter = true)
data class ListResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val deadline: String? = null,
    val deadlineInfo: DeadlineInfoDto? = null,
    val teamId: String? = null,
    val team: ListTeamDto? = null,
    val users: List<ListMemberDto>? = null,
    val tasksCount: Int = 0,
    val isArchived: Boolean = false,
    val isPersonal: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ListTeamDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val deadlineAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ListMemberDto(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class ListOperationMetaDto(
    val ignoredUsernames: List<String>? = null,
    val addedToTeam: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ListWithMetaResponseDto(
    val list: ListResponseDto,
    val meta: ListOperationMetaDto? = null
)

@JsonClass(generateAdapter = true)
data class PersonalListResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val isPersonal: Boolean,
    val owner: PersonalListOwnerDto,
    val taskCount: Int,
    val effectiveDeadline: String,
    val remainingDays: Int,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class PersonalListOwnerDto(
    val id: String,
    val username: String,
    val fullName: String
)

// Tasks
@JsonClass(generateAdapter = true)
data class CreateTaskDto(
    val name: String,
    val description: String? = null,
    val deadlineDays: Int? = null,
    val listId: String? = null,
    val status: String? = null,
    val tags: List<String>? = null,
    val usernames: List<String>? = null,
    val subtasks: List<CreateSubtaskDto>? = null
)

@JsonClass(generateAdapter = true)
data class UpdateTaskDto(
    val name: String? = null,
    val description: String? = null,
    val deadlineDays: Int? = null,
    val status: String? = null,
    val listId: String? = null,
    val tagsToAdd: List<String>? = null,
    val tagsToRemove: List<String>? = null,
    val usernamesToAdd: List<String>? = null,
    val usernamesToRemove: List<String>? = null,
    val subtasksToAdd: List<CreateSubtaskDto>? = null,
    val subtaskIdsToRemove: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class TaskSummaryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val deadlineInfo: TaskDeadlineInfoDto? = null,
    val status: String,
    val listId: String? = null,
    val listName: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val tagNames: List<String>? = null,
    val assigneesCount: Int,
    val subtasksCount: Int,
    val isArchived: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class TaskDeadlineInfoDto(
    val actualDeadline: String,
    val remainingDays: Int,
    val source: String
)

@JsonClass(generateAdapter = true)
data class TaskResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val deadline: String? = null,
    val deadlineInfo: TaskDeadlineInfoDto? = null,
    val status: String = "TODO",
    val listId: String? = null,
    val list: TaskListDto? = null,
    val team: TaskTeamDto? = null,
    val users: List<TaskUserDto>? = null,
    val tags: List<TaskTagDto>? = null,
    val subtasks: List<SubtaskDto>? = null,
    val isArchived: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TaskListDto(
    val id: String,
    val name: String,
    val deadline: String? = null,
    val teamId: String? = null
)

@JsonClass(generateAdapter = true)
data class TaskTeamDto(
    val id: String,
    val name: String,
    val deadlineAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TaskUserDto(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class TaskTagDto(
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class SubtaskDto(
    val id: String,
    val name: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class TaskOperationMetaDto(
    val ignoredUsernames: List<String>? = null,
    val createdTags: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class TaskWithMetaResponseDto(
    val task: TaskResponseDto,
    val meta: TaskOperationMetaDto? = null
)

@JsonClass(generateAdapter = true)
data class CreateSubtaskDto(
    val name: String,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class SubtaskResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    val taskId: String,
    val creator: SubtaskUserDto,
    val assignee: SubtaskUserDto? = null,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class SubtaskUserDto(
    val id: String,
    val username: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class UpdateSubtaskStatusDto(
    val status: String
)

// Sync
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncPullResponseDto(
    val serverTimestamp: String? = null,
    val updatedData: SyncUpdatedDataDto? = null,
    val deletedIds: SyncDeletedIdsDto? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncUpdatedDataDto(
    val tasks: List<TaskResponseDto> = emptyList(),
    val lists: List<ListResponseDto> = emptyList(),
    val teams: List<TeamResponseDto> = emptyList()
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncDeletedIdsDto(
    val tasks: List<String> = emptyList(),
    val lists: List<String> = emptyList(),
    val teams: List<String> = emptyList()
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncPushDto(
    val mutations: SyncMutationsDto
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncMutationsDto(
    val tasks: List<SyncTaskMutationDto>? = null,
    val lists: List<SyncListMutationDto>? = null,
    val teams: List<SyncTeamMutationDto>? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncTaskMutationDto(
    val id: String,
    val updatedAt: String,
    val isDeleted: Boolean,
    val name: String,
    val description: String?,
    val deadline: String? = null,
    val status: String,
    val listId: String?,
    val isArchived: Boolean
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncListMutationDto(
    val id: String,
    val updatedAt: String,
    val isDeleted: Boolean,
    val name: String,
    val description: String?,
    val deadline: String? = null,
    val teamId: String?,
    val isPersonal: Boolean,
    val isArchived: Boolean
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncTeamMutationDto(
    val id: String,
    val updatedAt: String,
    val isDeleted: Boolean,
    val name: String,
    val description: String?,
    val deadlineAt: String? = null,
    val isArchived: Boolean
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncPushResponseDto(
    val processedIds: List<String>,
    val conflicts: List<SyncConflictDto>
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SyncConflictDto(
    val id: String,
    val entity: String,
    val serverData: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class FileUploadDataDto(
    val key: String,
    val url: String,
    val originalName: String? = null,
    val mimeType: String? = null,
    val size: Long? = null
)

@JsonClass(generateAdapter = true)
data class FileDeleteDataDto(
    val key: String
)

// --- Notification DTOs ---

@JsonClass(generateAdapter = true)
data class RegisterDeviceTokenDto(
    val token: String,
    val platform: String = "android"
)

@JsonClass(generateAdapter = true)
data class UserNotificationResponseDto(
    val id: String,
    val type: String,
    val label: String? = null,
    val title: String,
    val body: String,
    val actionUrl: String? = null,
    val data: Map<String, String>? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class PaginatedUserNotificationsResponseDto(
    val items: List<UserNotificationResponseDto>,
    val total: Int,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val unreadCount: Int
)

@JsonClass(generateAdapter = true)
data class UnreadNotificationsCountResponseDto(
    val unreadCount: Int
)

@JsonClass(generateAdapter = true)
data class MarkAllNotificationsReadResponseDto(
    val markedCount: Int
)
