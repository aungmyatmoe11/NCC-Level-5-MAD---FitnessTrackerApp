package com.example.fitnesstrackingmobileapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object BatteryOptimizationUtils {

    private const val TAG = "BatteryOptimizationUtils"

    /**
     * Check if battery optimization is enabled for the app
     */
    fun isBatteryOptimizationEnabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Request to disable battery optimization for the app
     */
    fun requestDisableBatteryOptimization(activity: Activity) {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
            Log.d(TAG, "Battery optimization request sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting battery optimization: ${e.message}")
        }
    }

    /**
     * Check if the device is in battery saver mode
     */
    fun isBatterySaverEnabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }

    /**
     * Check if the device is in doze mode (Android 6.0+)
     */
    fun isDeviceIdleMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isDeviceIdleMode
    }

    /**
     * Get battery level
     */
    fun getBatteryLevel(context: Context): Int {
        val batteryStatus = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }

    /**
     * Check if battery is low
     */
    fun isBatteryLow(context: Context): Boolean {
        val batteryLevel = getBatteryLevel(context)
        return batteryLevel in 0..15
    }

    /**
     * Check if battery is charging
     */
    fun isBatteryCharging(context: Context): Boolean {
        val batteryStatus = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                status == android.os.BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * Get recommended sync interval based on battery level and charging status
     */
    fun getRecommendedSyncInterval(context: Context): Long {
        val batteryLevel = getBatteryLevel(context)
        val isCharging = isBatteryCharging(context)
        val isLowBattery = isBatteryLow(context)

        return when {
            isCharging -> 1L // 1 hour when charging
            isLowBattery -> 6L // 6 hours when battery is low
            batteryLevel < 30 -> 4L // 4 hours when battery is below 30%
            batteryLevel < 50 -> 3L // 3 hours when battery is below 50%
            else -> 2L // 2 hours when battery is above 50%
        }
    }

    /**
     * Check if GPS tracking should be enabled based on battery level
     */
    fun shouldEnableGPSTracking(context: Context): Boolean {
        val batteryLevel = getBatteryLevel(context)
        val isCharging = isBatteryCharging(context)
        
        return when {
            isCharging -> true // Always enable when charging
            batteryLevel < 20 -> false // Disable when battery is very low
            batteryLevel < 50 -> true // Enable with reduced frequency when battery is low
            else -> true // Always enable when battery is good
        }
    }

    /**
     * Get GPS update interval based on battery level
     */
    fun getGPSUpdateInterval(context: Context): Long {
        val batteryLevel = getBatteryLevel(context)
        val isCharging = isBatteryCharging(context)
        
        return when {
            isCharging -> 5000L // 5 seconds when charging
            batteryLevel < 30 -> 30000L // 30 seconds when battery is low
            batteryLevel < 50 -> 15000L // 15 seconds when battery is below 50%
            else -> 10000L // 10 seconds when battery is good
        }
    }

    /**
     * Check if background sync should be enabled
     */
    fun shouldEnableBackgroundSync(context: Context): Boolean {
        val batteryLevel = getBatteryLevel(context)
        val isCharging = isBatteryCharging(context)
        val isBatteryOptimizationEnabled = isBatteryOptimizationEnabled(context)
        
        return when {
            isCharging -> true // Always enable when charging
            !isBatteryOptimizationEnabled -> true // Enable if battery optimization is disabled
            batteryLevel < 20 -> false // Disable when battery is very low
            batteryLevel < 50 -> true // Enable with reduced frequency when battery is low
            else -> true // Always enable when battery is good
        }
    }

    /**
     * Get recommended notification frequency based on battery level
     */
    fun getNotificationFrequency(context: Context): Long {
        val batteryLevel = getBatteryLevel(context)
        val isCharging = isBatteryCharging(context)
        
        return when {
            isCharging -> 3600000L // 1 hour when charging
            batteryLevel < 30 -> 7200000L // 2 hours when battery is low
            batteryLevel < 50 -> 5400000L // 1.5 hours when battery is below 50%
            else -> 3600000L // 1 hour when battery is good
        }
    }
} 