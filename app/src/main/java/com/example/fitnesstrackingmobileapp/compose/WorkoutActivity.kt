package com.example.fitnesstrackingmobileapp.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent



class WorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
     /*       FitnessTrackingApp {
                Surface(
                    modifier = Modifier.fillMaxSize(), // ✅ Now Modifier is recognized
                    color = MaterialTheme.colorScheme.background
                ) { */
                    AppNavigation() // This is the function to handle navigation
                }
            }


}
