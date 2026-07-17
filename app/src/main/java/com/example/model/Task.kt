package com.example.model

enum class TaskStatus(theName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    DONE("Done");

    override fun toString(): String = name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

data class Task(
    val id: String,
    val title: String,
    val client: String,
    val listId: String? = null,
    val teamId: String? = null,
    val userId: String? = null,
    val specificDeadlineDays: Int? = null,
    val computedDeadlineMillis: Long? = null,
    val daysLeft: Int = 0, // numeric for sorting, computed
    val status: TaskStatus = TaskStatus.TODO,
    val assigneePhotos: List<String> = emptyList(), // "sara", "mohammad", "other" to match generated avatars
    val isUnplanned: Boolean = false,
    // New fields for detail screen
    val description: String = "",
    val tags: List<String> = emptyList(),
    val dueDateText: String = "", // e.g. "2025 Apr 11"
    val folder: String = "My Tasks" // e.g. "My Tasks", "Charchoob"
)

data class CalendarEvent(
    val day: String,
    val month: String,
    val title: String,
    val time: String
)

data class WeatherInfo(
    val location: String,
    val temperature: String,
    val condition: String,
    val uvIndex: String,
    val dateText: String,
    val lastRefreshText: String
)
