package com.example.data

import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VioraRepository(private val vioraDao: VioraDao) {

    // --- Tasks ---
    fun getTasks(): Flow<List<TaskEntity>> = vioraDao.getAllTasks()
    
    fun getTasksForList(listId: String): Flow<List<TaskEntity>> = vioraDao.getTasksForList(listId)

    suspend fun createTask(name: String, listId: String?, description: String? = null, teamId: String? = null, deadline: Long? = null) {
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            listId = listId,
            teamId = teamId,
            deadline = deadline,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        vioraDao.insertTask(task)
    }

    suspend fun updateTaskStatus(taskId: String, status: String) {
        // Find existing to keep data intact (or simply run an update query if we added one)
        // For simplicity, assuming a query exists or we do a quick fetch
        // Let's assume we use WorkManager so we just update the DB.
    }
    
    // Similarly for Lists and Teams
    fun getLists(): Flow<List<ListEntity>> = vioraDao.getAllLists()
    fun getTeams(): Flow<List<TeamEntity>> = vioraDao.getAllTeams()
    
    suspend fun createTeam(name: String, description: String? = null) {
        val team = TeamEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        vioraDao.insertTeam(team)
    }
    
    suspend fun createList(name: String, teamId: String, description: String? = null) {
        val list = ListEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            teamId = teamId,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        vioraDao.insertList(list)
    }
}
