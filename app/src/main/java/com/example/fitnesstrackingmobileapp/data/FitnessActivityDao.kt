package com.example.fitnesstrackingmobileapp.data

import androidx.room.*
import androidx.room.ColumnInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessActivityDao {

        // Basic CRUD operations
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertActivity(activity: FitnessActivity): Long

        @Update suspend fun updateActivity(activity: FitnessActivity)

        @Delete suspend fun deleteActivity(activity: FitnessActivity)

        @Query("SELECT * FROM fitness_activities ORDER BY created_at DESC")
        fun getAllActivities(): Flow<List<FitnessActivity>>

        @Query("SELECT * FROM fitness_activities WHERE id = :activityId")
        suspend fun getActivityById(activityId: Long): FitnessActivity?

        @Query("SELECT * FROM fitness_activities WHERE user_id = :userId ORDER BY start_time DESC")
        suspend fun getActivitiesByUserId(userId: String): List<FitnessActivity>

        @Query(
                "SELECT * FROM fitness_activities WHERE user_id = :userId AND start_time BETWEEN :startTime AND :endTime ORDER BY start_time DESC"
        )
        suspend fun getActivitiesByDateRange(
                userId: String,
                startTime: Long,
                endTime: Long
        ): List<FitnessActivity>

        @Query(
                "SELECT * FROM fitness_activities WHERE user_id = :userId AND (:activityType IS NULL OR activity_type = :activityType) AND (:startTime IS NULL OR start_time >= :startTime) ORDER BY start_time DESC"
        )
        suspend fun getActivitiesByTypeAndDate(
                userId: String,
                activityType: String?,
                startTime: Long?
        ): List<FitnessActivity>

        // Activity type specific queries
        @Query(
                "SELECT * FROM fitness_activities WHERE activity_type = :activityType ORDER BY created_at DESC"
        )
        fun getActivitiesByType(activityType: String): Flow<List<FitnessActivity>>

        @Query(
                "SELECT * FROM fitness_activities WHERE activity_type = :activityType AND start_time >= :startTime ORDER BY start_time DESC"
        )
        fun getActivitiesByTypeAndDate(
                activityType: String,
                startTime: Long
        ): Flow<List<FitnessActivity>>

        // Sync-related queries
        @Query(
                "SELECT * FROM fitness_activities WHERE user_id = :userId AND remote_id IS NULL ORDER BY created_at ASC"
        )
        suspend fun getUnsyncedActivities(userId: String): List<FitnessActivity>

        @Query("UPDATE fitness_activities SET remote_id = :remoteId WHERE id = :localId")
        suspend fun markAsSynced(localId: Long, remoteId: String = "synced")

        @Query(
                "SELECT COUNT(*) FROM fitness_activities WHERE user_id = :userId AND remote_id IS NULL"
        )
        suspend fun getUnsyncedCount(userId: String): Int

        @Query("SELECT * FROM fitness_activities WHERE remote_id = :remoteId LIMIT 1")
        suspend fun getActivityByRemoteId(remoteId: String?): FitnessActivity?

        @Query("DELETE FROM fitness_activities WHERE user_id = :userId")
        suspend fun deleteAllActivitiesForUser(userId: String)

        @Query(
                "DELETE FROM weightlifting_sessions WHERE activity_id IN (SELECT id FROM fitness_activities WHERE user_id = :userId)"
        )
        suspend fun deleteAllWeightliftingSessionsForUser(userId: String)

        @Query("DELETE FROM fitness_activities") suspend fun deleteAllActivities()

        @Query("DELETE FROM weightlifting_sessions") suspend fun deleteAllWeightliftingSessions()

        // Analytics and statistics
        @Query(
                """
        SELECT 
            COUNT(*) as totalActivities,
            SUM(duration_seconds) as totalDuration,
            SUM(distance_meters) as totalDistance,
            SUM(calories_burned) as totalCalories,
            AVG(average_speed) as avgSpeed,
            MAX(max_speed) as maxSpeed
        FROM fitness_activities 
        WHERE activity_type = :activityType AND start_time >= :startTime
    """
        )
        suspend fun getActivityStats(activityType: String, startTime: Long): ActivityStats?

        @Query(
                """
        SELECT 
            activity_type,
            COUNT(*) as count,
            SUM(duration_seconds) as totalDuration,
            SUM(calories_burned) as totalCalories,
            SUM(distance_meters) as totalDistance
        FROM fitness_activities 
        WHERE start_time >= :startTime
        GROUP BY activity_type
    """
        )
        suspend fun getActivitySummary(startTime: Long): List<ActivitySummary>

        // Recent activities
        @Query("SELECT * FROM fitness_activities ORDER BY created_at DESC LIMIT :limit")
        fun getRecentActivities(limit: Int = 10): Flow<List<FitnessActivity>>

        // Personal records
        @Query(
                """
        SELECT * FROM fitness_activities 
        WHERE activity_type = :activityType 
        ORDER BY distance_meters DESC 
        LIMIT 1
    """
        )
        suspend fun getLongestDistance(activityType: String): FitnessActivity?

        @Query(
                """
        SELECT * FROM fitness_activities 
        WHERE activity_type = :activityType 
        ORDER BY average_speed DESC 
        LIMIT 1
    """
        )
        suspend fun getFastestActivity(activityType: String): FitnessActivity?

        @Query(
                """
        SELECT * FROM fitness_activities 
        WHERE activity_type = :activityType 
        ORDER BY duration_seconds DESC 
        LIMIT 1
    """
        )
        suspend fun getLongestDuration(activityType: String): FitnessActivity?

        // Weightlifting specific
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertWeightliftingSession(session: WeightliftingSession): Long

        @Query("SELECT * FROM weightlifting_sessions WHERE activity_id = :activityId")
        suspend fun getWeightliftingSessions(activityId: Long): List<WeightliftingSession>

        // Goals
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertGoal(goal: ActivityGoal): Long

        @Update suspend fun updateGoal(goal: ActivityGoal)

        @Query("SELECT * FROM activity_goals WHERE is_active = 1 ORDER BY created_at DESC")
        fun getActiveGoals(): Flow<List<ActivityGoal>>

        @Query("SELECT * FROM activity_goals WHERE activity_type = :activityType AND is_active = 1")
        fun getGoalsByActivityType(activityType: String): Flow<List<ActivityGoal>>

        // Progress tracking
        @Query(
                """
        SELECT 
            SUM(distance_meters) as totalDistance,
            SUM(calories_burned) as totalCalories,
            SUM(duration_seconds) as totalDuration
        FROM fitness_activities 
        WHERE activity_type = :activityType 
        AND start_time >= :startTime 
        AND start_time <= :endTime
    """
        )
        suspend fun getProgressForPeriod(
                activityType: String,
                startTime: Long,
                endTime: Long
        ): ProgressData?
}

// Data classes for analytics
data class ActivityStats(
        val totalActivities: Int,
        val totalDuration: Long,
        val totalDistance: Double,
        val totalCalories: Double,
        val avgSpeed: Double,
        val maxSpeed: Double
)

data class ActivitySummary(
        @ColumnInfo(name = "activity_type") val activityType: String,
        val count: Int,
        val totalDuration: Long,
        val totalCalories: Double,
        val totalDistance: Double
)

data class ProgressData(
        val totalDistance: Double,
        val totalCalories: Double,
        val totalDuration: Long
)
