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
                val taskMutations = pendingTasks.map {
                    SyncTaskMutationDto(
                        id = it.id,
                        updatedAt = "2024-05-20T10:45:00.000Z", // convert it.updatedAt to ISO
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
                    // Update syncStatus to SYNCED
                    Log.d("SyncWorker", "Push successful")
                }
            }
            
            // 2. PULL server changes
            val pullRes = VioraNetworkModule.api.syncPull(null)
            if (pullRes.success) {
                // Update local DB
                Log.d("SyncWorker", "Pull successful")
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
