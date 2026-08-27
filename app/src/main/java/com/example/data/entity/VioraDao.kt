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

    // --- Teams ---
    @Query("SELECT * FROM teams WHERE isDeleted = 0")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("SELECT * FROM teams WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingTeams(): List<TeamEntity>
    
    // --- Subtasks ---
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId AND isDeleted = 0")
    fun getSubtasksForTask(taskId: String): Flow<List<SubtaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("SELECT * FROM subtasks WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSubtasks(): List<SubtaskEntity>
}
