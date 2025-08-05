package com.example.fitnesstrackingmobileapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackingmobileapp.data.LocationData
import com.example.fitnesstrackingmobileapp.data.Exercise
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class WalkingTrackerActivity : AppCompatActivity(), OnMapReadyCallback {

    private val pERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    private var isTracking = false // To track if the app is currently tracking
    private var startTime: Long = 0 // Start time of the run
    private var stopTime: Long = 0 // Stop time of the
    private var totalTime: Long = 0
    private var startLocation: Location? = null // Start location of the run
    private var stopLocation: Location? = null // Stop location of the run
    private lateinit var startStopButton: Button
    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var speedTextView: TextView
  //  private lateinit var locationDatabase: NoteDatabase
  //  private lateinit var locationDao: NotesDao
    lateinit var viewModal: ExerciseViewModal
    // Current location is set to India, this will be of no use
    var currentLocation: LatLng = LatLng(20.5, 78.9)
    var nextLocation:LatLng?=null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
    //  locationDatabase = NoteDatabase.getDatabase(this)
    //  locationDao = locationDatabase.getNotesDao()
      startStopButton = findViewById(R.id.startStopButton)
      distanceTextView = findViewById(R.id.dTextView)
      timeTextView = findViewById(R.id.tTextView)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ExerciseViewModal::class.java)
        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["AIzaSyARTcE2U8WMiW3kyOjVIXkCDdr3iOxzWXw"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Initializing Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initializing fused location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Adding functionality to the button
        val btn = findViewById<Button>(R.id.startStopButton)
        btn.setOnClickListener {
            if (isTracking) {
                getLastLocation()
            } else {
                getFirstLocation()
            }
            getFirstLocation()
        }
    }

    // Services such as getLastLocation()
    // will only run once map is ready
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        getFirstLocation()
    }

    // Get current location
    @SuppressLint("MissingPermission")
    private fun getFirstLocation() {
        isTracking = true
        startStopButton.text = "Stop Tracking" // Update button text
        startTime = System.currentTimeMillis()
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        startTime=System.currentTimeMillis()

                            startLocation= Location("starLocation").apply{
                            latitude =currentLocation.latitude
                            longitude=currentLocation.longitude
                        }
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(currentLocation))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        isTracking = false
        startStopButton.text = "Start Tracking" // Update button text

        // Record the stop time
        stopTime = System.currentTimeMillis()
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        stopTime=System.currentTimeMillis()
                        stopLocation= Location("nextLocation").apply{
                            latitude =currentLocation.latitude
                            longitude=currentLocation.longitude
                        }
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(currentLocation))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))
                        var distance =startLocation?.distanceTo(stopLocation!!)
                        Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show()
                        if (distance != null) {
                            saveRunSession(distance)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    // Get current location, if shifted
    // from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            if (mLastLocation != null) {
                currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            }
        }
    }

    // function to check if GPS is on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Check if location permissions are
    // granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            pERMISSION_ID
        )
    }

    // What must happen when permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == pERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
    private fun saveRunSession(distance: Float) {
        // Create a RunSession object
        // val runSession = RunSession(
        //    startTime = startTime
        //       endTime = stopTime,
        var totalDistance = distance.toDouble() // Convert to kilometers if needed
        val distanceInKm = distance / 1000
        //   )

        // Insert the run session into the database
        CoroutineScope(Dispatchers.IO).launch {
            //  val sessionId = runSessionDao.insertRunSession(runSession)

            // Save the start and stop locations with the session ID
            startLocation?.let {
                val startLocationData = LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    timestamp = startTime,
                    //    sessionId = sessionId
                )
                //locationDao.insertLocation(startLocationData)
            }

            stopLocation?.let {
                val stopLocationData = LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    timestamp = stopTime,
                    //   sessionId = sessionId
                )
                stopTime=System.currentTimeMillis()
                val duration =stopTime-startTime
                val title="Walking"
                val metric_one=distance.toDouble()
                val metric_two=duration.toDouble()
                val calburned= metric_one * metric_two /200
                val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                val currentDateAndTime: String = sdf.format(Date())
                viewModal.addNote(Exercise(title, metric_one, metric_two,calburned,currentDateAndTime))

                Toast.makeText(this@WalkingTrackerActivity, "Walking Metric added", Toast.LENGTH_LONG).show()

                updateUI(totalDistance.toFloat(),duration)
                callNextActivity()
            }
        }
    }

    private fun callNextActivity() {
        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI(distance: Float, duration: Long) {
        val distanceInKm = distance / 1000
        distanceTextView.setText(distanceInKm.toString() + "Km")
        val durationInSeconds = duration / 1000
        timeTextView.setText(durationInSeconds.toString()+ "minutes")

        val speed = if (durationInSeconds > 0) distanceInKm / (durationInSeconds / 3600f) else 0.0f
        speedTextView.setText(speed.toString() + "Km per hours")

    }
}