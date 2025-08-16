package com.example.fitnesstrackingmobileapp

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import com.example.fitnesstrackingmobileapp.data.WeightliftingSession
import java.util.*
import org.json.JSONObject

class ApiService(private val context: Context) {

        companion object {
                private const val TAG = "ApiService"
                // Use existing EndPoints configuration
                private val SAVE_ACTIVITY_URL = EndPoints.URL_SAVE_ACTIVITY
                private val GET_ACTIVITIES_URL = EndPoints.URL_GET_ACTIVITIES

                // Prevent duplicate API calls
                private val activeRequests = mutableSetOf<String>()
        }

        /** Save fitness activity to server */
        fun saveActivity(
                activity: FitnessActivity,
                exercises: List<WeightliftingSession>? = null,
                onSuccess: (String) -> Unit,
                onError: (String) -> Unit
        ) {
                // Create unique request ID to prevent duplicates
                val requestId = "${activity.userId}_${activity.startTime}_${activity.activityType}"

                synchronized(activeRequests) {
                        if (activeRequests.contains(requestId)) {
                                Log.w(TAG, "Duplicate request detected for: $requestId")
                                onError("Duplicate request detected")
                                return
                        }
                        activeRequests.add(requestId)
                }

                try {
                        val jsonObject =
                                JSONObject().apply {
                                        put("user_id", activity.userId)
                                        put("activity_type", activity.activityType)
                                        put("title", activity.title)
                                        put("description", activity.description)
                                        put("start_time", activity.startTime)
                                        put("end_time", activity.endTime)
                                        put("duration_seconds", activity.durationSeconds)
                                        put("distance_meters", activity.distanceMeters)
                                        put("calories_burned", activity.caloriesBurned)
                                        put("average_heart_rate", activity.averageHeartRate)
                                        put("max_heart_rate", activity.maxHeartRate)
                                        put("average_speed", activity.averageSpeed)
                                        put("max_speed", activity.maxSpeed)
                                        put("elevation_gain", activity.elevationGain)
                                        put("route_data", activity.routeData)
                                        put("notes", activity.notes)
                                }

                        // Add exercises for weightlifting activities
                        if (activity.activityType == "WEIGHTLIFTING" && exercises != null) {
                                Log.d(
                                        TAG,
                                        "Adding ${exercises.size} exercises to weightlifting activity"
                                )
                                val exercisesArray = org.json.JSONArray()
                                exercises.forEach { exercise ->
                                        Log.d(
                                                TAG,
                                                "Exercise: ${exercise.exerciseName} - ${exercise.sets} sets, ${exercise.reps} reps, ${exercise.weightKg}kg"
                                        )
                                        val exerciseObject =
                                                JSONObject().apply {
                                                        put("exercise_name", exercise.exerciseName)
                                                        put("sets", exercise.sets)
                                                        put("reps", exercise.reps)
                                                        put("weight_kg", exercise.weightKg)
                                                        put(
                                                                "rest_time_seconds",
                                                                exercise.restTimeSeconds
                                                        )
                                                }
                                        exercisesArray.put(exerciseObject)
                                }
                                jsonObject.put("exercises", exercisesArray)
                                Log.d(TAG, "Exercises JSON: ${exercisesArray.toString()}")
                                Log.d(TAG, "Full JSON object: ${jsonObject.toString()}")
                        }

                        Log.d(TAG, "Sending activity data: ${jsonObject.toString()}")
                        val request =
                                JsonObjectRequest(
                                        Request.Method.POST,
                                        SAVE_ACTIVITY_URL,
                                        jsonObject,
                                        { response ->
                                                try {
                                                        Log.d(
                                                                TAG,
                                                                "Save activity response: $response"
                                                        )
                                                        val error = response.getBoolean("error")
                                                        val message = response.getString("message")

                                                        if (!error) {
                                                                Log.d(
                                                                        TAG,
                                                                        "Activity saved successfully: $message"
                                                                )
                                                                synchronized(activeRequests) {
                                                                        activeRequests.remove(
                                                                                requestId
                                                                        )
                                                                }
                                                                onSuccess(message)
                                                        } else {
                                                                Log.e(
                                                                        TAG,
                                                                        "Error saving activity: $message"
                                                                )
                                                                synchronized(activeRequests) {
                                                                        activeRequests.remove(
                                                                                requestId
                                                                        )
                                                                }
                                                                onError(message)
                                                        }
                                                } catch (e: Exception) {
                                                        Log.e(
                                                                TAG,
                                                                "Error parsing response: ${e.message}"
                                                        )
                                                        Log.e(TAG, "Raw response: $response")
                                                        onError("Error parsing server response")
                                                }
                                        },
                                        { error ->
                                                Log.e(TAG, "Network error: ${error.message}")
                                                Log.e(
                                                        TAG,
                                                        "Network error details: ${error.networkResponse?.statusCode}"
                                                )
                                                synchronized(activeRequests) {
                                                        activeRequests.remove(requestId)
                                                }
                                                onError("Network error: ${error.message}")
                                        }
                                )

                        FitnessTrackerApplication.instance?.addToRequestQueue(request)
                } catch (e: Exception) {
                        Log.e(TAG, "Error creating request: ${e.message}")
                        synchronized(activeRequests) { activeRequests.remove(requestId) }
                        onError("Error creating request: ${e.message}")
                }
        }

