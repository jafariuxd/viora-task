package com.example.util

/**
 * Platform-agnostic sealed class representing deep links supported by Viora.
 * Ready for Compose Multiplatform (CMP) commonMain extraction.
 */
sealed class VioraDeepLink {
    data class TeamInvite(
        val teamId: String,
        val teamName: String? = null,
        val inviteId: String? = null,
        val inviterName: String? = null
    ) : VioraDeepLink()
    data class TaskDetail(
        val taskId: String,
        val title: String? = null,
        val client: String? = null,
        val daysLeft: Int? = null,
        val status: String? = null,
        val description: String? = null
    ) : VioraDeepLink()
    data class ListDetail(val listName: String, val teamName: String? = null) : VioraDeepLink()
    object TodayFocus : VioraDeepLink()
    object Unknown : VioraDeepLink()
}

/**
 * Universal Deep Link Router and Builder for Viora (Android & iOS / CMP ready).
 * Domain: viora.group
 */
object VioraDeepLinkHandler {
    const val DOMAIN = "viora.group"
    const val BASE_URL = "https://$DOMAIN"

    /**
     * Parse any incoming URL string (from Android Intent or iOS universal link)
     * and map it into a strongly typed VioraDeepLink.
     */
    fun parse(urlString: String?): VioraDeepLink {
        if (urlString.isNullOrBlank()) return VioraDeepLink.Unknown

        try {
            // Handle custom scheme: viora://tasks/{id}, viora://teams/{id}, viora://focus/today
            if (urlString.startsWith("viora://", ignoreCase = true)) {
                val uri = android.net.Uri.parse(urlString)
                val host = uri.host?.lowercase() ?: ""
                val pathSegments = uri.pathSegments ?: emptyList()
                return when (host) {
                    "tasks", "task" -> {
                        val taskId = pathSegments.firstOrNull() ?: uri.getQueryParameter("id") ?: ""
                        if (taskId.isNotBlank()) {
                            VioraDeepLink.TaskDetail(
                                taskId = taskId,
                                title = uri.getQueryParameter("title"),
                                client = uri.getQueryParameter("client"),
                                daysLeft = uri.getQueryParameter("daysLeft")?.toIntOrNull(),
                                status = uri.getQueryParameter("status"),
                                description = uri.getQueryParameter("desc") ?: uri.getQueryParameter("description")
                            )
                        } else VioraDeepLink.Unknown
                    }
                    "teams", "team", "invite" -> {
                        val teamId = uri.getQueryParameter("teamId")
                            ?: uri.getQueryParameter("id")
                            ?: pathSegments.firstOrNull()
                            ?: ""
                        val teamName = uri.getQueryParameter("teamName") ?: uri.getQueryParameter("name")
                        val inviteId = uri.getQueryParameter("inviteId")
                        val inviter = uri.getQueryParameter("invitedBy") ?: uri.getQueryParameter("inviter")
                        if (teamId.isNotBlank()) {
                            VioraDeepLink.TeamInvite(
                                teamId = teamId,
                                teamName = teamName,
                                inviteId = inviteId,
                                inviterName = inviter
                            )
                        } else VioraDeepLink.Unknown
                    }
                    "focus" -> {
                        VioraDeepLink.TodayFocus
                    }
                    else -> VioraDeepLink.Unknown
                }
            }

            // Strip protocol and find path/query
            val cleanUrl = if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                "https://$urlString"
            } else {
                urlString
            }

            val uri = android.net.Uri.parse(cleanUrl)
            val host = uri.host?.lowercase() ?: ""
            val isSupportedDomain = host == DOMAIN || host.endsWith(".$DOMAIN") || host == "www.$DOMAIN"

            if (!isSupportedDomain) {
                // Not a supported viora.group link
                return VioraDeepLink.Unknown
            }

            val pathSegments = uri.pathSegments ?: emptyList()
            if (pathSegments.isEmpty()) {
                // E.g. https://viora.group?teamId=... or https://viora.group?taskId=...
                val teamId = uri.getQueryParameter("teamId") ?: uri.getQueryParameter("team")
                if (!teamId.isNullOrBlank()) {
                    return VioraDeepLink.TeamInvite(
                        teamId = teamId,
                        teamName = uri.getQueryParameter("teamName"),
                        inviteId = uri.getQueryParameter("inviteId"),
                        inviterName = uri.getQueryParameter("invitedBy")
                    )
                }
                val taskId = uri.getQueryParameter("taskId") ?: uri.getQueryParameter("task")
                if (!taskId.isNullOrBlank()) {
                    return VioraDeepLink.TaskDetail(taskId = taskId)
                }
                return VioraDeepLink.Unknown
            }

            val firstSegment = pathSegments.firstOrNull()?.lowercase() ?: ""
            return when (firstSegment) {
                "invite", "invites", "team", "teams", "group", "groups", "join" -> {
                    // Supported formats:
                    // https://viora.group/invite?teamId=abc&teamName=Dev
                    // https://viora.group/invite/abc
                    // https://viora.group/join?code=abc
                    // https://viora.group/team/abc
                    val teamId = uri.getQueryParameter("teamId") 
                        ?: uri.getQueryParameter("team")
                        ?: uri.getQueryParameter("id")
                        ?: uri.getQueryParameter("code")
                        ?: uri.getQueryParameter("token")
                        ?: uri.getQueryParameter("inviteId")
                        ?: uri.getQueryParameter("invite")
                        ?: pathSegments.getOrNull(1)
                        ?: ""
                    val teamName = uri.getQueryParameter("teamName") ?: uri.getQueryParameter("name")
                    val inviteId = uri.getQueryParameter("inviteId") ?: uri.getQueryParameter("invite_id")
                    val inviter = uri.getQueryParameter("invitedBy") ?: uri.getQueryParameter("inviter")
                    if (teamId.isNotBlank()) {
                        VioraDeepLink.TeamInvite(
                            teamId = teamId,
                            teamName = teamName,
                            inviteId = inviteId,
                            inviterName = inviter
                        )
                    } else {
                        VioraDeepLink.Unknown
                    }
                }
                "task", "tasks" -> {
                    // Supported formats:
                    // https://viora.group/task/task_123
                    // https://viora.group/tasks/task_123
                    // https://viora.group/task?id=task_123
                    val taskId = uri.getQueryParameter("id")
                        ?: uri.getQueryParameter("taskId")
                        ?: uri.getQueryParameter("task")
                        ?: pathSegments.getOrNull(1)
                        ?: ""
                    val title = uri.getQueryParameter("title")
                    val client = uri.getQueryParameter("client")
                    val daysLeft = uri.getQueryParameter("daysLeft")?.toIntOrNull()
                    val status = uri.getQueryParameter("status")
                    val desc = uri.getQueryParameter("desc") ?: uri.getQueryParameter("description")
                    if (taskId.isNotBlank()) {
                        VioraDeepLink.TaskDetail(
                            taskId = taskId,
                            title = title,
                            client = client,
                            daysLeft = daysLeft,
                            status = status,
                            description = desc
                        )
                    } else {
                        VioraDeepLink.Unknown
                    }
                }
                "list", "lists" -> {
                    // Supported formats:
                    // https://viora.group/list/Work
                    val listName = uri.getQueryParameter("name")
                        ?: uri.getQueryParameter("list")
                        ?: pathSegments.getOrNull(1)
                        ?: ""
                    val teamName = uri.getQueryParameter("team") ?: uri.getQueryParameter("teamName")
                    if (listName.isNotBlank()) {
                        VioraDeepLink.ListDetail(listName = listName, teamName = teamName)
                    } else {
                        VioraDeepLink.Unknown
                    }
                }
                "focus", "today" -> {
                    VioraDeepLink.TodayFocus
                }
                else -> {
                    if (firstSegment.startsWith("team_") || firstSegment.startsWith("invite_")) {
                        VioraDeepLink.TeamInvite(
                            teamId = firstSegment,
                            teamName = uri.getQueryParameter("teamName"),
                            inviteId = uri.getQueryParameter("inviteId"),
                            inviterName = uri.getQueryParameter("invitedBy")
                        )
                    } else if (firstSegment.startsWith("task_")) {
                        VioraDeepLink.TaskDetail(taskId = firstSegment)
                    } else {
                        VioraDeepLink.Unknown
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return VioraDeepLink.Unknown
        }
    }

    /**
     * Generate shareable Universal Link for a team invite.
     */
    fun createTeamInviteUrl(teamId: String, teamName: String? = null): String {
        val encodedName = teamName?.let { "&teamName=" + android.net.Uri.encode(it) } ?: ""
        return "$BASE_URL/invite?teamId=${android.net.Uri.encode(teamId)}$encodedName"
    }

    /**
     * Generate shareable Universal Link for a specific task.
     */
    fun createTaskUrl(
        taskId: String,
        title: String? = null,
        client: String? = null,
        daysLeft: Int? = null,
        status: String? = null,
        description: String? = null
    ): String {
        val params = mutableListOf<String>()
        if (!title.isNullOrBlank()) {
            params.add("title=" + android.net.Uri.encode(title))
        }
        if (!client.isNullOrBlank()) {
            params.add("client=" + android.net.Uri.encode(client))
        }
        if (daysLeft != null) {
            params.add("daysLeft=$daysLeft")
        }
        if (!status.isNullOrBlank()) {
            params.add("status=" + android.net.Uri.encode(status))
        }
        if (!description.isNullOrBlank()) {
            params.add("desc=" + android.net.Uri.encode(description))
        }

        val query = if (params.isNotEmpty()) "?" + params.joinToString("&") else ""
        return "$BASE_URL/task/${android.net.Uri.encode(taskId)}$query"
    }

    /**
     * Generate shareable Universal Link for a specific list.
     */
    fun createListUrl(listName: String, teamName: String? = null): String {
        val encodedTeam = teamName?.let { "?team=" + android.net.Uri.encode(it) } ?: ""
        return "$BASE_URL/list/${android.net.Uri.encode(listName)}$encodedTeam"
    }
}
