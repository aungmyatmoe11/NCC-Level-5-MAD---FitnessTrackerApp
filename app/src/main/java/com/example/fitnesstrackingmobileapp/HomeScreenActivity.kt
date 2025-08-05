package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class HomeScreenActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeScreenActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_home_screen)

            // Initialize the main activity cards and functionality
            initializeMainUI()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            // Fallback to the fragment-based layout if the main layout fails
            setContentView(R.layout.activity_home_screen2)
            initializeFragmentNavigation()
        }
    }

    private fun initializeMainUI() {
        try {
            // Initialize click listeners for the main activity cards
            initializeCardClickListeners()
            
            // Initialize stats display
            initializeStatsDisplay()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing main UI: ${e.message}")
        }
    }

    private fun initializeStatsDisplay() {
        try {
            // You can update these values dynamically based on user data
            val stepsTextView = findViewById<TextView>(R.id.steps_count)
            val caloriesTextView = findViewById<TextView>(R.id.calories_count)
            
            // For now, we'll use placeholder values
            // In a real app, you'd fetch these from a database or sensor
            stepsTextView?.text = "8,432"
            caloriesTextView?.text = "324"
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing stats display: ${e.message}")
        }
    }

    private fun initializeCardClickListeners() {
        try {
            // Quick Action Cards
            val workoutCard = findViewById<MaterialCardView>(R.id.workout)
            val cyclingCard = findViewById<MaterialCardView>(R.id.cycling)

            // Feature Cards
            val exercisesCard = findViewById<MaterialCardView>(R.id.btnExec)
            val tasksCard = findViewById<MaterialCardView>(R.id.btntodoList)
            val settingsCard = findViewById<MaterialCardView>(R.id.settingsButton)
            val locationCard = findViewById<MaterialCardView>(R.id.idLocation)
            val routinesCard = findViewById<MaterialCardView>(R.id.idExRoutine)
            val profileCard = findViewById<MaterialCardView>(R.id.login)

            // Set click listeners for Quick Action cards
            workoutCard?.setOnClickListener {
                Log.d(TAG, "Workout card clicked - Starting workout session")
                // TODO: Navigate to workout activity
                showFeatureComingSoon("Workout Session")
            }

            cyclingCard?.setOnClickListener {
                Log.d(TAG, "Cycling card clicked - Starting cycling tracking")
                // TODO: Navigate to cycling activity
                showFeatureComingSoon("Cycling Tracker")
            }

            // Set click listeners for Feature cards
            exercisesCard?.setOnClickListener {
                Log.d(TAG, "Exercises card clicked")
                try {
                    val intent = Intent(this, ExerciseActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to ExerciseActivity: ${e.message}")
                    showFeatureComingSoon("Exercise Library")
                }
            }

            tasksCard?.setOnClickListener {
                Log.d(TAG, "Tasks card clicked")
                showFeatureComingSoon("Task Management")
            }

            settingsCard?.setOnClickListener {
                Log.d(TAG, "Settings card clicked")
                showFeatureComingSoon("Settings")
            }

            locationCard?.setOnClickListener {
                Log.d(TAG, "Location card clicked")
                try {
                    val intent = Intent(this, WalkingTrackerActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to WalkingTrackerActivity: ${e.message}")
                    showFeatureComingSoon("Location Tracking")
                }
            }

            routinesCard?.setOnClickListener {
                Log.d(TAG, "Routines card clicked")
                showFeatureComingSoon("Workout Routines")
            }

            profileCard?.setOnClickListener {
                Log.d(TAG, "Profile card clicked")
                showFeatureComingSoon("User Profile")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing card click listeners: ${e.message}")
        }
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

    private fun initializeFragmentNavigation() {
        try {
            // Load the default fragment (e.g., HomeFragment)
            loadFragment(HomeFragment())

            // Initialize BottomNavigationView and set up item selection listener
            val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> loadFragment(HomeFragment())
                    R.id.aboutUs -> loadFragment(AboutUsFragment())
                    R.id.userProfile -> loadFragment(UserProfileFragment())
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing fragment navigation: ${e.message}")
        }
    }

    // Helper function to load fragments
    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading fragment: ${e.message}")
        }
    }
}
