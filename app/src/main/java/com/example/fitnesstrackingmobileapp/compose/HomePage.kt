package com.example.fitnesstrackingmobileapp.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.fitnesstrackingmobileapp.ui.theme.Spacing

@Composable
fun HomePage(navController: NavController, onLogout: () -> Unit) {
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(Spacing.screenPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header
        Text(
                text = "Welcome to Fitness Tracker",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
                text = "Track your workouts and stay healthy",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Activity Buttons
        Button(
                onClick = { navController.navigate("cycling") },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = "Log Cycling Activity",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }

        Button(
                onClick = { navController.navigate("weightlifting") },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = "Log Weightlifting Activity",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }

        Button(
                onClick = { navController.navigate("running") },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = "Log Running Activity",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Return Home Button
        OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors =
                        ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                        ),
                shape = MaterialTheme.shapes.medium
        ) {
            Text(
                    text = "Return Home",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Spacing.buttonPadding)
            )
        }
    }
}
