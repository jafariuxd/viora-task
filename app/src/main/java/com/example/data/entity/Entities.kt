package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val status: String = "todo", // "todo", "in-progress", "in-review", "done"
    val listId: String? = null,
    val teamId: String? = null,
    val deadline: Long? = null,
    val tags: String = "", // Comma-separated or JSON
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)

@Entity(tableName = "lists")
data class ListEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val deadline: Long? = null,
    val teamId: String? = null,
    val isPersonal: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val deadline: Long? = null,
    val ownerId: String? = null,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val name: String,
    val status: String = "todo",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
)
