package com.example.model
import androidx.annotation.DrawableRes
data class User(
    val id: String,
    val name: String,
    val username: String,
    @DrawableRes val avatarRes: Int? = null,
    val defaultDeadlineDays: Int,
    val avatarUrl: String? = null
)
enum class TeamRole(
    val title: String,
    val description: String,
    val level: Int
) {
    OWNER(
        title = "Owner",
        description = "Full control over team settings, members, and ownership transfer.",
        level = 3
    ),
    ADMIN(
        title = "Admin",
        description = "Can invite/remove members, assign roles, and edit team settings.",
        level = 2
    ),
    MEMBER(
        title = "Member",
        description = "Can view team lists and collaborate on tasks.",
        level = 1
    );

    val canManageMembers: Boolean get() = this == OWNER || this == ADMIN
    val canManageSettings: Boolean get() = this == OWNER || this == ADMIN
    val canDeleteTeam: Boolean get() = this == OWNER
    val canTransferOwnership: Boolean get() = this == OWNER
}

data class Team(
    val id: String,
    val name: String,
    val ownerId: String,
    val defaultDeadlineDays: Int? = null,
    val isArchived: Boolean = false,
    val members: List<User> = emptyList(),
    val pendingInvitations: List<String> = emptyList(),
    val memberRoles: Map<String, TeamRole> = emptyMap()
) {
    fun getMemberRole(username: String): TeamRole {
        if (members.find { it.id == ownerId }?.username == username || ownerId == username) {
            return TeamRole.OWNER
        }
        return memberRoles[username] ?: TeamRole.MEMBER
    }

    fun canUserManageMembers(username: String): Boolean = getMemberRole(username).canManageMembers
    fun canUserManageSettings(username: String): Boolean = getMemberRole(username).canManageSettings
}

data class TaskList(
    val id: String,
    val name: String,
    val teamId: String,
    val defaultDeadlineDays: Int? = null,
    val isArchived: Boolean = false,
    val isPersonal: Boolean = false,
    val description: String? = null,
    val members: List<User> = emptyList()
)

