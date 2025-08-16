package com.example.fitnesstrackingmobileapp.utils

import java.util.*

object TimeBasedGreeting {

    /**
     * Get appropriate greeting based on current time
     * @return Greeting string (Good Morning, Good Afternoon, Good Evening, Good Night)
     */
    fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hourOfDay) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            in 21..23 -> "Good Night"
            in 0..4 -> "Good Night"
            else -> "Hello" // Fallback
        }
    }

    /**
     * Get greeting with user name
     * @param userName User's name
     * @return Personalized greeting with time-based greeting
     */
    fun getPersonalizedGreeting(userName: String): String {
        val greeting = getGreeting()
        return "$greeting, $userName!"
    }
}
