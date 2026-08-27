package com.example.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class CalendarEventListResponse(
    val items: List<CalendarEventItem>? = null,
    val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class CalendarEventItem(
    val id: String?,
    val status: String?,
    val summary: String?,
    val start: EventDateTime?,
    val end: EventDateTime?,
    val location: String?,
    val htmlLink: String?
)

@JsonClass(generateAdapter = true)
data class EventDateTime(
    val dateTime: String?, // "2023-11-20T10:00:00-07:00"
    val date: String?,     // "2023-11-20"
    val timeZone: String?
)

interface GoogleCalendarApi {
    @GET("calendar/v3/calendars/primary/events")
    suspend fun getEvents(
        @Header("Authorization") authHeader: String,
        @Query("timeMin") timeMin: String,
        @Query("timeMax") timeMax: String,
        @Query("maxResults") maxResults: Int = 10,
        @Query("pageToken") pageToken: String? = null,
        @Query("singleEvents") singleEvents: Boolean = true,
        @Query("orderBy") orderBy: String = "startTime"
    ): CalendarEventListResponse
}
