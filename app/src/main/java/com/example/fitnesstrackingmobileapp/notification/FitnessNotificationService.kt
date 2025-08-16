package com.example.fitnesstrackingmobileapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fitnesstrackingmobileapp.HomeScreenActivity
import com.example.fitnesstrackingmobileapp.R
import com.example.fitnesstrackingmobileapp.UserSession
import com.example.fitnesstrackingmobileapp.data.ActivityGoal
import com.example.fitnesstrackingmobileapp.data.FitnessRepository
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitnessNotificationService(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_ACTIVITY_REMINDER = "activity_reminder"
        private const val CHANNEL_ID_GOAL_ACHIEVEMENT = "goal_achievement"
        private const val CHANNEL_ID_MILESTONE = "milestone"
        private const val CHANNEL_ID_SYNC = "sync_status"

        private const val NOTIFICATION_ID_ACTIVITY_REMINDER = 1001
        private const val NOTIFICATION_ID_GOAL_ACHIEVEMENT = 1002
        private const val NOTIFICATION_ID_MILESTONE = 1003
        private const val NOTIFICATION_ID_SYNC = 1004
    }

    private val repository = FitnessRepository(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels =
                    listOf(
                            NotificationChannel(
                                            CHANNEL_ID_ACTIVITY_REMINDER,
                                            "Activity Reminders",
                                            NotificationManager.IMPORTANCE_DEFAULT
                                    )
                                    .apply {
                                        description = "Reminders to log your daily activities"
                                    },
                            NotificationChannel(
                                            CHANNEL_ID_GOAL_ACHIEVEMENT,
                                            "Goal Achievements",
                                            NotificationManager.IMPORTANCE_HIGH
                                    )
                                    .apply { description = "Notifications for goal achievements" },
                            NotificationChannel(
                                            CHANNEL_ID_MILESTONE,
                                            "Milestones",
                                            NotificationManager.IMPORTANCE_DEFAULT
                                    )
                                    .apply { description = "Notifications for fitness milestones" },
                            NotificationChannel(
                                            CHANNEL_ID_SYNC,
                                            "Sync Status",
                                            NotificationManager.IMPORTANCE_LOW
                                    )
                                    .apply { description = "Data sync status notifications" }
                    )

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { channel -> notificationManager.createNotificationChannel(channel) }
        }
    }

    // MARK: - Activity Reminders

    fun showActivityReminder() {
        if (!hasNotificationPermission()) {
            Log.w("FitnessNotificationService", "Notification permission not granted")
            return
        }

        val intent = Intent(context, HomeScreenActivity::class.java)
        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID_ACTIVITY_REMINDER)
                        .setSmallIcon(R.drawable.baseline_directions_run_24)
                        .setContentTitle("Time for a Workout!")
                        .setContentText("Don't forget to log your daily activity")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

        try {
            NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_ACTIVITY_REMINDER, notification)
        } catch (e: SecurityException) {
            Log.e(
                    "FitnessNotificationService",
                    "Security exception when showing notification: ${e.message}"
            )
        }
    }

    // MARK: - Goal Achievement Notifications

    fun checkAndShowGoalAchievements() {
        scope.launch {
            try {
                // For now, skip goal achievements until goals are implemented
                Log.d(
                        "FitnessNotificationService",
                        "Goal achievements check skipped - not implemented yet"
                )
            } catch (e: Exception) {
                Log.e(
                        "FitnessNotificationService",
                        "Error checking goal achievements: ${e.message}"
                )
            }
        }
    }

    private fun showGoalAchievementNotification(goal: ActivityGoal) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, HomeScreenActivity::class.java)
        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID_GOAL_ACHIEVEMENT)
                        .setSmallIcon(R.drawable.baseline_fitness_center_24)
                        .setContentTitle("Goal Achieved! 🎉")
                        .setContentText(
                                "You've reached your ${goal.goalType} goal for ${goal.activityType}"
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

        try {
            NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_GOAL_ACHIEVEMENT, notification)
        } catch (e: SecurityException) {
            Log.e(
                    "FitnessNotificationService",
                    "Security exception when showing goal notification: ${e.message}"
            )
        }
    }

    // MARK: - Milestone Notifications

    fun checkAndShowMilestones() {
        scope.launch {
            try {
                // Check for distance milestones
                checkDistanceMilestones()

                // Check for streak milestones
                checkStreakMilestones()

                // Check for personal records
                checkPersonalRecords()
            } catch (e: Exception) {
                Log.e("FitnessNotificationService", "Error checking milestones: ${e.message}")
            }
        }
    }

    private suspend fun checkDistanceMilestones() {
        try {
            val userId = UserSession.getUserId(context)
            if (userId.isNotEmpty()) {
                val activities = repository.getActivitiesForHomeScreen(userId)
                val totalDistance = activities.sumOf { it.distanceMeters } / 1000.0 // Convert to km

                val milestones = listOf(5.0, 10.0, 25.0, 50.0, 100.0)
                for (milestone in milestones) {
                    if (totalDistance >= milestone && !hasShownMilestone("distance_$milestone")) {
                        showMilestoneNotification(
                                "Distance Milestone",
                                "You've reached ${milestone}km total distance!"
                        )
                        markMilestoneShown("distance_$milestone")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FitnessNotificationService", "Error checking distance milestones: ${e.message}")
        }
    }

    private suspend fun checkStreakMilestones() {
        // Implementation for streak checking
        // This would check consecutive days of activity
    }

    private suspend fun checkPersonalRecords() {
        // Implementation for personal records
        // This would check for new personal bests
    }

    private fun showMilestoneNotification(title: String, message: String) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, HomeScreenActivity::class.java)
        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID_MILESTONE)
                        .setSmallIcon(R.drawable.baseline_local_fire_department_24)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MILESTONE, notification)
        } catch (e: SecurityException) {
            Log.e(
                    "FitnessNotificationService",
                    "Security exception when showing milestone notification: ${e.message}"
            )
        }
    }

    // MARK: - Sync Status Notifications

    fun showSyncStatusNotification(syncedCount: Int, failedCount: Int) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, HomeScreenActivity::class.java)
        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val message =
                when {
                    syncedCount > 0 && failedCount == 0 -> "All activities synced successfully"
                    syncedCount > 0 && failedCount > 0 ->
                            "$syncedCount activities synced, $failedCount failed"
                    else -> "Sync completed"
                }

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID_SYNC)
                        .setSmallIcon(R.drawable.baseline_save_alt_24)
                        .setContentTitle("Data Sync")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SYNC, notification)
        } catch (e: SecurityException) {
            Log.e(
                    "FitnessNotificationService",
                    "Security exception when showing sync notification: ${e.message}"
            )
        }
    }

    // MARK: - Utility Methods

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true // For older Android versions, notification permission is granted by default
        }
    }

    private fun hasShownMilestone(milestoneKey: String): Boolean {
        val sharedPrefs = context.getSharedPreferences("milestones", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(milestoneKey, false)
    }

    private fun markMilestoneShown(milestoneKey: String) {
        val sharedPrefs = context.getSharedPreferences("milestones", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(milestoneKey, true).apply()
    }

    // MARK: - Scheduled Notifications

    fun scheduleDailyReminder() {
        // This would use AlarmManager or WorkManager to schedule daily reminders
        // Implementation depends on your scheduling strategy
    }

    fun scheduleWeeklyProgressReport() {
        // This would schedule weekly progress reports
        // Implementation depends on your scheduling strategy
    }
}
