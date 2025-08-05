package com.example.fitnesstrackingmobileapp.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Modern Fitness App Palette
val PrimaryBlue = Color(0xFF2196F3) // Material Blue
val PrimaryBlueDark = Color(0xFF1976D2) // Darker Blue
val PrimaryBlueLight = Color(0xFF64B5F6) // Lighter Blue

// Secondary Colors
val SecondaryGreen = Color(0xFF4CAF50) // Success Green
val SecondaryOrange = Color(0xFFFF9800) // Warning Orange
val SecondaryRed = Color(0xFFF44336) // Error Red
val SecondaryPurple = Color(0xFF9C27B0) // Purple Accent

// Neutral Colors
val NeutralGray50 = Color(0xFFFAFAFA)
val NeutralGray100 = Color(0xFFF5F5F5)
val NeutralGray200 = Color(0xFFEEEEEE)
val NeutralGray300 = Color(0xFFE0E0E0)
val NeutralGray400 = Color(0xFFBDBDBD)
val NeutralGray500 = Color(0xFF9E9E9E)
val NeutralGray600 = Color(0xFF757575)
val NeutralGray700 = Color(0xFF616161)
val NeutralGray800 = Color(0xFF424242)
val NeutralGray900 = Color(0xFF212121)

// Light Theme Colors
val LightPrimary = PrimaryBlue
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFE3F2FD)
val LightOnPrimaryContainer = Color(0xFF0D47A1)

val LightSecondary = SecondaryGreen
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFE8F5E8)
val LightOnSecondaryContainer = Color(0xFF1B5E20)

val LightTertiary = SecondaryPurple
val LightOnTertiary = Color.White
val LightTertiaryContainer = Color(0xFFF3E5F5)
val LightOnTertiaryContainer = Color(0xFF4A148C)

val LightBackground = Color(0xFFFAFAFA)
val LightOnBackground = Color(0xFF1C1B1F)
val LightSurface = Color.White
val LightOnSurface = Color(0xFF1C1B1F)
val LightSurfaceVariant = Color(0xFFF5F5F5)
val LightOnSurfaceVariant = Color(0xFF424242)

val LightError = SecondaryRed
val LightOnError = Color.White
val LightErrorContainer = Color(0xFFFFEBEE)
val LightOnErrorContainer = Color(0xFFB71C1C)

val LightSuccess = SecondaryGreen
val LightOnSuccess = Color.White
val LightSuccessContainer = Color(0xFFE8F5E8)
val LightOnSuccessContainer = Color(0xFF1B5E20)

val LightWarning = SecondaryOrange
val LightOnWarning = Color.White
val LightWarningContainer = Color(0xFFFFF3E0)
val LightOnWarningContainer = Color(0xFFE65100)

// Dark Theme Colors
val DarkPrimary = PrimaryBlueLight
val DarkOnPrimary = Color.Black
val DarkPrimaryContainer = Color(0xFF0D47A1)
val DarkOnPrimaryContainer = Color(0xFFE3F2FD)

val DarkSecondary = Color(0xFF81C784)
val DarkOnSecondary = Color.Black
val DarkSecondaryContainer = Color(0xFF1B5E20)
val DarkOnSecondaryContainer = Color(0xFFE8F5E8)

val DarkTertiary = Color(0xFFCE93D8)
val DarkOnTertiary = Color.Black
val DarkTertiaryContainer = Color(0xFF4A148C)
val DarkOnTertiaryContainer = Color(0xFFF3E5F5)

val DarkBackground = Color(0xFF121212)
val DarkOnBackground = Color.White
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnSurface = Color.White
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val DarkOnSurfaceVariant = Color(0xFFBDBDBD)

val DarkError = Color(0xFFEF5350)
val DarkOnError = Color.Black
val DarkErrorContainer = Color(0xFFB71C1C)
val DarkOnErrorContainer = Color(0xFFFFEBEE)

val DarkSuccess = Color(0xFF81C784)
val DarkOnSuccess = Color.Black
val DarkSuccessContainer = Color(0xFF1B5E20)
val DarkOnSuccessContainer = Color(0xFFE8F5E8)

val DarkWarning = Color(0xFFFFB74D)
val DarkOnWarning = Color.Black
val DarkWarningContainer = Color(0xFFE65100)
val DarkOnWarningContainer = Color(0xFFFFF3E0)

// Status Colors
val StatusActive = SecondaryGreen
val StatusInactive = NeutralGray500
val StatusPending = SecondaryOrange
val StatusError = SecondaryRed

// Gradient Colors
val GradientStart = PrimaryBlue
val GradientEnd = SecondaryPurple
val GradientStartLight = PrimaryBlueLight
val GradientEndLight = Color(0xFFE1BEE7)

// Card Colors
val CardElevation = Color(0xFF000000)
val CardShadow = Color(0x1A000000)

// Divider Colors
val DividerLight = NeutralGray200
val DividerDark = NeutralGray700

// Overlay Colors
val OverlayLight = Color(0x80000000)
val OverlayDark = Color(0x80FFFFFF)

// Legacy colors for backward compatibility
val Purple80 = DarkPrimary
val PurpleGrey80 = DarkSecondary
val Pink80 = DarkTertiary
val Purple40 = LightPrimary
val PurpleGrey40 = LightSecondary
val Pink40 = LightTertiary
