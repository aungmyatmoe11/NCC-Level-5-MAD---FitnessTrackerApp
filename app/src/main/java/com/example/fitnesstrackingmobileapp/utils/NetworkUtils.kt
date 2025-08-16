package com.example.fitnesstrackingmobileapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    private const val TAG = "NetworkUtils"

    /**
     * Check if device has internet connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    /**
     * Register network connectivity callback
     */
    fun registerNetworkCallback(context: Context, onNetworkAvailable: () -> Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network became available")
                onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network became unavailable")
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Test API endpoint connectivity
     */
    fun testApiConnectivity(url: String): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            val responseCode = connection.responseCode
            Log.d(TAG, "API connectivity test for $url: $responseCode")

            connection.disconnect()
            responseCode in 200..299
        } catch (e: IOException) {
            Log.e(TAG, "API connectivity test failed for $url: ${e.message}")
            false
        }
    }

    /**
     * Get detailed network information for debugging
     */
    fun getNetworkInfo(context: Context): String {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return buildString {
            appendLine("Network Status:")
            appendLine("- Active Network: ${network != null}")
            if (networkCapabilities != null) {
                appendLine(
                        "- Has WiFi: ${networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}"
                )
                appendLine(
                        "- Has Cellular: ${networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}"
                )
                appendLine(
                        "- Has Ethernet: ${networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)}"
                )
                appendLine(
                        "- Has Internet: ${networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}"
                )
                appendLine(
                        "- Has Validated: ${networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}"
                )
            }
        }
    }
}
