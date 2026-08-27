import re

with open('app/src/main/java/com/example/model/viora/VioraDtos.kt', 'r') as f:
    content = f.read()

rep = """@JsonClass(generateAdapter = true)
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
)"""

content = re.sub(r'@JsonClass\(generateAdapter = true\)\ndata class UserReportResponseDto\(.*?\)', rep, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/model/viora/VioraDtos.kt', 'w') as f:
    f.write(content)
