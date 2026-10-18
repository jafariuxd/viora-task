package com.example.data.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VioraDao {
    // --- Tasks ---
    @Query("SELECT * FROM tasks WHERE isDeleted = 0")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE listId = :listId AND isDeleted = 0")
    fun getTasksForList(listId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET syncStatus = :status WHERE id = :id")
    suspend fun updateTaskSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE tasks SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateTasksSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE tasks SET isDeleted = 1, syncStatus = 'PENDING_DELETE', updatedAt = :now WHERE id = :id")
    suspend fun softDeleteTask(id: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun hardDeleteTask(id: String)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isDeleted = 0")
    suspend fun getAllTasksList(): List<TaskEntity>

    // --- Lists ---
    @Query("SELECT * FROM lists WHERE isDeleted = 0")
    fun getAllLists(): Flow<List<ListEntity>>

    @Query("SELECT * FROM lists WHERE teamId = :teamId AND isDeleted = 0")
    fun getListsForTeam(teamId: String): Flow<List<ListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<ListEntity>)

    @Query("SELECT * FROM lists WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingLists(): List<ListEntity>

    @Query("UPDATE lists SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateListsSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("SELECT * FROM lists WHERE isDeleted = 0")
    suspend fun getAllListsList(): List<ListEntity>

    // --- Teams ---
    @Query("SELECT * FROM teams WHERE isDeleted = 0")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("SELECT * FROM teams WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingTeams(): List<TeamEntity>

    @Query("UPDATE teams SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateTeamsSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("SELECT * FROM teams WHERE isDeleted = 0")
    suspend fun getAllTeamsList(): List<TeamEntity>
    
    @Query("UPDATE tasks SET status = :status, syncStatus = 'PENDING_UPDATE', updatedAt = :now WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE lists SET isDeleted = 1, syncStatus = 'PENDING_DELETE', updatedAt = :now WHERE id = :id")
    suspend fun softDeleteList(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE teams SET isDeleted = 1, syncStatus = 'PENDING_DELETE', updatedAt = :now WHERE id = :id")
    suspend fun softDeleteTeam(id: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM lists WHERE id = :id")
    suspend fun hardDeleteList(id: String)

    @Query("DELETE FROM teams WHERE id = :id")
    suspend fun hardDeleteTeam(id: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE syncStatus != 'SYNCED'")
    fun getPendingTaskCountFlow(): Flow<Int>
}
