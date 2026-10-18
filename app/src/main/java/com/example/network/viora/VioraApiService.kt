package com.example.network.viora
import com.example.model.viora.*
import retrofit2.http.*
interface VioraApiService {
    // Auth
    @POST("auth/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpDto): GenericResponse<OtpResponseDto>
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpDto): GenericResponse<AuthResponseDto>
    @POST("auth/login")
    suspend fun login(@Body request: LoginDto): GenericResponse<AuthResponseDto>
    @POST("auth/refresh")
    suspend fun refreshTokens(@Body request: RefreshTokenDto): GenericResponse<TokensResponseDto>
    @POST("auth/logout")
    suspend fun logout(): GenericResponse<MessageResponseDto>
    // Users
    @GET("users/me/report")
    suspend fun getCurrentUserReport(): GenericResponse<UserReportResponseDto>
    @PATCH("users/me/deadline")
    suspend fun updateCurrentUserDeadline(@Body request: UpdateUserDeadlineDto): GenericResponse<UserReportResponseDto>
    @PATCH("users/me")
    suspend fun updateCurrentUser(@Body request: UpdateUserDto): GenericResponse<UserReportResponseDto>
    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): GenericResponse<UserResponseDto>
    // Teams
    @POST("teams")
    suspend fun createTeam(@Body request: CreateTeamDto): GenericResponse<TeamResponseDto>
    @GET("teams")
    suspend fun getTeams(
        @Query("ownerId") ownerId: String? = null,
        @Query("memberId") memberId: String? = null,
        @Query("name") name: String? = null,
        @Query("isArchived") isArchived: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 100
    ): GenericResponse<PaginatedTeamsResponseDto>
    @GET("teams/{id}")
    suspend fun getTeam(@Path("id") id: String): GenericResponse<TeamResponseDto>
    @PATCH("teams/{id}")
    suspend fun updateTeam(@Path("id") id: String, @Body request: UpdateTeamDto): GenericResponse<TeamResponseDto>
    @DELETE("teams/{id}")
    suspend fun deleteTeam(@Path("id") id: String): GenericResponse<Any>

    // Team Invitations
    @GET("teams/invitations")
    suspend fun getTeamInvitations(): GenericResponse<List<TeamInviteResponseDto>>
    @POST("teams/invitations/{inviteId}/accept")
    suspend fun acceptTeamInvitation(@Path("inviteId") inviteId: String): GenericResponse<MessageResponseDto>
    @POST("teams/invitations/{inviteId}/decline")
    suspend fun declineTeamInvitation(@Path("inviteId") inviteId: String): GenericResponse<MessageResponseDto>

    // Users Search
    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): GenericResponse<UserSearchResponseDto>

    // Lists
    @POST("lists")
    suspend fun createList(@Body request: CreateListDto): GenericResponse<ListWithMetaResponseDto>
    @GET("lists")
    suspend fun getLists(
        @Query("teamId") teamId: String? = null,
        @Query("myListsOnly") myListsOnly: Boolean? = null,
        @Query("archived") archived: Boolean? = null
    ): GenericResponse<List<ListSummaryDto>>
    @GET("lists/personal")
    suspend fun getPersonalList(): GenericResponse<PersonalListResponseDto>
        @PATCH("lists/personal")
    suspend fun updatePersonalList(@Body request: UpdatePersonalListDto): GenericResponse<PersonalListResponseDto>
    @GET("lists/{id}")
    suspend fun getList(@Path("id") id: String): GenericResponse<ListResponseDto>
    @PATCH("lists/{id}")
    suspend fun updateList(@Path("id") id: String, @Body request: UpdateListDto): GenericResponse<ListWithMetaResponseDto>
    @DELETE("lists/{id}")
    suspend fun deleteList(@Path("id") id: String): GenericResponse<Any>
    // Tasks
    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskDto): GenericResponse<TaskWithMetaResponseDto>
    @GET("tasks")
    suspend fun getTasks(
        @Query("listId") listId: String? = null,
        @Query("teamId") teamId: String? = null,
        @Query("myTasksOnly") myTasksOnly: Boolean? = null,
        @Query("status") status: String? = null,
        @Query("tag") tag: String? = null,
        @Query("archived") archived: Boolean? = null
    ): GenericResponse<List<TaskSummaryDto>>
    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): GenericResponse<TaskResponseDto>
    @PATCH("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body request: UpdateTaskDto): GenericResponse<TaskWithMetaResponseDto>
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): GenericResponse<Any>
    // Subtasks
    @PATCH("subtasks/{id}/status")
    suspend fun updateSubtaskStatus(@Path("id") id: String, @Body request: UpdateSubtaskStatusDto): GenericResponse<SubtaskResponseDto>
    // Archive
    @PATCH("teams/{id}/archive")
    suspend fun archiveTeam(@Path("id") id: String): GenericResponse<TeamResponseDto>
    @PATCH("teams/{id}/unarchive")
    suspend fun unarchiveTeam(@Path("id") id: String): GenericResponse<TeamResponseDto>
    @POST("lists/{id}/archive")
    suspend fun archiveList(@Path("id") id: String): GenericResponse<MessageResponseDto>
    @POST("tasks/{id}/archive")
    suspend fun archiveTask(@Path("id") id: String): GenericResponse<MessageResponseDto>

    @GET("v1/sync")
    suspend fun syncPull(@Query("lastSyncAt") lastSyncAt: String? = null): GenericResponse<SyncPullResponseDto>
    @POST("v1/sync")
    suspend fun syncPush(@Body request: SyncPushDto): GenericResponse<SyncPushResponseDto>

    // Uploads
    @Multipart
    @POST("uploads")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part
    ): GenericResponse<FileUploadDataDto>

    @DELETE("uploads")
    suspend fun deleteFile(
        @Query("key") key: String
    ): GenericResponse<FileDeleteDataDto>

    // Notifications & Inbox
    @GET("notifications/inbox")
    suspend fun getNotificationsInbox(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 20,
        @Query("isRead") isRead: Boolean? = null,
        @Query("type") type: String? = null
    ): GenericResponse<PaginatedUserNotificationsResponseDto>

    @GET("notifications/inbox/unread-count")
    suspend fun getUnreadNotificationsCount(): GenericResponse<UnreadNotificationsCountResponseDto>

    @GET("notifications/inbox/{id}")
    suspend fun getNotification(
        @Path("id") id: String
    ): GenericResponse<UserNotificationResponseDto>

    @PATCH("notifications/inbox/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") id: String
    ): GenericResponse<UserNotificationResponseDto>

    @PATCH("notifications/inbox/read-all")
    suspend fun markAllNotificationsAsRead(): GenericResponse<MarkAllNotificationsReadResponseDto>

    @POST("notifications/devices")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenDto
    ): GenericResponse<Any>

    @DELETE("notifications/devices/{token}")
    suspend fun deleteDeviceToken(
        @Path("token") token: String
    ): GenericResponse<Any>
}
