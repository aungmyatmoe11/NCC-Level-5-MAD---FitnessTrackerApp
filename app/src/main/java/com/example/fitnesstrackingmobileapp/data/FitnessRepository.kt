package com.example.fitnesstrackingmobileapp.data

import android.content.Context
import android.util.Log
import com.example.fitnesstrackingmobileapp.ApiService
import com.example.fitnesstrackingmobileapp.UserSession
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import kotlinx.coroutines.runBlocking

class FitnessRepository(private val context: Context) {

    companion object {
        private const val TAG = "FitnessRepository"
    }

    private val fitnessActivityDao = FitnessDatabase.getDatabase(context).fitnessActivityDao()
    private val apiService = ApiService(context)

    /**
     * Save activity with offline-first approach
     * 1. Save to local database first
     * 2. If network available, sync to server
     * 3. Mark as synced if successful
     */
    suspend fun saveActivity(activity: FitnessActivity): Result<Long> {
        return try {
            Log.d(TAG, "Saving activity: ${activity.title}")

            // Always save to local database first
            val localId = fitnessActivityDao.insertActivity(activity)
            Log.d(TAG, "Activity saved locally with ID: $localId")

            // Try to sync to server if network is available
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    Log.d(TAG, "Network available, syncing to server...")
                    apiService.saveActivity(
                            activity = activity,
                            exercises = null,
                            onSuccess = { message ->
                                runBlocking {
                                    fitnessActivityDao.markAsSynced(localId)
                                    fitnessActivityDao.deleteAllWeightliftingSessionsForUser(
                                            activity.userId
                                    )
                                    fitnessActivityDao.deleteAllActivitiesForUser(activity.userId)
                                }
                                Log.d(TAG, "Activity synced to server: $message")
                            },
                            onError = { error ->
                                Log.w(TAG, "Failed to sync activity to server: $error")
                                // Activity remains in local database with pending sync
                            }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing to server: ${e.message}")
                    // Activity remains in local database with pending sync
                }
            } else {
                Log.d(TAG, "No network available, activity saved locally only")
            }

            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving activity: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get activities for home screen with improved online/offline flow:
     * 1. Online: Get data from server API and show it
     * 2. Offline: Show only unsynced local data
     * 3. Offline to Online: Sync unsynced data, then delete local unsynced data after success
     */
    suspend fun getActivitiesForHomeScreen(userId: String): List<FitnessActivity> {
        return try {
            Log.d(TAG, "Getting activities for home screen for user: $userId")

            if (NetworkUtils.isNetworkAvailable(context)) {
                Log.d(TAG, "Network available - fetching from server")

                // 1. First sync any pending local data to server
                // val syncResult = syncPendingActivities()

                // 2. Get fresh data from server
                val serverActivities = fetchServerActivities(userId)
                Log.d(TAG, "Received ${serverActivities.size} activities from server!!!")

                // 3. Update local database with server data
                updateLocalDatabaseWithServerData(serverActivities)

                // 4. Return server data (fresh data)
                serverActivities
            } else {
                Log.d(TAG, "No network - showing only unsynced local data")

                // Show only unsynced local data when offline
                val unsyncedActivities = fitnessActivityDao.getUnsyncedActivities(userId)
                Log.d(TAG, "Found ${unsyncedActivities.size} unsynced activities locally")
                unsyncedActivities
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activities for home screen: ${e.message}")
            emptyList()
        }
    }

    /** Fetch activities from server */
    private suspend fun fetchServerActivities(userId: String): List<FitnessActivity> {
        return try {
            Log.d(TAG, "Fetching server activities for user: $userId")

            // Use runBlocking to handle async callback
            return runBlocking {
                val serverActivities = mutableListOf<FitnessActivity>()

                apiService.getUserActivities(
                        userId = userId,
                        onSuccess = { activities ->
                            Log.d(TAG, "Received ${activities.size} activities from serversssss")
                            for (activity in activities) {
                                Log.d(TAG, "Activity: ${activity.title} (${activity.activityType})")
                            }
                            serverActivities.addAll(activities)
                        },
                        onError = { error ->
                            Log.e(TAG, "Error fetching from server: $error")
                            Log.e(TAG, "User ID: $userId")
                        }
                )

                Log.d(TAG, "Returning ${serverActivities.size} server activities")
                serverActivities
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchServerActivities: ${e.message}")
            emptyList()
        }
    }

    /** Update local database with server data */
    private suspend fun updateLocalDatabaseWithServerData(serverActivities: List<FitnessActivity>) {
        try {
            runBlocking {
                for (activity in serverActivities) {
                    try {
                        // Check if activity already exists locally
                        val existingActivity =
                                fitnessActivityDao.getActivityByRemoteId(activity.remoteId)
                        if (existingActivity == null) {
                            // Insert new activity from server
                            fitnessActivityDao.insertActivity(activity)
                            Log.d(TAG, "Inserted new activity from server: ${activity.title}")
                        } else {
                            // Update existing activity if server data is newer
                            if (activity.updatedAt > existingActivity.updatedAt) {
                                fitnessActivityDao.updateActivity(activity)
                                Log.d(
                                        TAG,
                                        "Updated existing activity from server: ${activity.title}"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing server activity: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating local database: ${e.message}")
        }
    }

    /** Get recent activities for home screen */
    suspend fun getRecentActivities(userId: String, limit: Int = 5): List<FitnessActivity> {
        return try {
            Log.d(TAG, "Getting recent activities for user: $userId, limit: $limit")
            // For now, get all activities and take the most recent ones
            val allActivities = fitnessActivityDao.getActivitiesByUserId(userId)
            val recentActivities = allActivities.take(limit)
            Log.d(TAG, "Found ${recentActivities.size} recent activities")
            recentActivities
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent activities: ${e.message}")
            emptyList()
        }
    }

    /** Get all activities for a user (for Activity History) */
    suspend fun getAllActivitiesForUser(userId: String): List<FitnessActivity> {
        return try {
            Log.d(TAG, "Getting all activities for user: $userId")

            if (NetworkUtils.isNetworkAvailable(context)) {
                // Online: Get from server and update local DB
                val serverActivities = fetchServerActivities(userId)
                updateLocalDatabaseWithServerData(serverActivities)
                serverActivities
            } else {
                // Offline: Get from local DB
                val localActivities = fitnessActivityDao.getActivitiesByUserId(userId)
                Log.d(TAG, "Found ${localActivities.size} activities locally")
                localActivities
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all activities for user: ${e.message}")
            emptyList()
        }
    }

    /** Get activities by date range for stats */
    suspend fun getActivitiesByDateRange(
            userId: String,
            startTime: Long,
            endTime: Long
    ): List<FitnessActivity> {
        return try {
            Log.d(TAG, "Getting activities by date range for user: $userId")
            val activities = fitnessActivityDao.getActivitiesByDateRange(userId, startTime, endTime)
            Log.d(TAG, "Found ${activities.size} activities in date range")
            activities
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activities by date range: ${e.message}")
            emptyList()
        }
    }

    /** Sync pending activities to server and delete local unsynced data after success */
    suspend fun syncPendingActivities(): Result<Int> {
        return try {
            Log.d(TAG, "Syncing pending activities...")

            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.d(TAG, "No network available for sync")
                return Result.success(0)
            }

            val userId = UserSession.getUserId(context)
            val unsyncedActivities = fitnessActivityDao.getUnsyncedActivities(userId)
            Log.d(TAG, "Found ${unsyncedActivities.size} unsynced activities")

            if (unsyncedActivities.isEmpty()) {
                Log.d(TAG, "No unsynced activities to sync")
                return Result.success(0)
            }

            var syncedCount = 0
            var failedCount = 0
            val successfullySyncedIds = mutableListOf<Long>()

            for (activity in unsyncedActivities) {
                try {
                    apiService.saveActivity(
                            activity = activity,
                            exercises = null,
                            onSuccess = { message ->
                                runBlocking {
                                    fitnessActivityDao.markAsSynced(activity.id)
                                    successfullySyncedIds.add(activity.id)
                                }
                                syncedCount++
                                Log.d(TAG, "Activity ${activity.id} synced successfully: $message")
                            },
                            onError = { error ->
                                failedCount++
                                Log.e(TAG, "Failed to sync activity ${activity.id}: $error")
                            }
                    )
                } catch (e: Exception) {
                    failedCount++
                    Log.e(TAG, "Error syncing activity ${activity.id}: ${e.message}")
                }
            }

            // Delete successfully synced activities from local database
            if (successfullySyncedIds.isNotEmpty()) {
                Log.d(
                        TAG,
                        "Deleting ${successfullySyncedIds.size} successfully synced activities from local database"
                )
                for (activityId in successfullySyncedIds) {
                    try {
                        val activity = fitnessActivityDao.getActivityById(activityId)
                        if (activity != null) {
                            fitnessActivityDao.deleteActivity(activity)
                            Log.d(
                                    TAG,
                                    "Deleted synced activity ${activity.title} from local database"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting synced activity $activityId: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Sync completed. Synced $syncedCount activities, failed $failedCount")
            // delete
            fitnessActivityDao.deleteAllWeightliftingSessionsForUser(userId)
            fitnessActivityDao.deleteAllActivitiesForUser(userId)

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncPendingActivities: ${e.message}")
            Result.failure(e)
        }
    }

    /** Get unsynced count for UI indicators */
    suspend fun getUnsyncedCount(userId: String): Int {
        return try {
            fitnessActivityDao.getUnsyncedCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced count: ${e.message}")
            0
        }
    }

    /** Migrate data from server to local database */
    suspend fun migrateDataFromServer(): Result<Int> {
        return try {
            Log.d(TAG, "Migrating data from server...")

            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.d(TAG, "No network available for migration")
                return Result.success(0)
            }

            val userId = UserSession.getUserId(context)
            if (userId.isEmpty()) {
                Log.w(TAG, "No user ID available for migration")
                return Result.success(0)
            }

            var migratedCount = 0
            apiService.getUserActivities(
                    userId = userId,
                    onSuccess = { activities ->
                        Log.d(TAG, "Received ${activities.size} activities from server")
                        for (activity in activities) {
                            try {
                                runBlocking { fitnessActivityDao.insertActivity(activity) }
                                migratedCount++
                            } catch (e: Exception) {
                                Log.e(TAG, "Error migrating activity: ${e.message}")
                            }
                        }
                        Log.d(TAG, "Migration completed. Migrated $migratedCount activities")
                    },
                    onError = { error -> Log.e(TAG, "Error migrating data from server: $error") }
            )

            Result.success(migratedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error in migrateDataFromServer: ${e.message}")
            Result.failure(e)
        }
    }

    /** Get activity stats for home screen */
    suspend fun getActivityStats(
            userId: String,
            activityType: String = "RUNNING",
            startTime: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
    ): ActivityStats? {
        return try {
            Log.d(TAG, "Getting activity stats for user: $userId, activityType: $activityType")
            fitnessActivityDao.getActivityStats(activityType, startTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activity stats: ${e.message}")
            null
        }
    }

    /** Get weightlifting sessions for an activity */
    suspend fun getWeightliftingSessions(activityId: Long): List<WeightliftingSession> {
        return try {
            // This would need to be implemented in the DAO
            // For now, return empty list
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weightlifting sessions: ${e.message}")
            emptyList()
        }
    }

    /** Clear all local data for a user */
    suspend fun clearAllDataForUser(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Clearing all data for user: $userId")
            fitnessActivityDao.deleteAllWeightliftingSessionsForUser(userId)
            fitnessActivityDao.deleteAllActivitiesForUser(userId)
            Log.d(TAG, "All data cleared for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data for user: ${e.message}")
            Result.failure(e)
        }
    }

    /** Clear all local data */
    suspend fun clearAllData(): Result<Unit> {
        return try {
            Log.d(TAG, "Clearing all local data")
            fitnessActivityDao.deleteAllWeightliftingSessions()
            fitnessActivityDao.deleteAllActivities()
            Log.d(TAG, "All local data cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all data: ${e.message}")
            Result.failure(e)
        }
    }

    /** Sync and resolve conflicts with server data */
    suspend fun syncAndResolveConflicts(userId: String): Result<Int> {
        return try {
            Log.d(TAG, "Syncing and resolving conflicts for user: $userId")

            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.d(TAG, "No network available for conflict resolution")
                return Result.success(0)
            }

            // Get server activities
            val serverActivities = mutableListOf<FitnessActivity>()
            apiService.getUserActivities(
                    userId = userId,
                    onSuccess = { activities ->
                        Log.d(TAG, "Received ${activities.size} activities from server")
                        serverActivities.addAll(activities)
                    },
                    onError = { error -> Log.e(TAG, "Error fetching server activities: $error") }
            )

            // If server has no data, clear local synced data
            if (serverActivities.isEmpty()) {
                Log.d(TAG, "Server has no data, clearing local synced data")
                val localActivities = fitnessActivityDao.getActivitiesByUserId(userId)
                val syncedActivities = localActivities.filter { it.remoteId != null }

                if (syncedActivities.isNotEmpty()) {
                    Log.d(TAG, "Found ${syncedActivities.size} synced activities to remove")
                    for (activity in syncedActivities) {
                        fitnessActivityDao.deleteActivity(activity)
                    }
                }
                return Result.success(syncedActivities.size)
            }

            // Update local database with server data
            var updatedCount = 0
            for (activity in serverActivities) {
                try {
                    val existingActivity =
                            fitnessActivityDao.getActivityByRemoteId(activity.remoteId)
                    if (existingActivity == null) {
                        fitnessActivityDao.insertActivity(activity)
                        updatedCount++
                    } else {
                        fitnessActivityDao.updateActivity(activity)
                        updatedCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing server activity: ${e.message}")
                }
            }

            Log.d(TAG, "Conflict resolution completed. Updated $updatedCount activities")
            Result.success(updatedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncAndResolveConflicts: ${e.message}")
            Result.failure(e)
        }
    }
}
