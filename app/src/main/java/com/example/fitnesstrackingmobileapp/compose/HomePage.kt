package com.example.fitnesstrackingmobileapp.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomePage(navController: NavController, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to the Fitness Tracking App!",
            color = Color(0xFFDA70D6),
            style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Buttons for different activities with navigation
//        Button(
//            onClick = { navController.navigate("running") },
//            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
//        ) {
//            Text("Log Running Activity")
//        }

        Button(
            onClick = { navController.navigate("cycling") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),  // background
                contentColor   = Color.White         // text color
            )
        ) {
            Text("Log Cycling Activity")
        }

        Button(
            onClick = { navController.navigate("weightlifting") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),  // background
                contentColor   = Color.White         // text color
            )
        ) {
            Text("Log Weightlifting Activity")
        }

        Spacer(modifier = Modifier.height(16.dp),)
        Button(onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),  // button background
                contentColor   = Color.White         // text color
            )
        ) {
            Text("Return Home")
        }
    }
}
