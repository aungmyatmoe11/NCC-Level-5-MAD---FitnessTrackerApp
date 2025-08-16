package com.example.fitnesstrackingmobileapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import com.example.fitnesstrackingmobileapp.data.FitnessActivityDao
import com.example.fitnesstrackingmobileapp.data.FitnessDatabase
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import com.google.android.gms.location.*
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class CyclingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CyclingActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val UPDATE_INTERVAL = 1000L // 1 second
        private const val FASTEST_INTERVAL = 500L // 500ms
    }

    private lateinit var fitnessActivityDao: FitnessActivityDao
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // UI Components
    private lateinit var chronometer: Chronometer
    private lateinit var distanceText: TextView
    private lateinit var speedText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var startStopButton: Button
    private lateinit var toolbar: MaterialToolbar

    // Tracking variables
    private var isTracking = false
    private var startTime = 0L
    private var totalDistance = 0.0
    private var currentSpeed = 0.0
    private var maxSpeed = 0.0
    private var averageSpeed = 0.0
    private var totalCalories = 0.0
    private var previousLocation: Location? = null
    private val locationList = mutableListOf<Location>()
    private var userId: String = "default_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cycling)

        try {
            // Initialize database
            fitnessActivityDao = FitnessDatabase.getDatabase(this).fitnessActivityDao()

            // Get user ID from session
            userId = UserSession.getUserId(this)

            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Initialize UI
            initializeUI()

            // Setup location callback
            setupLocationCallback()

            // Check GPS status
            checkGPSStatus()

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing activity: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            finish()
        }
    }

    private fun initializeUI() {
        try {
            // Initialize UI components
            chronometer = findViewById(R.id.chronometer)
            distanceText = findViewById(R.id.distanceText)
            speedText = findViewById(R.id.speedText)
            caloriesText = findViewById(R.id.caloriesText)
            startStopButton = findViewById(R.id.startStopButton)
            toolbar = findViewById(R.id.toolbar)

            // Setup toolbar with proper navigation
            toolbar.setNavigationOnClickListener {
                Log.d(TAG, "Navigation clicked - finishing activity")
                finish()
            }

            // Setup start/stop button with debug logging
            startStopButton.setOnClickListener {
                Log.d(TAG, "Start/Stop button clicked. Current tracking state: $isTracking")
                try {
                    if (isTracking) {
                        stopTracking()
                    } else {
                        startTracking()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in button click: ${e.message}")
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Initialize text displays
            updateDistanceDisplay()
            updateSpeedDisplay()
            updateCaloriesDisplay()

            Log.d(TAG, "UI initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UI: ${e.message}")
            Toast.makeText(this, "Error setting up UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLocationCallback() {
        locationCallback =
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location -> updateLocation(location) }
                    }
                }
    }

    private fun checkGPSStatus() {
        try {
            val locationManager =
                    getSystemService(LOCATION_SERVICE) as android.location.LocationManager
            val isGPSEnabled =
                    locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                    locationManager.isProviderEnabled(
                            android.location.LocationManager.NETWORK_PROVIDER
                    )

            if (!isGPSEnabled && !isNetworkEnabled) {
                showGPSDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking GPS status: ${e.message}")
        }
    }

    private fun showGPSDialog() {
        AlertDialog.Builder(this)
                .setTitle("GPS Required")
                .setMessage(
                        "GPS is required for accurate tracking. Please enable GPS in your device settings."
                )
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .show()
    }

    private fun startTracking() {
        try {
            Log.d(TAG, "Attempting to start cycling tracking...")

            if (checkLocationPermission()) {
                Log.d(TAG, "Location permission granted")

                if (checkGPSEnabled()) {
                    Log.d(TAG, "GPS is enabled, starting tracking...")

                    isTracking = true
                    startTime = System.currentTimeMillis()

                    // Start chronometer
                    chronometer.base = SystemClock.elapsedRealtime()
                    chronometer.start()

                    // Update button text
                    startStopButton.text = "Stop"

                    // Start location updates
                    startLocationUpdates()

                    Toast.makeText(
                                    this,
                                    "Cycling tracking started! GPS is active.",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    Log.d(TAG, "Started cycling tracking successfully")
                } else {
                    Log.d(TAG, "GPS is disabled, showing dialog")
                    showGPSDialog()
                }
            } else {
                Log.d(TAG, "Location permission not granted, requesting...")
                requestLocationPermission()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking: ${e.message}")
            Toast.makeText(this, "Error starting tracking: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopTracking() {
        try {
            isTracking = false

            // Stop chronometer
            chronometer.stop()

            // Stop location updates
            stopLocationUpdates()

            // Update button text
            startStopButton.text = "Start"

            // Save activity
            saveActivity()

            Log.d(TAG, "Stopped cycling tracking")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking: ${e.message}")
            Toast.makeText(this, "Error stopping tracking: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateLocation(location: Location) {
        try {
            if (previousLocation != null) {
                val distance = previousLocation!!.distanceTo(location)
                totalDistance += distance

                // Calculate speed (m/s to km/h)
                val timeDiff = (location.time - previousLocation!!.time) / 1000.0
                if (timeDiff > 0) {
                    currentSpeed = (distance / timeDiff) * 3.6 // Convert to km/h
                    if (currentSpeed > maxSpeed) {
                        maxSpeed = currentSpeed
                    }
                }

                // Calculate calories (simplified formula)
                val weight = 70.0 // kg - should be user's weight
                val duration = (System.currentTimeMillis() - startTime) / 1000.0 / 3600.0 // hours
                totalCalories = (weight * 8.0 * duration) // MET value for cycling

                // Update UI
                updateDistanceDisplay()
                updateSpeedDisplay()
                updateCaloriesDisplay()
            }

            previousLocation = location
            locationList.add(location)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location: ${e.message}")
        }
    }

    private fun updateDistanceDisplay() {
        try {
            val distanceKm = totalDistance / 1000.0
            distanceText.text = "%.2f km".format(distanceKm)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating distance display: ${e.message}")
        }
    }

    private fun updateSpeedDisplay() {
        try {
            speedText.text = "%.1f km/h".format(currentSpeed)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating speed display: ${e.message}")
        }
    }

    private fun updateCaloriesDisplay() {
        try {
            caloriesText.text = "%.0f cal".format(totalCalories)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating calories display: ${e.message}")
        }
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                            .build()

            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        mainLooper
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates: ${e.message}")
        }
    }

    private fun saveActivity() {
        // Show loading dialog
        val loadingDialog =
                AlertDialog.Builder(this)
                        .setView(R.layout.loading_dialog)
                        .setCancelable(false)
                        .create()
        loadingDialog.show()

        lifecycleScope.launch {
            try {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                val averageSpeed = if (duration > 0) (totalDistance / duration) * 3.6 else 0.0

                val activity =
                        FitnessActivity(
                                userId = userId,
                                activityType = "CYCLING",
                                title =
                                        "Cycling - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}",
                                startTime = startTime,
                                endTime = System.currentTimeMillis(),
                                durationSeconds = duration,
                                distanceMeters = totalDistance,
                                caloriesBurned = totalCalories,
                                averageSpeed = averageSpeed,
                                maxSpeed = maxSpeed,
                                routeData =
                                        locationList.joinToString(",") {
                                            "${it.latitude},${it.longitude}"
                                        },
                                notes = "Cycling session completed"
                        )

                // Network connection ရှိ/မရှိ စစ်ဆေးပြီး save method ကိုခွဲသုံးသည်။
                if (!NetworkUtils.isNetworkAvailable(this@CyclingActivity)) {
                    // အင်တာနက်မရှိလျှင် local database သို့သာသိမ်းဆည်းမည်။
                    fitnessActivityDao.insertActivity(activity)
                    Log.d(
                            TAG,
                            "No internet connection. Saved cycling activity to local database: ${activity.title}"
                    )
                    loadingDialog.dismiss()
                    Toast.makeText(
                                    this@CyclingActivity,
                                    "No internet connection. Activity saved locally.",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    // Home screen သို့ ပြန်ပို့ရန်
                    val intent = Intent(this@CyclingActivity, HomeScreenActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // အင်တာနက်ရှိလျှင် server သို့သိမ်းဆည်းမည်။
                    saveActivityToServer(activity, loadingDialog)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cycling activity: ${e.message}")
                loadingDialog.dismiss()
                Toast.makeText(
                                this@CyclingActivity,
                                "Error saving activity: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }

    private fun saveActivityToServer(activity: FitnessActivity, loadingDialog: AlertDialog) {
        try {
            val apiService = ApiService(this)
            apiService.saveActivity(
                    activity = activity,
                    onSuccess = { message ->
                        Log.d(TAG, "Activity saved to server: $message")
                        loadingDialog.dismiss()

                        // Show success message
                        Toast.makeText(
                                        this@CyclingActivity,
                                        "Activity saved successfully!",
                                        Toast.LENGTH_SHORT
                                )
                                .show()

                        // Navigate back to home screen
                        val intent = Intent(this@CyclingActivity, HomeScreenActivity::class.java)
                        intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    onError = { error ->
                        Log.e(TAG, "Error saving to server: $error")
                        loadingDialog.dismiss()

                        // Show more specific error message
                        val errorMessage =
                                when {
                                    error.contains("Network error") ->
                                            "Network connection failed. Activity saved locally."
                                    error.contains("null") ->
                                            "Server connection failed. Activity saved locally."
                                    error.contains("Duplicate request") ->
                                            "Activity already being saved."
                                    else -> "Server sync failed: $error"
                                }

                        Toast.makeText(this@CyclingActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveActivityToServer: ${e.message}")
            loadingDialog.dismiss()
            Toast.makeText(
                            this@CyclingActivity,
                            "Error saving activity: ${e.message}",
                            Toast.LENGTH_LONG
                    )
                    .show()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun checkGPSEnabled(): Boolean {
        try {
            val locationManager =
                    getSystemService(LOCATION_SERVICE) as android.location.LocationManager
            return locationManager.isProviderEnabled(
                    android.location.LocationManager.GPS_PROVIDER
            ) ||
                    locationManager.isProviderEnabled(
                            android.location.LocationManager.NETWORK_PROVIDER
                    )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking GPS status: ${e.message}")
            return false
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "Location permission granted")
                    if (isTracking) {
                        startLocationUpdates()
                    }
                } else {
                    Log.d(TAG, "Location permission denied")
                    Toast.makeText(
                                    this,
                                    "Location permission required for tracking",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (isTracking) {
                stopLocationUpdates()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
        }
    }
}
