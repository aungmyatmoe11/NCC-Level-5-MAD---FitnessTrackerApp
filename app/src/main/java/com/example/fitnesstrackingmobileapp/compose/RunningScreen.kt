package com.example.fitnesstrackingmobileapp.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RunningScreenUtils {
    companion object {
        fun formatTime(seconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            val secs = seconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, secs)
        }

        // Average stride length in meters (adjustable based on user height)
        const val STRIDE_LENGTH: Float = 0.75f

        // Calculate distance based on steps
        fun calculateDistance(steps: Int): Float {
            return (steps * STRIDE_LENGTH) / 1000f // Convert to kilometers
        }

        // Calculate calories burned while running
        fun calculateCalories(seconds: Long, weight: Float = 70f): Int {
            val minutes = seconds / 60f
            val MET = 8f // Assuming moderate running pace
            return (0.0175f * MET * weight * minutes).toInt()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RunningScreen(navController: NavController) {
    var isRunning by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0L) }
    var steps by remember { mutableStateOf(0) }
    var lastStepTime by remember { mutableStateOf(0L) }

    // Calculate distance and calories based on steps
    val distance = RunningScreenUtils.calculateDistance(steps)
    val calories = RunningScreenUtils.calculateCalories(seconds)

    // Step detection simulation
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(50) // Check more frequently for smooth step detection
            val currentTime = System.currentTimeMillis()

            // Simulate natural step patterns with varying intervals
            if (currentTime - lastStepTime > getStepInterval()) {
                steps++
                lastStepTime = currentTime
            }
        }
    }

    // Timer
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            seconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Timer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = RunningScreenUtils.formatTime(seconds),
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() with
                                    slideOutVertically { height -> -height } + fadeOut()
                        }
                    ) { time ->
                        Text(
                            text = time,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedStatCard(
                    title = "Steps",
                    value = steps.toString(),
                    isActive = isRunning
                )
                AnimatedStatCard(
                    title = "Distance",
                    value = String.format("%.2f km", distance),
                    isActive = isRunning
                )
                AnimatedStatCard(
                    title = "Calories",
                    value = "$calories kcal",
                    isActive = isRunning
                )
            }

            // Pace Display
            if (seconds > 0 && distance > 0) {
                val paceMinutes = (seconds.toFloat() / 60f) / distance
                Text(
                    text = "Current Pace: ${String.format("%.2f", paceMinutes)} min/km",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.size(89.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color.Red else Color.Green
                    )
                ) {
                    Text(
                        text = if (isRunning) "Stop" else "Start",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        isRunning = false
                        seconds = 0
                        steps = 0
                    },
                    modifier = Modifier.size(89.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = "Back to Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper function to simulate natural variation in step timing
private fun getStepInterval(): Long {
    // Simulate running cadence of ~160-180 steps per minute
    val baseInterval = 500L // ~121 steps per minute
    return baseInterval + Random.nextLong(-50, 50) // Add some natural variation
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedStatCard(
    title: String,
    value: String,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .scale(
                animateFloatAsState(
                    targetValue = if (isActive) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ).value
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                }
            ) { currentValue ->
                Text(
                    text = currentValue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}