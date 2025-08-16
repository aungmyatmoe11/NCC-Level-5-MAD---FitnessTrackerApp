package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.ApiService
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import com.example.fitnesstrackingmobileapp.data.FitnessRepository
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class ActivityHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ActivityHistoryActivity"
    }

    private lateinit var fitnessRepository: FitnessRepository
    private lateinit var apiService: ApiService
    private lateinit var activitiesAdapter: ActivityHistoryAdapter
    private var allActivities = listOf<FitnessActivity>()
    private var filteredActivities = listOf<FitnessActivity>()

    // UI Components
    private lateinit var toolbar: MaterialToolbar
    private lateinit var activityTypeFilter: AutoCompleteTextView
    private lateinit var fromDateInput: TextInputEditText
    private lateinit var toDateInput: TextInputEditText
    private lateinit var applyFilterButton: MaterialButton
    private lateinit var clearFilterButton: MaterialButton
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
    private lateinit var totalActivitiesText: TextView
    private lateinit var totalDistanceText: TextView
    private lateinit var totalCaloriesText: TextView
    private lateinit var totalDurationText: TextView
    private lateinit var avgSpeedText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        try {
            // Initialize repository and API service
            fitnessRepository = FitnessRepository(this)
            apiService = ApiService(this)

            // Initialize UI
            initializeUI()

            // Load activities
            loadActivities()

            // Remove the problematic ViewCompat call that was causing null pointer exception
            // The enableEdgeToEdge() should handle the window insets properly

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(
                            this,
                            "Error initializing activity history: ${e.message}",
                            Toast.LENGTH_LONG
                    )
                    .show()
            finish()
        }
    }

    private fun initializeUI() {
        try {
            // Initialize UI components
            toolbar = findViewById(R.id.toolbar)
            activityTypeFilter = findViewById(R.id.activityTypeFilter)
            fromDateInput = findViewById(R.id.fromDateInput)
            toDateInput = findViewById(R.id.toDateInput)
            applyFilterButton = findViewById(R.id.applyFilterButton)
            clearFilterButton = findViewById(R.id.clearFilterButton)
            activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView)
            emptyStateLayout = findViewById(R.id.emptyStateLayout)
            totalActivitiesText = findViewById(R.id.totalActivitiesText)
            totalDistanceText = findViewById(R.id.totalDistanceText)
            totalCaloriesText = findViewById(R.id.totalCaloriesText)
            totalDurationText = findViewById(R.id.totalDurationText)
            avgSpeedText = findViewById(R.id.avgSpeedText)

            // Setup toolbar with proper navigation
            setupToolbar()

            // Setup activity type filter
            setupActivityTypeFilter()

            // Setup date inputs
            setupDateInputs()

            // Setup filter buttons
            setupFilterButtons()

            // Setup RecyclerView
            setupRecyclerView()

            Log.d(TAG, "UI initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UI: ${e.message}", e)
            Toast.makeText(this, "Error setting up UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        try {
            // Set toolbar as action bar
            setSupportActionBar(toolbar)

            // Enable back button
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            // Set custom navigation click listener
            toolbar.setNavigationOnClickListener {
                // Navigate back to home screen instead of just finishing
                val intent = Intent(this, HomeScreenActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }

            // Set navigation icon color to white for better visibility
            toolbar.setNavigationIconTint(getColor(android.R.color.white))
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar: ${e.message}", e)
        }
    }

    private fun setupActivityTypeFilter() {
        try {
            val activityTypes = arrayOf("All", "RUNNING", "CYCLING", "WEIGHTLIFTING")
            val adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityTypes)
            activityTypeFilter.setAdapter(adapter)
            activityTypeFilter.setText("All", false)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up activity type filter: ${e.message}", e)
        }
    }

    private fun setupDateInputs() {
        try {
            val today = Calendar.getInstance()

            // Set empty dates to show all activities initially
            fromDateInput.setText("")
            toDateInput.setText("")

            // Add date picker functionality
            fromDateInput.setOnClickListener { showDatePicker(fromDateInput, today.timeInMillis) }
            toDateInput.setOnClickListener { showDatePicker(toDateInput, today.timeInMillis) }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up date inputs: ${e.message}", e)
        }
    }

    private fun showDatePicker(input: TextInputEditText, defaultTime: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = defaultTime

        val datePickerDialog =
                android.app.DatePickerDialog(
                        this,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = Calendar.getInstance()
                            selectedDate.set(year, month, dayOfMonth)
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            input.setText(dateFormat.format(selectedDate.time))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
        datePickerDialog.show()
    }

    private fun setupFilterButtons() {
        try {
            applyFilterButton.setOnClickListener { applyFilters() }

            clearFilterButton.setOnClickListener { clearFilters() }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up filter buttons: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            activitiesAdapter =
                    ActivityHistoryAdapter(
                            onViewDetails = { activity ->
                                // TODO: Navigate to activity details
                                Toast.makeText(
                                                this,
                                                "View details for ${activity.title}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            },
                            onShare = { activity -> shareActivity(activity) }
                    )

            activitiesRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ActivityHistoryActivity)
                adapter = activitiesAdapter
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}", e)
        }
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            try {
                val userId = UserSession.getUserId(this@ActivityHistoryActivity)
                Log.d(TAG, "Loading activities for user: $userId")

                // Use the same approach as home screen - call API directly if online
                if (NetworkUtils.isNetworkAvailable(this@ActivityHistoryActivity)) {
                    Log.d(TAG, "Network available - fetching from server")
                    
                    // Call API directly like home screen does
                    apiService.getUserActivities(
                        userId = userId,
                        onSuccess = { activities ->
                            Log.d(TAG, "Received ${activities.size} activities from server for Activity History")
                            allActivities = activities
                            filteredActivities = allActivities
                            updateUI()
                            
                            // Show toast if no activities found
                            if (allActivities.isEmpty()) {
                                Toast.makeText(
                                    this@ActivityHistoryActivity,
                                    "No activities found. Start tracking to see your history!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        onError = { error ->
                            Log.e(TAG, "Error fetching from server: $error")
                            // Fallback to local data
                            lifecycleScope.launch {
                                loadLocalActivities()
                            }
                        }
                    )
                } else {
                    Log.d(TAG, "No network - loading from local database")
                    loadLocalActivities()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading activities: ${e.message}", e)
                Toast.makeText(
                    this@ActivityHistoryActivity,
                    "Error loading activities: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun loadLocalActivities() {
        try {
            val userId = UserSession.getUserId(this@ActivityHistoryActivity)
            allActivities = fitnessRepository.getAllActivitiesForUser(userId)
            filteredActivities = allActivities
            updateUI()
            Log.d(TAG, "Loaded ${allActivities.size} activities from local database")

            // Show toast if no activities found
            if (allActivities.isEmpty()) {
                Toast.makeText(
                    this@ActivityHistoryActivity,
                    "No activities found. Start tracking to see your history!",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local activities: ${e.message}", e)
        }
    }

    private fun applyFilters() {
        try {
            val selectedType = activityTypeFilter.text.toString()
            val fromDate = fromDateInput.text.toString()
            val toDate = toDateInput.text.toString()

            // Apply filters
            filteredActivities =
                    allActivities.filter { activity ->
                        var matches = true

                        // Filter by activity type
                        if (selectedType != "All") {
                            matches = matches && activity.activityType == selectedType
                        }

                        // Filter by date range (only if both dates are provided)
                        if (fromDate.isNotEmpty() && toDate.isNotEmpty()) {
                            try {
                                val dateFormat =
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                val fromDateParsed = dateFormat.parse(fromDate)
                                val toDateParsed = dateFormat.parse(toDate)

                                if (fromDateParsed != null && toDateParsed != null) {
                                    val activityDate = Date(activity.startTime)
                                    matches =
                                            matches &&
                                                    activityDate >= fromDateParsed &&
                                                    activityDate <= toDateParsed
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing dates: ${e.message}")
                            }
                        }
                        // If dates are empty, don't filter by date (show all)

                        matches
                    }

            updateUI()
            Log.d(TAG, "Applied filters: ${filteredActivities.size} activities found")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying filters: ${e.message}", e)
            Toast.makeText(this, "Error applying filters: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFilters() {
        try {
            activityTypeFilter.setText("All", false)
            // Reset date inputs to empty (show all activities)
            fromDateInput.setText("")
            toDateInput.setText("")
            filteredActivities = allActivities
            updateUI()
            Log.d(TAG, "Cleared filters - showing all activities")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing filters: ${e.message}", e)
            Toast.makeText(this, "Error clearing filters: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        try {
            // Update summary stats with more details like home screen
            val totalActivities = filteredActivities.size
            val totalDistance = filteredActivities.sumOf { it.distanceMeters } / 1000.0
            val totalCalories = filteredActivities.sumOf { it.caloriesBurned.toLong() }
            val totalDuration = filteredActivities.sumOf { it.durationSeconds }
            val avgSpeed =
                    if (filteredActivities.isNotEmpty()) {
                        filteredActivities.mapNotNull { it.averageSpeed }.average()
                    } else 0.0

            totalActivitiesText.text = totalActivities.toString()
            totalDistanceText.text = "%.1f km".format(totalDistance)
            totalCaloriesText.text = totalCalories.toString()
            totalDurationText.text = formatDuration(totalDuration)
            avgSpeedText.text = "%.1f km/h".format(avgSpeed)

            // Update RecyclerView
            activitiesAdapter.submitList(filteredActivities)

            // Show/hide empty state
            if (filteredActivities.isEmpty()) {
                emptyStateLayout.visibility = View.VISIBLE
                activitiesRecyclerView.visibility = View.GONE
            } else {
                emptyStateLayout.visibility = View.GONE
                activitiesRecyclerView.visibility = View.VISIBLE
            }

            Log.d(
                    TAG,
                    "UI updated: ${filteredActivities.size} activities, ${"%.1f".format(totalDistance)} km, $totalCalories calories"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI: ${e.message}", e)
        }
    }

    private fun shareActivity(activity: FitnessActivity) {
        try {
            val shareText =
                    """
                ${activity.title}
                Duration: ${formatDuration(activity.durationSeconds)}
                Distance: ${"%.2f".format(activity.distanceKm)} km
                Calories: ${"%.0f".format(activity.caloriesBurned)}
                Average Speed: ${"%.1f".format(activity.averageSpeed)} km/h
                
                Tracked with Fitness Tracker App
                """.trimIndent()

            val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "My Fitness Activity")
                    }

            startActivity(Intent.createChooser(intent, "Share Activity"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing activity: ${e.message}", e)
            Toast.makeText(this, "Error sharing activity: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> "%02d:%02d:%02d".format(hours, minutes, secs)
            else -> "%02d:%02d".format(minutes, secs)
        }
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        // Navigate back to home screen
        val intent = Intent(this, HomeScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
        return true
    }
}
