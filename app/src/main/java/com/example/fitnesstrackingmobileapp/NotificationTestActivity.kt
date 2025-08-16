package com.example.fitnesstrackingmobileapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstrackingmobileapp.notification.FitnessNotificationService
import com.google.android.material.button.MaterialButton

class NotificationTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NotificationTestActivity"
    }

    private lateinit var notificationService: FitnessNotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_test)

        notificationService = FitnessNotificationService(this)

        // Test different notification types
        findViewById<MaterialButton>(R.id.btnTestActivityReminder).setOnClickListener {
            testActivityReminder()
        }

        findViewById<MaterialButton>(R.id.btnTestMilestone).setOnClickListener {
            testMilestoneNotification()
        }

        findViewById<MaterialButton>(R.id.btnTestSyncStatus).setOnClickListener {
            testSyncStatusNotification()
        }

        findViewById<MaterialButton>(R.id.btnTestGoalAchievement).setOnClickListener {
            testGoalAchievementNotification()
        }

        findViewById<MaterialButton>(R.id.btnTestAllNotifications).setOnClickListener {
            testAllNotifications()
        }
    }

    private fun testActivityReminder() {
        try {
            notificationService.showActivityReminder()
            Toast.makeText(this, "Activity reminder notification sent!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Activity reminder notification test completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error testing activity reminder: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testMilestoneNotification() {
        try {
            // Simulate a milestone achievement
            notificationService.checkAndShowMilestones()
            Toast.makeText(this, "Milestone notification test completed!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Milestone notification test completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error testing milestone notification: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testSyncStatusNotification() {
        try {
            notificationService.showSyncStatusNotification(5, 0) // 5 synced, 0 failed
            Toast.makeText(this, "Sync status notification sent!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Sync status notification test completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error testing sync status notification: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testGoalAchievementNotification() {
        try {
            notificationService.checkAndShowGoalAchievements()
            Toast.makeText(this, "Goal achievement notification test completed!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Goal achievement notification test completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error testing goal achievement notification: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testAllNotifications() {
        try {
            // Test all notification types with a delay
            notificationService.showActivityReminder()
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                notificationService.showSyncStatusNotification(3, 1)
            }, 2000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                notificationService.checkAndShowMilestones()
            }, 4000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                notificationService.checkAndShowGoalAchievements()
            }, 6000)
            
            Toast.makeText(this, "All notification tests started! Check notifications.", Toast.LENGTH_LONG).show()
            Log.d(TAG, "All notification tests completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error testing all notifications: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} 