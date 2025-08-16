package com.example.fitnesstrackingmobileapp

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class FitnessTrackerApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "FitnessTrackerApplication"
        @get:Synchronized
        var instance: FitnessTrackerApplication? = null
            get
            private set
    }

    private var requestQueue: RequestQueue? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "FitnessTrackerApplication initialized")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.INFO).build()

    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(applicationContext)
            Log.d(TAG, "Request queue created")
        }
        return requestQueue!!
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        try {
            request.tag = TAG
            getRequestQueue().add(request)
            Log.d(TAG, "Request added to queue: ${request.url}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding request to queue: ${e.message}")
        }
    }

    fun cancelAllRequests() {
        try {
            getRequestQueue().cancelAll(TAG)
            Log.d(TAG, "All requests cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling requests: ${e.message}")
        }
    }
}
