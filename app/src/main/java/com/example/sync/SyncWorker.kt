package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.VioraRepository
import com.example.data.AppDatabase
import com.example.network.viora.VioraNetworkModule
import com.example.model.viora.SyncPushDto
import com.example.model.viora.SyncMutationsDto
import com.example.model.viora.SyncTaskMutationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (VioraNetworkModule.getTokenManager() == null) {
                VioraNetworkModule.init(applicationContext)
            }
            
            if (VioraNetworkModule.getTokenManager()?.getAccessToken() == null) {
                Log.d("SyncWorker", "No access token, skipping sync.")
                return@withContext Result.failure()
            }

            Log.d("SyncWorker", "Starting sync...")
            val dao = AppDatabase.getDatabase(applicationContext).vioraDao()
            
            // 1. PUSH local pending changes
            val pendingTasks = dao.getPendingTasks()
            if (pendingTasks.isNotEmpty()) {
                val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }

                val taskMutations = pendingTasks.map {
                    SyncTaskMutationDto(
                        id = it.id,
                        updatedAt = isoFormat.format(java.util.Date(it.updatedAt)),
                        isDeleted = it.isDeleted,
                        name = it.name,
                        description = it.description,
                        status = it.status,
                        listId = it.listId,
                        isArchived = it.isArchived
                    )
                }
                
                val pushReq = SyncPushDto(
                    mutations = SyncMutationsDto(
                        tasks = taskMutations,
                        lists = null,
                        teams = null
                    )
                )
                
                val res = VioraNetworkModule.api.syncPush(pushReq)
                if (res.success) {
                    val processedIds = res.data?.processedIds ?: pendingTasks.map { it.id }
                    if (processedIds.isNotEmpty()) {
                        dao.updateTasksSyncStatus(processedIds, com.example.data.entity.SyncStatus.SYNCED)
                        // Hard delete local tasks that were marked as deleted and acknowledged by server
                        pendingTasks.filter { it.isDeleted && processedIds.contains(it.id) }.forEach {
                            dao.hardDeleteTask(it.id)
                        }
                    }
                    Log.d("SyncWorker", "Push successful for ${processedIds.size} tasks")
                }
            }
            
            // 2. PULL server changes
            val pullRes = VioraNetworkModule.api.syncPull(null)
            if (pullRes.success && pullRes.data != null) {
                val updatedTasks = pullRes.data.updatedData?.tasks ?: emptyList()
                val deletedTaskIds = pullRes.data.deletedIds?.tasks ?: emptyList()

                // Delete local tasks deleted on server
                deletedTaskIds.forEach { deletedId ->
                    dao.hardDeleteTask(deletedId)
                }

                // Insert or update server tasks locally with Last-Write-Wins (LWW) conflict check
                if (updatedTasks.isNotEmpty()) {
                    for (t in updatedTasks) {
                        val localTask = dao.getTaskById(t.id)
                        val serverUpdatedAt = try {
                            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            t.updatedAt?.let { isoFormat.parse(it)?.time } ?: System.currentTimeMillis()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }

                        // LWW Check: If local item has unsynced changes and is newer than server, preserve local state
                        if (localTask != null && localTask.syncStatus != com.example.data.entity.SyncStatus.SYNCED && localTask.updatedAt > serverUpdatedAt) {
                            Log.d("SyncWorker", "LWW Conflict: Local task ${t.id} is newer (${localTask.updatedAt} > $serverUpdatedAt). Keeping local change.")
                            continue
                        }

                        val entity = com.example.data.entity.TaskEntity(
                            id = t.id,
                            name = t.name,
                            description = t.description,
                            status = t.status,
                            listId = t.listId,
                            teamId = t.team?.id,
                            tags = t.tags?.joinToString(",") { it.name } ?: "",
                            isArchived = t.isArchived,
                            isDeleted = false,
                            createdAt = localTask?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = serverUpdatedAt,
                            syncStatus = com.example.data.entity.SyncStatus.SYNCED
                        )
                        dao.insertTask(entity)
                    }
                }
                Log.d("SyncWorker", "Pull successful: updated=${updatedTasks.size}, deleted=${deletedTaskIds.size}")
            }

            Result.success()
        } catch (e: HttpException) {
            Log.e("SyncWorker", "Sync failed with HTTP error", e)
            if (e.code() == 401) {
                Result.failure()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}
