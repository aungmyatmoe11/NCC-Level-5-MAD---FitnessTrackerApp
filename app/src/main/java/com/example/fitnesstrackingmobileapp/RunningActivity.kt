package com.example.fitnesstrackingmobileapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.example.fitnesstrackingmobileapp.data.FitnessRepository
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class RunningActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "RunningActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val UPDATE_INTERVAL = 1000L // 1 second
        private const val FASTEST_INTERVAL = 500L // 500ms
    }

    private lateinit var fitnessActivityDao: FitnessActivityDao
    private lateinit var fitnessRepository: FitnessRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var googleMap: GoogleMap? = null

    // UI Components
    private lateinit var chronometer: Chronometer
    private lateinit var distanceText: TextView
    private lateinit var timeText: TextView
    private lateinit var startStopButton: Button
    private lateinit var currentLocButton:
            com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var networkStatusMenuItem: MenuItem

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
    private val polylineOptions = PolylineOptions()

    // User ID (retrieved from login session)
    private var userId: String = "default_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)

        try {
            // Initialize database
            fitnessActivityDao = FitnessDatabase.getDatabase(this).fitnessActivityDao()
            fitnessRepository = FitnessRepository(this)

            // Get user ID from session
            userId = UserSession.getUserId(this)

            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Initialize UI
            initializeUI()

            // Setup map
            setupMap()

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

    // Network status functionality
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.running_toolbar_menu, menu)
        networkStatusMenuItem = menu.findItem(R.id.network_status)
        updateNetworkStatus()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun updateNetworkStatus() {
        if (::networkStatusMenuItem.isInitialized) {
            val isOnline = NetworkUtils.isNetworkAvailable(this)
            val iconRes =
                    if (isOnline) R.drawable.baseline_wifi_24 else R.drawable.baseline_wifi_off_24
            networkStatusMenuItem.setIcon(iconRes)

            Log.d(TAG, "Network status updated: ${if (isOnline) "Online" else "Offline"}")
        }
    }

    private fun initializeUI() {
        try {
            Log.d(TAG, "Initializing UI components...")

            // Initialize UI components
            chronometer = findViewById(R.id.chronometer)
            Log.d(TAG, "Chronometer found: ${chronometer != null}")

            distanceText = findViewById(R.id.dTextView)
            Log.d(TAG, "Distance text found: ${distanceText != null}")

            timeText = findViewById(R.id.tTextView)
            Log.d(TAG, "Time text found: ${timeText != null}")

            startStopButton = findViewById(R.id.startStopButton)
            Log.d(TAG, "Start/Stop button found: ${startStopButton != null}")

            currentLocButton = findViewById(R.id.currentLoc)
            Log.d(TAG, "Current location button found: ${currentLocButton != null}")

            toolbar = findViewById(R.id.toolbar)
            Log.d(TAG, "Toolbar found: ${toolbar != null}")

            // Setup toolbar with proper navigation
            toolbar?.setNavigationOnClickListener {
                Log.d(TAG, "Navigation clicked - finishing activity")
                finish()
            }

            // Setup start/stop button with debug logging
            startStopButton?.setOnClickListener {
                Log.d(TAG, "Start/Stop button clicked. Current tracking state: $isTracking")
                try {
                    if (isTracking) {
                        Log.d(TAG, "Stopping tracking...")
                        stopTracking()
                    } else {
                        Log.d(TAG, "Starting tracking...")
                        startTracking()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in button click: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Setup current location button
            currentLocButton?.setOnClickListener {
                Log.d(TAG, "Current location button clicked")
                moveToCurrentLocation()
            }

            // Initialize text displays
            updateDistanceDisplay()
            updateTimeDisplay()

            Log.d(TAG, "UI initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UI: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error setting up UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupMap() {
        try {
            val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map: ${e.message}")
        }
    }

    private fun setupLocationCallback() {
        locationCallback =
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location -> updateLocation(location) }
                        // Update network status periodically during tracking
                        if (isTracking) {
                            updateNetworkStatus()
                        }
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

    override fun onMapReady(map: GoogleMap) {
        try {
            googleMap = map

            // Enable my location button
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap?.isMyLocationEnabled = true

                // Move to current location automatically
                moveToCurrentLocation()
            } else {
                // Request permission if not granted
                requestLocationPermission()
            }

            // Set initial camera position (you can set this to a default location)
            val defaultLocation = LatLng(16.8661, 96.1951) // Yangon, Myanmar
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMapReady: ${e.message}")
        }
    }

    private fun startTracking() {
        try {
            Log.d(TAG, "Attempting to start tracking...")

            if (checkLocationPermission()) {
                Log.d(TAG, "Location permission granted")

                if (checkGPSEnabled()) {
                    Log.d(TAG, "GPS is enabled, starting tracking...")

                    isTracking = true
                    startTime = System.currentTimeMillis()
                    Log.d(TAG, "Set start time: $startTime")

                    // Start chronometer
                    chronometer.base = SystemClock.elapsedRealtime()
                    chronometer.start()
                    Log.d(TAG, "Chronometer started")

                    // Update button text
                    startStopButton.text = "Stop Tracking"
                    Log.d(TAG, "Button text updated to Stop Tracking")

                    // Start location updates
                    startLocationUpdates()

                    Toast.makeText(this, "Tracking started! GPS is active.", Toast.LENGTH_SHORT)
                            .show()
                    Log.d(TAG, "Started running tracking successfully")
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
            e.printStackTrace()
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
            startStopButton.text = "Start Tracking"

            // Save activity
            saveActivity()

            Log.d(TAG, "Stopped running tracking")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking: ${e.message}")
            Toast.makeText(this, "Error stopping tracking: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateLocation(location: Location) {
        try {
            Log.d(TAG, "Updating location: ${location.latitude}, ${location.longitude}")

            if (previousLocation != null) {
                val distance = previousLocation!!.distanceTo(location)
                totalDistance += distance
                Log.d(TAG, "Distance added: $distance meters, Total: $totalDistance meters")

                // Calculate speed (m/s to km/h)
                val timeDiff = (location.time - previousLocation!!.time) / 1000.0
                if (timeDiff > 0) {
                    currentSpeed = (distance / timeDiff) * 3.6 // Convert to km/h
                    if (currentSpeed > maxSpeed) {
                        maxSpeed = currentSpeed
                    }
                    Log.d(TAG, "Current speed: $currentSpeed km/h, Max speed: $maxSpeed km/h")
                }

                // Calculate calories (simplified formula)
                val weight = 70.0 // kg - should be user's weight
                val duration = (System.currentTimeMillis() - startTime) / 1000.0 / 3600.0 // hours
                totalCalories = (weight * 10.0 * duration) // MET value for running
                Log.d(TAG, "Calories calculated: $totalCalories")

                // Update UI
                updateDistanceDisplay()
                updateTimeDisplay()

                // Update map
                updateMap(location)
            } else {
                Log.d(TAG, "First location update, setting previous location")
            }

            previousLocation = location
            locationList.add(location)
            Log.d(TAG, "Location list size: ${locationList.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateMap(location: Location) {
        try {
            val latLng = LatLng(location.latitude, location.longitude)

            // Add point to polyline
            polylineOptions.add(latLng)

            // Update polyline on map
            googleMap?.clear()
            googleMap?.addPolyline(polylineOptions)

            // Add marker for current location
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Current Location"))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating map: ${e.message}")
        }
    }

    private fun moveToCurrentLocation() {
        try {
            Log.d(TAG, "Moving to current location...")

            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            location?.let {
                                val latLng = LatLng(it.latitude, it.longitude)
                                googleMap?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                )
                                Log.d(
                                        TAG,
                                        "Moved to current location: ${it.latitude}, ${it.longitude}"
                                )
                                Toast.makeText(
                                                this,
                                                "Moved to your current location",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                                    ?: run {
                                        Log.d(TAG, "No last location available")
                                        Toast.makeText(
                                                        this,
                                                        "Location not available. Please enable GPS.",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                    }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error getting last location: ${exception.message}")
                            Toast.makeText(
                                            this,
                                            "Error getting location: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
            } else {
                Log.d(TAG, "Location permission not granted")
                requestLocationPermission()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error moving to current location: ${e.message}")
            Toast.makeText(this, "Error moving to location: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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

    private fun updateTimeDisplay() {
        try {
            val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
            val hours = (elapsedTime / 3600000).toInt()
            val minutes = ((elapsedTime % 3600000) / 60000).toInt()
            val seconds = ((elapsedTime % 60000) / 1000).toInt()
            timeText.text = "%02d:%02d:%02d".format(hours, minutes, seconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time display: ${e.message}")
        }
    }

    private fun startLocationUpdates() {
        try {
            Log.d(TAG, "Starting location updates...")

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
                Log.d(TAG, "Location updates started successfully")
            } else {
                Log.e(TAG, "Location permission not granted for updates")
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates: ${e.message}")
            Toast.makeText(
                            this,
                            "Error starting location updates: ${e.message}",
                            Toast.LENGTH_SHORT
                    )
                    .show()
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
                                activityType = "RUNNING",
                                title =
                                        "Running - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}",
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
                                notes = "Running session completed"
                        )

                Log.d(TAG, "Saving running activity: ${activity.title}")

                if (!NetworkUtils.isNetworkAvailable(this@RunningActivity)) {
                    // Use repository to save with offline-first approach
                    val result = fitnessRepository.saveActivity(activity)

                    result.fold(
                            onSuccess = { localId ->
                                Log.d(TAG, "Activity saved successfully with local ID: $localId")
                                loadingDialog.dismiss()

                                // Show success message
                                Toast.makeText(
                                                this@RunningActivity,
                                                "Activity saved successfully!",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()

                                // Navigate back to home screen
                                val intent =
                                        Intent(this@RunningActivity, HomeScreenActivity::class.java)
                                intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                                Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Error saving activity: ${exception.message}")
                                loadingDialog.dismiss()
                                Toast.makeText(
                                                this@RunningActivity,
                                                "Error saving activity: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                    )
                } else {
                    // အင်တာနက်ရှိလျှင် server သို့သိမ်းဆည်းမည်။
                    saveActivityToServer(activity, loadingDialog)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving running activity: ${e.message}")
                loadingDialog.dismiss()
                Toast.makeText(
                                this@RunningActivity,
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
                                        this@RunningActivity,
                                        "Activity saved successfully!",
                                        Toast.LENGTH_SHORT
                                )
                                .show()

                        // Navigate back to home screen
                        val intent = Intent(this@RunningActivity, HomeScreenActivity::class.java)
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

                        Toast.makeText(this@RunningActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveActivityToServer: ${e.message}")
            loadingDialog.dismiss()
            Toast.makeText(
                            this@RunningActivity,
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
