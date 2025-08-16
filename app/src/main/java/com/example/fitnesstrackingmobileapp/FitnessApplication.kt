package com.example.fitnesstrackingmobileapp

import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import com.example.fitnesstrackingmobileapp.data.SyncWorker
import com.example.fitnesstrackingmobileapp.notification.FitnessNotificationService

class FitnessApplication : Application() {

    companion object {
        private const val TAG = "FitnessApplication"
    }

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize WorkManager
            initializeWorkManager()
            
            // Initialize notification service
            initializeNotificationService()
            
            // Schedule periodic sync
            schedulePeriodicSync()
            
            Log.d(TAG, "FitnessApplication initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FitnessApplication: ${e.message}", e)
        }
    }

    private fun initializeWorkManager() {
        try {
            // WorkManager is automatically initialized by the system
            // We just need to ensure it's available
            WorkManager.getInstance(this)
            Log.d(TAG, "WorkManager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing WorkManager: ${e.message}", e)
        }
    }

    private fun initializeNotificationService() {
        try {
            // Initialize notification channels
            val notificationService = FitnessNotificationService(this)
            Log.d(TAG, "Notification service initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing notification service: ${e.message}", e)
        }
    }

    private fun schedulePeriodicSync() {
        try {
            // Schedule periodic data sync
            SyncWorker.schedulePeriodicSync(this)
            Log.d(TAG, "Periodic sync scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling periodic sync: ${e.message}", e)
        }
    }
} 