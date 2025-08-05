package com.example.fitnesstrackingmobileapp.compose
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun WeightliftingScreen(navController: NavController) {
    var progress by remember { mutableStateOf(0f) }
    var lifting by remember { mutableStateOf(false) }
    var reps by remember { mutableStateOf(0) }
    var caloriesBurned by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val barbellOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (lifting) -50f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(lifting) {
        while (lifting) {
            progress = 0f
            for (i in 1..10) {
                delay(300)
                progress += 0.1f
            }
            reps++
            caloriesBurned += 0.5f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weightlifting",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFDA70D6) // if you also want the title in purple
        )
        Spacer(modifier = Modifier.height(20.dp))

        Canvas(modifier = Modifier.size(150.dp)) {
            translate(0f, barbellOffset) {
                drawRect(color = Color.Black, size = size)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            trackColor = Color.LightGray,
            color = Color(0xFFDA70D6)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Reps: $reps", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Calories Burned: ${caloriesBurned.roundToInt()} kcal",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Start/Stop Lifting Button
        Button(
            onClick = { lifting = !lifting },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(if (lifting) "Stop Lifting" else "Start Lifting")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Back Button
        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Back")
        }
    }
}

