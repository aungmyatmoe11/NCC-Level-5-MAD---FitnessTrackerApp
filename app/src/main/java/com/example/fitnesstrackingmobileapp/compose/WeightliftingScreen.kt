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
fun WeightliftingScreen(navController: NavController) {
    var exercise by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0.6f) }

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
                text = "Weightlifting Tracker",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
        )

        Text(
                text = "Track your strength training",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Progress Indicator
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
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                        text = "${(progress * 100).toInt()}% Complete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Input Fields
        OutlinedTextField(
                value = exercise,
                onValueChange = { exercise = it },
                label = { Text("Exercise Name") },
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

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    modifier = Modifier.weight(1f),
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
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f),
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
        }

        OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
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

        // Action Buttons
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Button(
                    onClick = { /* Save exercise */},
                    modifier = Modifier.weight(1f),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                    shape = MaterialTheme.shapes.medium
            ) {
                Text(
                        text = "Save Exercise",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(Spacing.buttonPadding)
                )
            }

            Button(
                    onClick = { /* Start workout */},
                    modifier = Modifier.weight(1f),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                    shape = MaterialTheme.shapes.medium
            ) {
                Text(
                        text = "Start Workout",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(Spacing.buttonPadding)
                )
            }
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

        // Recent Exercises Card
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
                        text = "Recent Exercises",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                        text = "Bench Press: 3 sets × 8 reps @ 80kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                        text = "Squats: 4 sets × 10 reps @ 100kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                        text = "Deadlift: 3 sets × 6 reps @ 120kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
