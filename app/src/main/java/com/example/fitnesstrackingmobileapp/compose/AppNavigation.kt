package com.example.fitnesstrackingmobileapp.compose

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomePage(navController, onLogout = { navController.navigate("login") }) }
        composable("cycling") { CyclingScreen(navController) }
        composable("weightlifting") { WeightliftingScreen(navController) }
    }
}
