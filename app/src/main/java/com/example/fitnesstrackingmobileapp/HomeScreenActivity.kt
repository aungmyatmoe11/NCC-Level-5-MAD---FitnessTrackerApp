package com.example.fitnesstrackingmobileapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen2)

        // Load the default fragment (e.g., HomeFragment)
        loadFragment(HomeFragment())

        // Initialize BottomNavigationView and set up item selection listener
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.aboutUs -> loadFragment(AboutUsFragment())
                R.id.userProfile -> loadFragment(UserProfileFragment())
             //   R.id.login -> loadFragment(LoginActivity())
            }
            true
        }
    }

    // Helper function to load fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}