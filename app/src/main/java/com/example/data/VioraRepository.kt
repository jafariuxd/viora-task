package com.example.data

import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VioraRepository(private val vioraDao: VioraDao) {

    // --- Tasks ---
    fun getTasks(): Flow<List<TaskEntity>> = vioraDao.getAllTasks()
    
    fun getTasksForList(listId: String): Flow<List<TaskEntity>> = vioraDao.getTasksForList(listId)

    fun getPendingCountFlow(): Flow<Int> = vioraDao.getPendingTaskCountFlow()

    suspend fun createTask(
        name: String,
        listId: String? = null,
        description: String? = null,
        teamId: String? = null,
        deadline: Long? = null,
        status: String = "todo",
        tags: String = ""
    ): TaskEntity {
        val now = System.currentTimeMillis()
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            status = status,
            listId = listId,
            teamId = teamId,
            deadline = deadline,
            tags = tags,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        vioraDao.insertTask(task)
        return task
    }

    suspend fun updateTaskStatus(taskId: String, newStatus: String) {
        val existing = vioraDao.getTaskById(taskId)
        if (existing != null) {
            val updated = existing.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis(),
                syncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE) SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE
            )
            vioraDao.insertTask(updated)
        } else {
            vioraDao.updateTaskStatus(taskId, newStatus)
        }
    }

    suspend fun updateTask(task: TaskEntity) {
        val updated = task.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (task.syncStatus == SyncStatus.PENDING_CREATE) SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE
        )
        vioraDao.insertTask(updated)
    }

    suspend fun deleteTask(taskId: String) {
        val existing = vioraDao.getTaskById(taskId)
        if (existing != null && existing.syncStatus == SyncStatus.PENDING_CREATE) {
            // Unsynced creation can be safely hard deleted
            vioraDao.hardDeleteTask(taskId)
        } else {
            vioraDao.softDeleteTask(taskId)
        }
    }
    
    // --- Lists & Teams ---
    fun getLists(): Flow<List<ListEntity>> = vioraDao.getAllLists()
    fun getTeams(): Flow<List<TeamEntity>> = vioraDao.getAllTeams()
    
    suspend fun createTeam(name: String, description: String? = null): TeamEntity {
        val now = System.currentTimeMillis()
        val team = TeamEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        vioraDao.insertTeam(team)
        return team
    }
    
    suspend fun createList(name: String, teamId: String, description: String? = null): ListEntity {
        val now = System.currentTimeMillis()
        val list = ListEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            teamId = teamId,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        vioraDao.insertList(list)
        return list
    }

    suspend fun deleteList(listId: String) {
        vioraDao.softDeleteList(listId)
    }

    suspend fun deleteTeam(teamId: String) {
        vioraDao.softDeleteTeam(teamId)
    }
}