        /** Get user activities from server */
        fun getUserActivities(
                userId: String,
                activityType: String? = null,
                limit: Int = 50,
                onSuccess: (List<FitnessActivity>) -> Unit,
                onError: (String) -> Unit
        ) {
                try {
                        val url =
                                StringBuilder(GET_ACTIVITIES_URL)
                                        .append("&user_id=")
                                        .append(userId)
                                        .append("&limit=")
                                        .append(limit)

                        if (activityType != null) {
                                url.append("&activity_type=").append(activityType)
                        }

                        val request =
                                StringRequest(
                                        Request.Method.GET,
                                        url.toString(),
                                        { response ->
                                                try {
                                                        Log.d(
                                                                TAG,
                                                                "Get activities response: $response"
                                                        )
                                                        val jsonResponse = JSONObject(response)

                                                        // Debug: Log the full JSON structure
                                                        Log.d(
                                                                TAG,
                                                                "Full JSON response: ${jsonResponse.toString()}"
                                                        )

                                                        val success =
                                                                jsonResponse.getBoolean("success")
                                                        val message =
                                                                jsonResponse.getString("message")

                                                        Log.d(
                                                                TAG,
                                                                "Success: $success, Message: $message"
                                                        )

                                                        if (success) {
                                                                Log.d(
                                                                        TAG,
                                                                        "Success is true, parsing data..."
                                                                )
                                                                val dataObject =
                                                                        jsonResponse.getJSONObject(
                                                                                "data"
                                                                        )
                                                                Log.d(
                                                                        TAG,
                                                                        "Data object: ${dataObject.toString()}"
                                                                )

                                                                val activitiesArray =
                                                                        dataObject.getJSONArray(
                                                                                "activities"
                                                                        )
                                                                Log.d(
                                                                        TAG,
                                                                        "Activities array length: ${activitiesArray.length()}"
                                                                )
                                                                val activities =
                                                                        mutableListOf<
                                                                                FitnessActivity>()

                                                                for (i in
                                                                        0 until
                                                                                activitiesArray
                                                                                        .length()) {
                                                                        val activityObject =
                                                                                activitiesArray
                                                                                        .getJSONObject(
                                                                                                i
                                                                                        )
                                                                        Log.d(
                                                                                TAG,
                                                                                "Parsing activity $i: ${activityObject.toString()}"
                                                                        )
                                                                        try {
                                                                                val activity =
                                                                                        FitnessActivity(
                                                                                                id =
                                                                                                        activityObject
                                                                                                                .getLong(
                                                                                                                        "id"
                                                                                                                ),
                                                                                                userId =
                                                                                                        activityObject
                                                                                                                .getString(
                                                                                                                        "user_id"
                                                                                                                ),
                                                                                                activityType =
                                                                                                        activityObject
                                                                                                                .getString(
                                                                                                                        "activity_type"
                                                                                                                ),
                                                                                                title =
                                                                                                        activityObject
                                                                                                                .getString(
                                                                                                                        "title"
                                                                                                                ),
                                                                                                description =
                                                                                                        activityObject
                                                                                                                .optString(
                                                                                                                        "description",
                                                                                                                        ""
                                                                                                                ),
                                                                                                startTime =
                                                                                                        activityObject
                                                                                                                .getLong(
                                                                                                                        "start_time"
                                                                                                                ),
                                                                                                endTime =
                                                                                                        activityObject
                                                                                                                .getLong(
                                                                                                                        "end_time"
                                                                                                                ),
                                                                                                durationSeconds =
                                                                                                        activityObject
                                                                                                                .getLong(
                                                                                                                        "duration_seconds"
                                                                                                                ),
                                                                                                distanceMeters =
                                                                                                        activityObject
                                                                                                                .getDouble(
                                                                                                                        "distance_meters"
                                                                                                                ),
                                                                                                caloriesBurned =
                                                                                                        activityObject
                                                                                                                .getDouble(
                                                                                                                        "calories_burned"
                                                                                                                ),
                                                                                                averageHeartRate =
                                                                                                        activityObject
                                                                                                                .optInt(
                                                                                                                        "average_heart_rate",
                                                                                                                        0
                                                                                                                ),
                                                                                                maxHeartRate =
                                                                                                        activityObject
                                                                                                                .optInt(
                                                                                                                        "max_heart_rate",
                                                                                                                        0
                                                                                                                ),
                                                                                                averageSpeed =
                                                                                                        activityObject
                                                                                                                .optDouble(
                                                                                                                        "average_speed",
                                                                                                                        0.0
                                                                                                                ),
                                                                                                maxSpeed =
                                                                                                        activityObject
                                                                                                                .optDouble(
                                                                                                                        "max_speed",
                                                                                                                        0.0
                                                                                                                ),
                                                                                                elevationGain =
                                                                                                        activityObject
                                                                                                                .optDouble(
                                                                                                                        "elevation_gain",
                                                                                                                        0.0
                                                                                                                ),
                                                                                                routeData =
                                                                                                        activityObject
                                                                                                                .optString(
                                                                                                                        "route_data",
                                                                                                                        ""
                                                                                                                ),
                                                                                                notes =
                                                                                                        activityObject
                                                                                                                .optString(
                                                                                                                        "notes",
                                                                                                                        ""
                                                                                                                ),
                                                                                                createdAt =
                                                                                                        activityObject
                                                                                                                .optLong(
                                                                                                                        "created_at",
                                                                                                                        System.currentTimeMillis()
                                                                                                                ),
                                                                                                updatedAt =
                                                                                                        activityObject
                                                                                                                .optLong(
                                                                                                                        "updated_at",
                                                                                                                        System.currentTimeMillis()
                                                                                                                )
                                                                                        )
                                                                                activities.add(
                                                                                        activity
                                                                                )
                                                                                Log.d(
                                                                                        TAG,
                                                                                        "Added activity: ${activity.title} (${activity.activityType}) - Created: ${activity.createdAt}"
                                                                                )
                                                                        } catch (e: Exception) {
                                                                                Log.e(
                                                                                        TAG,
                                                                                        "Error parsing activity $i: ${e.message}"
                                                                                )
                                                                                Log.e(
                                                                                        TAG,
                                                                                        "Activity object: ${activityObject.toString()}"
                                                                                )
                                                                                // Continue with
                                                                                // next activity
                                                                                // instead of
                                                                                // failing
                                                                                // completely
                                                                        }
                                                                }

                                                                Log.d(
                                                                        TAG,
                                                                        "Retrieved ${activities.size} activities"
                                                                )
                                                                onSuccess(activities)
                                                        } else {
                                                                Log.e(
                                                                        TAG,
                                                                        "Error retrieving activities: $message"
                                                                )
                                                                onError(message)
                                                        }
                                                } catch (e: Exception) {
                                                        Log.e(
                                                                TAG,
                                                                "Error parsing response: ${e.message}"
                                                        )
                                                        Log.e(TAG, "Raw response: $response")
                                                        Log.e(
                                                                TAG,
                                                                "Response length: ${response.length}"
                                                        )
                                                        onError(
                                                                "Error parsing server response: ${e.message}"
                                                        )
                                                }
                                        },
                                        { error ->
                                                Log.e(TAG, "Network error: ${error.message}")
                                                onError("Network error: ${error.message}")
                                        }
                                )

                        FitnessTrackerApplication.instance?.addToRequestQueue(request)
                } catch (e: Exception) {
                        Log.e(TAG, "Error creating request: ${e.message}")
                        onError("Error creating request: ${e.message}")
                }
        }
}
