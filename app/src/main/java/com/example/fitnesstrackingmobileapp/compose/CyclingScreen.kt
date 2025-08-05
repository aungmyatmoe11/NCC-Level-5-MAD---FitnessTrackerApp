package com.example.fitnesstrackingmobileapp.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitnesstrackingmobileapp.ui.theme.Spacing

@Composable
fun CyclingScreen(navController: NavController) {
    var isCycling by remember { mutableStateOf(false) }
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(Spacing.screenPadding)
                            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
                text = "Cycling Tracker",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
        )

        Text(
                text = "Track your cycling activities",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Start/Stop Button
        Button(
                onClick = { isCycling = !isCycling },
                modifier = Modifier.fillMaxWidth(),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (isCycling) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary,
                                contentColor =
                                        if (isCycling) MaterialTheme.colorScheme.onError
                                        else MaterialTheme.colorScheme.onPrimary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = if (isCycling) "Stop Cycling" else "Start Cycling",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }

        // Input Fields
        OutlinedTextField(
                value = distance,
                onValueChange = { distance = it },
                label = { Text("Distance (km)") },
                modifier = Modifier.fillMaxWidth(),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                shape = MaterialTheme.shapes.small,
                textStyle = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                shape = MaterialTheme.shapes.small,
                textStyle = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
                value = speed,
                onValueChange = { speed = it },
                label = { Text("Average Speed (km/h)") },
                modifier = Modifier.fillMaxWidth(),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                shape = MaterialTheme.shapes.small,
                textStyle = MaterialTheme.typography.bodyLarge
        )

        // Save Button
        Button(
                onClick = { /* Save cycling data */},
                modifier = Modifier.fillMaxWidth(),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = "Save Cycling Session",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }

        // Back Button
        OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth(),
                colors =
                        ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }

        // Display current values if cycling
        if (isCycling) {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                        modifier = Modifier.padding(Spacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                            text = "Current Session",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                            text = "Distance: ${distance.ifEmpty { "0" }} km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                            text = "Duration: ${duration.ifEmpty { "0" }} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                            text = "Speed: ${speed.ifEmpty { "0" }} km/h",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
