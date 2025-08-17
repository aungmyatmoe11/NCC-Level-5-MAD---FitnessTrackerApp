package com.example.fitnesstrackingmobileapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import com.example.fitnesstrackingmobileapp.data.FitnessRepository
import com.example.fitnesstrackingmobileapp.data.SyncWorker
import com.example.fitnesstrackingmobileapp.notification.FitnessNotificationService
import com.example.fitnesstrackingmobileapp.utils.BatteryOptimizationUtils
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import com.example.fitnesstrackingmobileapp.utils.TimeBasedGreeting
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.util.*
import kotlinx.coroutines.launch

class HomeScreenActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeScreenActivity"
    }

    private lateinit var fitnessRepository: FitnessRepository
    private lateinit var recentActivitiesAdapter: RecentActivitiesTableAdapter
    private lateinit var notificationService: FitnessNotificationService

    // UI Components
    private lateinit var recentActivitiesRecyclerView: RecyclerView
    private lateinit var emptyRecentActivities: LinearLayout
    private lateinit var viewAllButton: Button
    private lateinit var syncButton: MaterialButton
    private lateinit var profileAvatar: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_home_screen)

            // Initialize repository and services
            fitnessRepository = FitnessRepository(this)
            notificationService = FitnessNotificationService(this)

            // Initialize the main activity cards and functionality
            initializeMainUI()

            // Check battery optimization
            checkBatteryOptimization()

            // Initialize sync and notifications
            initializeBackgroundServices()

            // Register network connectivity callback for auto-sync
            NetworkUtils.registerNetworkCallback(this) {
                Log.d(TAG, "Network available, checking for unsynced data...")
                checkUnsyncedData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
        }
    }

    private fun initializeMainUI() {
        try {
            // Initialize click listeners for the main activity cards
            initializeCardClickListeners()

            // Initialize stats display
            initializeStatsDisplay()

            // Setup recent activities recycler view
            setupRecentActivitiesRecyclerView()

            // Initialize sync button
            syncButton = findViewById(R.id.syncButton)
            syncButton.setOnClickListener { performManualSync() }

            // Initialize data management button
            findViewById<MaterialButton>(R.id.dataManagementButton)?.setOnClickListener {
                showDataManagementDialog()
            }

            // Initialize test notifications button
            findViewById<MaterialButton>(R.id.testNotificationsButton)?.setOnClickListener {
                startActivity(Intent(this, NotificationTestActivity::class.java))
            }

            // Initialize profile dropdown
            initializeProfileDropdown()

            // Initialize Quick Start FAB
            setupQuickStartFAB()

            // Load data
            loadRecentActivities()
            loadWeeklyStats()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing main UI: ${e.message}")
        }
    }

    private fun checkBatteryOptimization() {
        try {
            if (!BatteryOptimizationUtils.isBatteryOptimizationEnabled(this)) {
                Log.d(
                        TAG,
                        "Battery optimization is enabled - requesting disable for better app performance"
                )
                // You can show a dialog here to request battery optimization disable
                // BatteryOptimizationUtils.requestDisableBatteryOptimization(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking battery optimization: ${e.message}")
        }
    }

    private fun initializeBackgroundServices() {
        try {
            // Schedule immediate sync if needed
            SyncWorker.scheduleImmediateSync(this)

            // Check for notifications
            notificationService.checkAndShowGoalAchievements()
            notificationService.checkAndShowMilestones()

            Log.d(TAG, "Background services initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing background services: ${e.message}")
        }
    }

    private fun initializeStatsDisplay() {
        try {
            lifecycleScope.launch {
                try {
                    val userId = UserSession.getUserId(this@HomeScreenActivity)

                    // Check network status
                    val isNetworkAvailable =
                            NetworkUtils.isNetworkAvailable(this@HomeScreenActivity)
                    Log.d(TAG, "Network available for stats: $isNetworkAvailable")

                    val today = Calendar.getInstance()
                    today.set(Calendar.HOUR_OF_DAY, 0)
                    today.set(Calendar.MINUTE, 0)
                    today.set(Calendar.SECOND, 0)
                    today.set(Calendar.MILLISECOND, 0)

                    val todayStart = today.timeInMillis
                    val todayEnd = todayStart + (24 * 60 * 60 * 1000) - 1

                    if (isNetworkAvailable) {
                        // Network available - get data from server
                        Log.d(TAG, "Getting today's stats from server...")
                        val apiService = ApiService(this@HomeScreenActivity)
                        apiService.getUserActivities(
                                userId = userId,
                                onSuccess = { activities ->
                                    // Filter today's activities
                                    val todayActivities =
                                            activities.filter { activity ->
                                                activity.startTime >= todayStart &&
                                                        activity.startTime <= todayEnd
                                            }

                                    // Calculate stats from server data
                                    val totalSteps =
                                            todayActivities.sumOf {
                                                (it.distanceMeters * 1.3).toLong()
                                            }
                                    val totalCalories =
                                            todayActivities.sumOf { it.caloriesBurned.toLong() }

                                    Log.d(
                                            TAG,
                                            "Server stats: $totalSteps steps, $totalCalories calories"
                                    )

                                    // Update UI on main thread
                                    runOnUiThread { updateStatsUI(totalSteps, totalCalories) }
                                },
                                onError = { error ->
                                    Log.e(TAG, "Error getting stats from server: $error")
                                    Log.e(TAG, "User ID: $userId")
                                    // Fallback to local data
                                    lifecycleScope.launch {
                                        loadLocalStats(userId, todayStart, todayEnd)
                                    }
                                }
                        )
                    } else {
                        // No network - use local data
                        Log.d(TAG, "No network, using local stats")
                        loadLocalStats(userId, todayStart, todayEnd)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading today's stats: ${e.message}")
                    // Fallback to default values
                    updateStatsUI(0, 0)
                }
            }

            // Update welcome message with user name
            updateWelcomeMessage()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing stats display: ${e.message}")
        }
    }

    private suspend fun loadLocalStats(userId: String, todayStart: Long, todayEnd: Long) {
        try {
            // Get today's activities using repository
            val todayActivities =
                    fitnessRepository.getActivitiesByDateRange(userId, todayStart, todayEnd)

            // Calculate total steps (approximate based on distance)
            val totalSteps = todayActivities.sumOf { (it.distanceMeters * 1.3).toLong() }
            val totalCalories = todayActivities.sumOf { it.caloriesBurned.toLong() }

            Log.d(TAG, "Local stats: $totalSteps steps, $totalCalories calories")

            // Update UI on main thread
            runOnUiThread { updateStatsUI(totalSteps, totalCalories) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local stats: ${e.message}")
            runOnUiThread { updateStatsUI(0, 0) }
        }
    }

    private fun updateWelcomeMessage() {
        try {
            val userName = UserSession.getUserName(this)
            val userEmail = UserSession.getUserEmail(this)
            val welcomeText = findViewById<TextView>(R.id.welcomeText)

            // Use time-based greeting with user name
            val personalizedGreeting = TimeBasedGreeting.getPersonalizedGreeting(userName)
            welcomeText?.text = personalizedGreeting

            // You can also update the subtitle if needed
            val subtitleText = findViewById<TextView>(R.id.subtitleText)
            // if (userEmail.isNotEmpty()) {
            // subtitleText?.text = "Ready for your workout? ($userEmail)"
            // } else {
            subtitleText?.text = "Ready for your workout?"
            // }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating welcome message: ${e.message}")
        }
    }

    private fun setupRecentActivitiesRecyclerView() {
        try {
            val recyclerView = findViewById<RecyclerView>(R.id.recentActivitiesRecyclerView)
            recentActivitiesAdapter = RecentActivitiesTableAdapter { activity ->
                // Handle activity click - show details
                showActivityDetails(activity)
            }

            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@HomeScreenActivity)
                adapter = recentActivitiesAdapter
            }

            // Setup View All button
            val viewAllButton = findViewById<TextView>(R.id.viewAllButton)
            viewAllButton?.setOnClickListener {
                startActivity(Intent(this, ActivityHistoryActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up recent activities recycler view: ${e.message}")
        }
    }

    private fun initializeCardClickListeners() {
        try {
            // Main Activity Cards
            val runningCard = findViewById<MaterialCardView>(R.id.runningCard)
            val cyclingCard = findViewById<MaterialCardView>(R.id.cyclingCard)
            val weightliftingCard = findViewById<MaterialCardView>(R.id.weightliftingCard)

            // Set click listeners for main activity cards
            runningCard?.setOnClickListener {
                Log.d(TAG, "Running card clicked - Starting running activity with maps")
                startRunningActivity()
            }

            cyclingCard?.setOnClickListener {
                Log.d(TAG, "Cycling card clicked - Starting cycling tracking")
                startActivity(Intent(this, CyclingActivity::class.java))
            }

            weightliftingCard?.setOnClickListener {
                Log.d(TAG, "Weightlifting card clicked - Starting weightlifting activity")
                startActivity(Intent(this, WeightliftingActivity::class.java))
            }

            // Activity History Button
            val viewHistoryButton = findViewById<MaterialButton>(R.id.viewHistoryButton)
            viewHistoryButton?.setOnClickListener {
                Log.d(TAG, "View History button clicked - Opening activity history")
                startActivity(Intent(this, ActivityHistoryActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing card click listeners: ${e.message}")
        }
    }

    private fun startRunningActivity() {
        try {
            // Check if GPS tracking should be enabled based on battery level
            if (BatteryOptimizationUtils.shouldEnableGPSTracking(this)) {
                // Start the new RunningActivity with maps
                val intent = Intent(this, RunningActivity::class.java)
                startActivity(intent)
            } else {
                // Show battery optimization message
                showFeatureComingSoon("GPS tracking is disabled due to low battery")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting running activity: ${e.message}")
            showFeatureComingSoon("Running Tracker")
        }
    }

    private fun initializeProfileDropdown() {
        try {
            profileAvatar = findViewById(R.id.profileAvatar)
            profileAvatar.setOnClickListener { showProfileDropdown(it) }
            Log.d(TAG, "Profile dropdown initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing profile dropdown: ${e.message}")
        }
    }

    private fun showProfileDropdown(anchorView: View) {
        try {
            val popupMenu = PopupMenu(this, anchorView)
            popupMenu.menuInflater.inflate(R.menu.profile_dropdown_menu, popupMenu.menu)

            // Set up menu item click listeners
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_logout -> {
                        showLogoutConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing profile dropdown: ${e.message}")
        }
    }

    private fun showLogoutConfirmationDialog() {
        try {
            AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout") { _, _ -> performLogout() }
                    .setNegativeButton("Cancel", null)
                    .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout confirmation dialog: ${e.message}")
        }
    }

    private fun performLogout() {
        try {
            Log.d(TAG, "Performing logout")

            // Clear user session
            UserSession.clearUserSession(this)

            // Cancel all pending requests
            FitnessTrackerApplication.instance?.cancelAllRequests()

            // Stop background services
            SyncWorker.cancelAllWork(this)

            // Show logout message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Log.d(TAG, "Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecentActivities() {
        lifecycleScope.launch {
            try {
                val userId = UserSession.getUserId(this@HomeScreenActivity)
                if (userId.isNotEmpty()) {
                    Log.d(TAG, "Loading recent activities for user: $userId")

                    // Check network status
                    val isNetworkAvailable =
                            NetworkUtils.isNetworkAvailable(this@HomeScreenActivity)
                    Log.d(TAG, "Network available: $isNetworkAvailable")

                    if (isNetworkAvailable) {
                        // Network available - get data directly from server
                        Log.d(TAG, "Network available, fetching data from server...")
                        try {
                            val apiService = ApiService(this@HomeScreenActivity)
                            apiService.getUserActivities(
                                    userId = userId,
                                    onSuccess = { activities ->
                                        Log.d(
                                                TAG,
                                                "Received!!!! ${activities.size} activities from server"
                                        )

                                        // Debug: Log each server activity
                                        activities.forEach { activity ->
                                            Log.d(
                                                    TAG,
                                                    "Server activity: ${activity.title} (${activity.activityType}) - ID: ${activity.id}"
                                            )
                                        }

                                        // Get the most recent 5 activities
                                        val recentActivities = activities.take(5)
                                        Log.d(
                                                TAG,
                                                "Recent activities to display: ${recentActivities.size}"
                                        )
                                        updateRecentActivitiesUI(recentActivities)
                                    },
                                    onError = { error ->
                                        Log.e(TAG, "Error fetching from server: $error")
                                        Log.e(TAG, "User ID: $userId")
                                        // Show empty state on error
                                        updateRecentActivitiesUI(emptyList())
                                    }
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching from server: ${e.message}")
                            e.printStackTrace()
                            // Show empty state on error
                            updateRecentActivitiesUI(emptyList())
                        }
                    } else {
                        // No network - show local data
                        Log.d(TAG, "No network available, showing local data only")
                        val localActivities = fitnessRepository.getRecentActivities(userId, 5)
                        Log.d(
                                TAG,
                                "Loaded ${localActivities.size} recent activities from local database"
                        )
                        updateRecentActivitiesUI(localActivities)
                    }
                } else {
                    Log.w(TAG, "No user ID available for loading recent activities")
                    updateRecentActivitiesUI(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent activities: ${e.message}")
                e.printStackTrace()
                updateRecentActivitiesUI(emptyList())
            }
        }
    }

    private fun updateRecentActivitiesUI(activities: List<FitnessActivity>) {
        try {
            Log.d(TAG, "updateRecentActivitiesUI called with ${activities.size} activities")
            recentActivitiesAdapter.submitList(activities)

            // Show/hide empty state
            val emptyState = findViewById<View>(R.id.emptyRecentActivities)
            val recyclerView = findViewById<RecyclerView>(R.id.recentActivitiesRecyclerView)

            if (activities.isEmpty()) {
                emptyState?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
                Log.d(TAG, "No recent activities to display - showing empty state")
            } else {
                emptyState?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                Log.d(TAG, "Updated recent activities UI with ${activities.size} activities")
                Log.d(TAG, "RecyclerView visibility: ${recyclerView?.visibility}")
                Log.d(TAG, "Empty state visibility: ${emptyState?.visibility}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating recent activities UI: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadWeeklyStats() {
        lifecycleScope.launch {
            try {
                val userId = UserSession.getUserId(this@HomeScreenActivity)
                if (userId.isNotEmpty()) {
                    Log.d(TAG, "Loading weekly stats for user: $userId")

                    // Check network status
                    val isNetworkAvailable =
                            NetworkUtils.isNetworkAvailable(this@HomeScreenActivity)
                    Log.d(TAG, "Network available for weekly stats: $isNetworkAvailable")

                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - (7 * 24 * 60 * 60 * 1000) // 7 days ago

                    if (isNetworkAvailable) {
                        // Network available - get data from server
                        Log.d(TAG, "Getting weekly stats from server...")
                        val apiService = ApiService(this@HomeScreenActivity)
                        apiService.getUserActivities(
                                userId = userId,
                                onSuccess = { activities ->
                                    // Filter weekly activities
                                    val weeklyActivities =
                                            activities.filter { activity ->
                                                activity.startTime >= startTime &&
                                                        activity.startTime <= endTime
                                            }

                                    // Calculate stats from server data
                                    val totalSteps =
                                            weeklyActivities.sumOf {
                                                (it.distanceMeters * 1.3).toLong()
                                            }
                                    val totalCalories =
                                            weeklyActivities.sumOf { it.caloriesBurned.toLong() }

                                    Log.d(
                                            TAG,
                                            "Server weekly stats: $totalSteps steps, $totalCalories calories"
                                    )

                                    // Update UI on main thread
                                    runOnUiThread { updateStatsUI(totalSteps, totalCalories) }
                                },
                                onError = { error ->
                                    Log.e(TAG, "Error getting weekly stats from server: $error")
                                    Log.e(TAG, "User ID: $userId")
                                    // Fallback to local data
                                    lifecycleScope.launch {
                                        loadLocalWeeklyStats(userId, startTime, endTime)
                                    }
                                }
                        )
                    } else {
                        // No network - use local data
                        Log.d(TAG, "No network, using local weekly stats")
                        loadLocalWeeklyStats(userId, startTime, endTime)
                    }
                } else {
                    Log.w(TAG, "No user ID available for loading weekly stats")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading weekly stats: ${e.message}")
            }
        }
    }

    private suspend fun loadLocalWeeklyStats(userId: String, startTime: Long, endTime: Long) {
        try {
            val activities = fitnessRepository.getActivitiesByDateRange(userId, startTime, endTime)
            Log.d(TAG, "Loaded ${activities.size} activities for weekly stats")

            // Calculate stats
            val totalSteps = activities.sumOf { (it.distanceMeters * 1.3).toLong() }
            val totalCalories = activities.sumOf { it.caloriesBurned.toLong() }

            Log.d(TAG, "Local weekly stats: $totalSteps steps, $totalCalories calories")

            // Update UI on main thread
            runOnUiThread { updateStatsUI(totalSteps, totalCalories) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local weekly stats: ${e.message}")
            runOnUiThread { updateStatsUI(0, 0) }
        }
    }

    private fun updateStatsUI(steps: Long, calories: Long) {
        try {
            findViewById<TextView>(R.id.steps_count)?.text = String.format("%,d", steps)
            findViewById<TextView>(R.id.calories_count)?.text = calories.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stats UI: ${e.message}")
        }
    }

    private fun getWeekStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun showActivityDetails(activity: FitnessActivity) {
        // TODO: Create ActivityDetailsActivity
        Log.d(TAG, "Showing details for activity: ${activity.title}")
        showFeatureComingSoon("Activity Details")
    }

    private fun showFeatureComingSoon(featureName: String) {
        try {
            // You can replace this with a proper dialog or navigation
            Log.d(TAG, "$featureName feature coming soon!")
            // For now, just log the action
            // In a real app, you'd show a dialog or navigate to the feature
        } catch (e: Exception) {
            Log.e(TAG, "Error showing feature coming soon: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            // Refresh data when returning to the app
            loadRecentActivities()
            loadWeeklyStats()

            // Check for unsynced data
            checkUnsyncedData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}")
        }
    }

    private fun checkUnsyncedData() {
        lifecycleScope.launch {
            try {
                val userId = UserSession.getUserId(this@HomeScreenActivity)
                if (userId.isNotEmpty()) {
                    val unsyncedCount = fitnessRepository.getUnsyncedCount(userId)
                    if (unsyncedCount > 0) {
                        Log.d(TAG, "Found $unsyncedCount unsynced activities")
                        // Show toast notification about unsynced data
                        runOnUiThread {
                            Toast.makeText(
                                            this@HomeScreenActivity,
                                            "$unsyncedCount activities pending sync",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }

                        // Auto-sync if network is available
                        if (NetworkUtils.isNetworkAvailable(this@HomeScreenActivity)) {
                            Log.d(TAG, "Network available, auto-syncing data...")
                            fitnessRepository.syncPendingActivities()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking unsynced data: ${e.message}")
            }
        }
    }

    private fun performManualSync() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Manual sync initiated")

                // Show loading toast
                runOnUiThread {
                    Toast.makeText(this@HomeScreenActivity, "Syncing data...", Toast.LENGTH_SHORT)
                            .show()
                }

                // Perform sync
                val result = fitnessRepository.syncPendingActivities()

                result.fold(
                        onSuccess = { syncedCount ->
                            runOnUiThread {
                                if (syncedCount > 0) {
                                    Toast.makeText(
                                                    this@HomeScreenActivity,
                                                    "Successfully synced $syncedCount activities",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()

                                    // Clear local data after successful sync
                                    lifecycleScope.launch {
                                        try {
                                            val result = fitnessRepository.clearAllData()
                                            result.fold(
                                                    onSuccess = {
                                                        Log.d(
                                                                TAG,
                                                                "Local data cleared after successful sync"
                                                        )
                                                        loadRecentActivities() // Refresh UI
                                                    },
                                                    onFailure = { exception ->
                                                        Log.e(
                                                                TAG,
                                                                "Error clearing local data: ${exception.message}"
                                                        )
                                                    }
                                            )
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error clearing local data: ${e.message}")
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                                    this@HomeScreenActivity,
                                                    "No activities to sync",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                }
                            }
                        },
                        onFailure = { exception ->
                            runOnUiThread {
                                Toast.makeText(
                                                this@HomeScreenActivity,
                                                "Sync failed: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in manual sync: ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                                    this@HomeScreenActivity,
                                    "Sync error: ${e.message}",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }
        }
    }

    private fun showDataManagementDialog() {
        val options = arrayOf("Clear All Data", "Sync & Resolve Conflicts", "Cancel")
        AlertDialog.Builder(this)
                .setTitle("Data Management")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> clearAllData()
                        1 -> syncAndResolveConflicts()
                        2 -> {
                            /* Cancel */
                        }
                    }
                }
                .show()
    }

    private fun clearAllData() {
        AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will delete all local data. Are you sure?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            val result = fitnessRepository.clearAllData()
                            result.fold(
                                    onSuccess = {
                                        runOnUiThread {
                                            Toast.makeText(
                                                            this@HomeScreenActivity,
                                                            "All data cleared",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            loadRecentActivities()
                                        }
                                    },
                                    onFailure = { exception ->
                                        runOnUiThread {
                                            Toast.makeText(
                                                            this@HomeScreenActivity,
                                                            "Error clearing data: ${exception.message}",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                    }
                            )
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(
                                                this@HomeScreenActivity,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun syncAndResolveConflicts() {
        lifecycleScope.launch {
            try {
                val userId = UserSession.getUserId(this@HomeScreenActivity)
                if (userId.isNotEmpty()) {
                    val result = fitnessRepository.syncAndResolveConflicts(userId)
                    result.fold(
                            onSuccess = { count ->
                                runOnUiThread {
                                    Toast.makeText(
                                                    this@HomeScreenActivity,
                                                    "Resolved $count conflicts",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    loadRecentActivities()
                                }
                            },
                            onFailure = { exception ->
                                runOnUiThread {
                                    Toast.makeText(
                                                    this@HomeScreenActivity,
                                                    "Error resolving conflicts: ${exception.message}",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                }
                            }
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                                    this@HomeScreenActivity,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }
        }
    }

    /**
     * Setup Quick Start Floating Action Button Provides quick access to start different types of
     * activities
     */
    private fun setupQuickStartFAB() {
        try {
            val quickStartFab = findViewById<FloatingActionButton>(R.id.quickStartFab)

            quickStartFab.setOnClickListener {
                Log.d(TAG, "Quick Start FAB clicked")
                showQuickStartMenu(it)
            }

            Log.d(TAG, "Quick Start FAB initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Quick Start FAB: ${e.message}")
        }
    }

    /** Show Quick Start Menu with activity options */
    private fun showQuickStartMenu(anchorView: View) {
        try {
            val popupMenu = PopupMenu(this, anchorView)
            popupMenu.menuInflater.inflate(R.menu.quick_start_menu, popupMenu.menu)

            // Set up menu item click listeners
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_start_running -> {
                        Log.d(TAG, "Quick start: Running selected")
                        startRunningActivity()
                        true
                    }
                    R.id.menu_start_cycling -> {
                        Log.d(TAG, "Quick start: Cycling selected")
                        startActivity(Intent(this, CyclingActivity::class.java))
                        true
                    }
                    R.id.menu_start_weightlifting -> {
                        Log.d(TAG, "Quick start: Weightlifting selected")
                        startActivity(Intent(this, WeightliftingActivity::class.java))
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
            Log.d(TAG, "Quick start menu displayed")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing quick start menu: ${e.message}")
            Toast.makeText(this, "Error showing menu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
