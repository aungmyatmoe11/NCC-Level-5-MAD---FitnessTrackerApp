package com.example.fitnesstrackingmobileapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_activities")
data class FitnessActivity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name = "user_id") val userId: String = "default_user",
        @ColumnInfo(name = "activity_type")
        val activityType: String, // RUNNING, CYCLING, WEIGHTLIFTING
        @ColumnInfo(name = "title") val title: String = "",
        @ColumnInfo(name = "description") val description: String = "",
        @ColumnInfo(name = "start_time") val startTime: Long = 0,
        @ColumnInfo(name = "end_time") val endTime: Long = 0,
        @ColumnInfo(name = "duration_seconds") val durationSeconds: Long = 0,
        @ColumnInfo(name = "distance_meters") val distanceMeters: Double = 0.0,
        @ColumnInfo(name = "calories_burned") val caloriesBurned: Double = 0.0,
        @ColumnInfo(name = "average_heart_rate") val averageHeartRate: Int = 0,
        @ColumnInfo(name = "max_heart_rate") val maxHeartRate: Int = 0,
        @ColumnInfo(name = "average_speed") val averageSpeed: Double = 0.0,
        @ColumnInfo(name = "max_speed") val maxSpeed: Double = 0.0,
        @ColumnInfo(name = "elevation_gain") val elevationGain: Double = 0.0,
        @ColumnInfo(name = "route_data") val routeData: String = "",
        @ColumnInfo(name = "notes") val notes: String = "",
        @ColumnInfo(name = "remote_id") val remoteId: String? = null, // For sync tracking
        @ColumnInfo(name = "sync_status")
        val syncStatus: String = "pending", // pending, synced, failed
        @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
        @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    // Computed properties
    val distanceKm: Double
        get() = distanceMeters / 1000.0

    val durationMinutes: Double
        get() = durationSeconds / 60.0

    val paceMinutesPerKm: Double
        get() = if (distanceKm > 0) durationMinutes / distanceKm else 0.0

    val isCompleted: Boolean
        get() = endTime > 0

    val isSynced: Boolean
        get() = syncStatus == "synced" && remoteId != null
}

// Specific activity types with additional data
@Entity(tableName = "weightlifting_sessions")
data class WeightliftingSession(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "activity_id") val activityId: Int,
        @ColumnInfo(name = "exercise_name") val exerciseName: String,
        @ColumnInfo(name = "sets") val sets: Int,
        @ColumnInfo(name = "reps") val reps: Int,
        @ColumnInfo(name = "weight_kg") val weightKg: Double,
        @ColumnInfo(name = "rest_time_seconds") val restTimeSeconds: Int = 0,
        @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_goals")
data class ActivityGoal(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "activity_type") val activityType: String,
        @ColumnInfo(name = "goal_type")
        val goalType: String, // DISTANCE, DURATION, CALORIES, FREQUENCY
        @ColumnInfo(name = "target_value") val targetValue: Double,
        @ColumnInfo(name = "current_value") val currentValue: Double = 0.0,
        @ColumnInfo(name = "time_period") val timePeriod: String, // DAILY, WEEKLY, MONTHLY
        @ColumnInfo(name = "start_date") val startDate: Long,
        @ColumnInfo(name = "end_date") val endDate: Long,
        @ColumnInfo(name = "is_active") val isActive: Boolean = true,
        @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Double
        get() = if (targetValue > 0) (currentValue / targetValue) * 100 else 0.0

    val isCompleted: Boolean
        get() = currentValue >= targetValue
}
