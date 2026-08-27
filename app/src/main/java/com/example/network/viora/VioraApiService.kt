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
}
