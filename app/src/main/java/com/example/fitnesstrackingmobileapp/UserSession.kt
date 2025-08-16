package com.example.fitnesstrackingmobileapp

import android.content.Context
import android.content.SharedPreferences

/** Utility class to manage user session data Stores and retrieves user information after login */
object UserSession {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /** Save user login information */
    fun saveUserSession(
            context: Context,
            userId: String,
            userName: String,
            userEmail: String = ""
    ) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
                .apply {
                    putString(KEY_USER_ID, userId)
                    putString(KEY_USER_NAME, userName)
                    putString(KEY_USER_EMAIL, userEmail)
                    putBoolean(KEY_IS_LOGGED_IN, true)
                }
                .apply()
    }

    /** Get current user ID */
    fun getUserId(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_USER_ID, "default_user") ?: "default_user"
    }

    /** Get current user name */
    fun getUserName(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    /** Get current user email */
    fun getUserEmail(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_USER_EMAIL, "EMAIL") ?: "EMAIL"
    }

    /** Check if user is logged in */
    fun isLoggedIn(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /** Clear user session (logout) */
    fun clearSession(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().clear().apply()
    }

    /** Clear user session (logout) - alias for clearSession */
    fun clearUserSession(context: Context) {
        clearSession(context)
    }
}
