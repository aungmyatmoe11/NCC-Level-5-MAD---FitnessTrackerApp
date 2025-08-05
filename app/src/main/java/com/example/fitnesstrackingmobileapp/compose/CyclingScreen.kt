package com.example.fitnesstrackingmobileapp.compose
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun CyclingScreen(navController: NavController) {
    var isCycling by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf(0) }
    var distance by remember { mutableStateOf(0.0) }
    var speed by remember { mutableStateOf(0.0) }
    var calories by remember { mutableStateOf(0) }

    LaunchedEffect(isCycling) {
        while (isCycling) {
            delay(1000L)
            time += 1
            distance += 0.1
            speed = (distance / time) * 3600
            calories = (time * 5).toInt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5))   // soft pink background
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Cycling Activity",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDA70D6)        // purple text
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Metrics
        CyclingMetricsDisplay(time, distance, speed, calories)

        Spacer(modifier = Modifier.height(32.dp))

        // Start/Stop Button
        Button(
            onClick = { isCycling = !isCycling },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCycling) Color(0xFFB00020)  // error red, adjust if needed
                else Color(0xFFDA70D6),               // purple start
                contentColor   = Color.White
            )
        ) {
            Text(
                text = if (isCycling) "Stop Cycling" else "Start Cycling",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back Button (also purple)
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),
                contentColor   = Color.White
            )
        ) {
            Text(
                text = "Back",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CyclingMetricsDisplay(time: Int, distance: Double, speed: Double, calories: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricItem(label = "Time",     value = "${time}s")
        MetricItem(label = "Distance", value = "${"%.2f".format(distance)} km")
        MetricItem(label = "Speed",    value = "${"%.2f".format(speed)} km/h")
        MetricItem(label = "Calories", value = "${calories} kcal")
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDA70D6)  // purple value text
        )
    }
}
