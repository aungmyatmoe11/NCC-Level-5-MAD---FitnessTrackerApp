package com.example.fitnesstrackingmobileapp.data

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.fitnesstrackingmobileapp.ApiService
import com.example.fitnesstrackingmobileapp.UserSession
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SyncWorker(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
        private const val SYNC_WORK_NAME = "fitness_data_sync"
        private const val SYNC_INTERVAL_HOURS = 6L

        fun schedulePeriodicSync(context: Context) {
            val constraints =
                    Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .setRequiresBatteryNotLow(true)
                            .build()

            val syncRequest =
                    PeriodicWorkRequestBuilder<SyncWorker>(SYNC_INTERVAL_HOURS, TimeUnit.HOURS)
                            .setConstraints(constraints)
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                            .build()

            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                            SYNC_WORK_NAME,
                            ExistingPeriodicWorkPolicy.KEEP,
                            syncRequest
                    )

            Log.d(TAG, "Periodic sync scheduled")
        }

        fun scheduleImmediateSync(context: Context) {
            val constraints =
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val syncRequest =
                    OneTimeWorkRequestBuilder<SyncWorker>()
                            .setConstraints(constraints)
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                            .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
            Log.d(TAG, "Immediate sync scheduled")
        }

        fun cancelAllWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWork()
            Log.d(TAG, "All background work cancelled")
        }
    }

    override suspend fun doWork(): Result =
            withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Starting data sync...")

                    val userId = UserSession.getUserId(applicationContext)
                    if (userId.isEmpty()) {
                        Log.w(TAG, "No user ID found, skipping sync")
                        return@withContext Result.failure()
                    }

                    val database = FitnessDatabase.getDatabase(applicationContext)
                    val fitnessActivityDao = database.fitnessActivityDao()

                    // Get unsynced activities (activities without remote ID)
                    val unsyncedActivities = fitnessActivityDao.getUnsyncedActivities(userId)

                    if (unsyncedActivities.isEmpty()) {
                        Log.d(TAG, "No unsynced activities found")
                        return@withContext Result.success()
                    }

                    Log.d(TAG, "Found ${unsyncedActivities.size} unsynced activities")

                    // Sync each activity to the server
                    var successCount = 0
                    var failureCount = 0

                    for (activity in unsyncedActivities) {
                        try {
                            val apiService = ApiService(applicationContext)
                            apiService.saveActivity(
                                    activity = activity,
                                    exercises = null,
                                    onSuccess = { message ->
                                        runBlocking {
                                            // Mark as synced
                                            fitnessActivityDao.markAsSynced(activity.id)
                                        }
                                        successCount++
                                        Log.d(
                                                TAG,
                                                "Activity ${activity.id} synced successfully: $message"
                                        )
                                    },
                                    onError = { error ->
                                        failureCount++
                                        Log.e(TAG, "Failed to sync activity ${activity.id}: $error")
                                    }
                            )
                        } catch (e: Exception) {
                            failureCount++
                            Log.e(TAG, "Error syncing activity ${activity.id}: ${e.message}")
                        }
                    }

                    Log.d(TAG, "Sync completed: $successCount successful, $failureCount failed")

                    return@withContext if (failureCount == 0) {
                        Result.success()
                    } else if (successCount > 0) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in sync worker: ${e.message}", e)
                    return@withContext Result.retry()
                }
            }
}
