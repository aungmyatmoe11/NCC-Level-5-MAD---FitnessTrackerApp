package com.example.fitnesstrackingmobileapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Common theme-aware components and utilities */
@Composable
fun PrimaryButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun SecondaryButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun TertiaryButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun OutlinedPrimaryButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun ErrorButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun SuccessButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun WarningButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
            shape = MaterialTheme.shapes.medium
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.buttonPadding)
        )
    }
}

@Composable
fun PrimaryTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        singleLine: Boolean = true
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
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

@Composable
fun InfoCard(title: String, content: String, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth(),
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = modifier.padding(vertical = Spacing.md)
    )
}

@Composable
fun SectionSubtitle(text: String, modifier: Modifier = Modifier) {
    Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier.padding(bottom = Spacing.lg)
    )
}
