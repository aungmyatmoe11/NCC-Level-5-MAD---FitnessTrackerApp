package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    // edittext and spinner
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var buttonAddUser: Button
    lateinit var buttonViewUser: Button
    lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_login)

            // getting it from xml
            username = findViewById(R.id.username)
            password = findViewById(R.id.password)

            // adding a click listener to button
            buttonAddUser = findViewById(R.id.buttonAddUser)
            // buttonViewUser = findViewById(R.id.buttonViewUsers)
            buttonLogin = findViewById(R.id.buttonLogin)

            buttonLogin.setOnClickListener {
                Log.d(TAG, "Login button clicked")
                loginUser()
            }

            buttonAddUser.setOnClickListener {
                Log.d(TAG, "Register button clicked")
                // Navigate to register screen
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            Log.d(TAG, "LoginActivity initialized successfully")

            // Debug network status
            debugNetworkStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_LONG).show()
        }
    }

    private fun debugNetworkStatus() {
        try {
            Log.d(TAG, "=== NETWORK DEBUG INFO ===")
            Log.d(TAG, NetworkUtils.getNetworkInfo(this))
            Log.d(TAG, "Network Available: ${NetworkUtils.isNetworkAvailable(this)}")
            Log.d(TAG, "API URL: ${EndPoints.URL_LOGIN_USER}")
            Log.d(
                    TAG,
                    "API Connectivity Test: ${NetworkUtils.testApiConnectivity(EndPoints.URL_LOGIN_USER)}"
            )
            Log.d(TAG, "=== END NETWORK DEBUG ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error in debugNetworkStatus: ${e.message}")
        }
    }

    private fun loginUser() {
        try {
            val usernameText = username.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(
                                applicationContext,
                                "Username and password cannot be empty",
                                Toast.LENGTH_LONG
                        )
                        .show()
                return
            }

            // Check network connectivity first
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(
                                applicationContext,
                                "No internet connection. Please check your network settings.",
                                Toast.LENGTH_LONG
                        )
                        .show()
                return
            }

            Log.d(TAG, "Attempting login for user: $usernameText")
            Log.d(TAG, "API URL: ${EndPoints.URL_LOGIN_USER}")

            // Show loading indicator
            buttonLogin.isEnabled = false
            buttonLogin.text = "Signing In..."

            // creating volley string request
            val stringRequest =
                    object :
                            StringRequest(
                                    Request.Method.POST,
                                    EndPoints.URL_LOGIN_USER,
                                    Response.Listener<String> { response ->
                                        try {
                                            Log.d(TAG, "Login response received: $response")

                                            val obj = JSONObject(response)
                                            val error = obj.getBoolean("error")

                                            if (!error) {
                                                // Login successful
                                                val message = obj.getString("message")
                                                Log.d(TAG, "Login successful: $message")

                                                // Extract user information from response
                                                try {
                                                    val userObject = obj.getJSONObject("user")
                                                    val userId = userObject.getString("id")
                                                    val userName = userObject.getString("name")
                                                    val userEmail = userObject.getString("email")

                                                    Log.d(
                                                            TAG,
                                                            "User ID: $userId, Name: $userName, Email: $userEmail"
                                                    )

                                                    // Save user session with actual user ID from
                                                    // API
                                                    UserSession.saveUserSession(
                                                            applicationContext,
                                                            userId, // Use actual user ID from API
                                                            userName, // Use actual user name from
                                                            // API
                                                            userEmail // Use actual user email from
                                                            // API
                                                            )
                                                } catch (e: Exception) {
                                                    Log.e(
                                                            TAG,
                                                            "Error parsing user data: ${e.message}"
                                                    )
                                                    // Fallback to username if user object parsing
                                                    // fails
                                                    UserSession.saveUserSession(
                                                            applicationContext,
                                                            usernameText, // Fallback to username
                                                            usernameText, // Fallback to username
                                                            "" // No email available
                                                    )
                                                }

                                                Toast.makeText(
                                                                applicationContext,
                                                                message,
                                                                Toast.LENGTH_LONG
                                                        )
                                                        .show()

                                                // Proceed to the next activity or update UI
                                                navigateToHomeScreen()
                                            } else {
                                                // Login failed
                                                val message = obj.getString("message")
                                                Log.w(TAG, "Login failed: $message")
                                                Toast.makeText(
                                                                applicationContext,
                                                                message,
                                                                Toast.LENGTH_LONG
                                                        )
                                                        .show()

                                                // Re-enable login button
                                                buttonLogin.isEnabled = true
                                                buttonLogin.text = "Sign In"
                                            }
                                        } catch (e: JSONException) {
                                            Log.e(TAG, "JSON parsing error: ${e.message}")
                                            e.printStackTrace()
                                            Toast.makeText(
                                                            applicationContext,
                                                            "Invalid server response. Please try again.",
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()

                                            // Re-enable login button
                                            buttonLogin.isEnabled = true
                                            buttonLogin.text = "Sign In"
                                        }
                                    },
                                    object : Response.ErrorListener {
                                        override fun onErrorResponse(volleyError: VolleyError) {
                                            Log.e(TAG, "Volley error: ${volleyError.message}")
                                            Log.e(
                                                    TAG,
                                                    "Network error details: ${volleyError.networkResponse?.statusCode}"
                                            )
                                            Log.e(
                                                    TAG,
                                                    "Network error data: ${String(volleyError.networkResponse?.data ?: ByteArray(0))}"
                                            )

                                            val errorMessage =
                                                    when {
                                                        volleyError.networkResponse?.statusCode ==
                                                                404 ->
                                                                "Server not found. Please check your connection."
                                                        volleyError.networkResponse?.statusCode ==
                                                                500 ->
                                                                "Server error. Please try again later."
                                                        volleyError.message?.contains("timeout") ==
                                                                true ->
                                                                "Request timeout. Please check your connection."
                                                        else ->
                                                                "Network error: ${volleyError.message ?: "Unknown error"}"
                                                    }

                                            Toast.makeText(
                                                            applicationContext,
                                                            errorMessage,
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()

                                            // Re-enable login button
                                            buttonLogin.isEnabled = true
                                            buttonLogin.text = "Sign In"
                                        }
                                    }
                            ) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params["UserName"] = usernameText
                            params["Password"] = passwordText
                            Log.d(TAG, "Request parameters: $params")
                            return params
                        }

                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-Type"] = "application/x-www-form-urlencoded"
                            headers["Accept"] = "application/json"
                            Log.d(TAG, "Request headers: $headers")
                            return headers
                        }
                    }

            // Set timeout for the request
            stringRequest.retryPolicy =
                    com.android.volley.DefaultRetryPolicy(
                            10000, // 10 seconds timeout
                            1, // 1 retry
                            1.0f // No backoff multiplier
                    )

            // adding request to queue
            FitnessTrackerApplication.instance?.addToRequestQueue(stringRequest)
            Log.d(TAG, "Login request sent to queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error in loginUser: ${e.message}")
            Toast.makeText(this, "Error during login: ${e.message}", Toast.LENGTH_LONG).show()

            // Re-enable login button
            buttonLogin.isEnabled = true
            buttonLogin.text = "Sign In"
        }
    }

    private fun navigateToHomeScreen() {
        try {
            Log.d(TAG, "Navigating to HomeScreenActivity")
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to home screen: ${e.message}")
            Toast.makeText(this, "Error navigating to home screen", Toast.LENGTH_LONG).show()
        }
    }

    // adding a new record to database
    private fun addUser() {
        try {
            // getting the record values
            val usernameText = username.text.toString().trim()
            val passwordText = password.text.toString().trim()

            // creating volley string request
            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(
                                applicationContext,
                                "Username and password cannot be empty",
                                Toast.LENGTH_LONG
                        )
                        .show()
                return
            }

            Log.d(TAG, "Attempting to add user: $usernameText")
            Log.d(TAG, "API URL: ${EndPoints.URL_ADD_USER}")

            val stringRequest =
                    object :
                            StringRequest(
                                    Request.Method.POST,
                                    EndPoints.URL_ADD_USER,
                                    Response.Listener<String> { response ->
                                        try {
                                            Log.d(TAG, "Add user response: $response")
                                            val obj = JSONObject(response)
                                            val message = obj.getString("message")
                                            Toast.makeText(
                                                            applicationContext,
                                                            message,
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        } catch (e: JSONException) {
                                            Log.e(
                                                    TAG,
                                                    "JSON parsing error in addUser: ${e.message}"
                                            )
                                            e.printStackTrace()
                                            Toast.makeText(
                                                            applicationContext,
                                                            "Invalid server response",
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                    },
                                    object : Response.ErrorListener {
                                        override fun onErrorResponse(volleyError: VolleyError) {
                                            Log.e(
                                                    TAG,
                                                    "Volley error in addUser: ${volleyError.message}"
                                            )
                                            Toast.makeText(
                                                            applicationContext,
                                                            "Network error: ${volleyError.message}",
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                    }
                            ) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params["UserName"] = usernameText
                            params["Password"] = passwordText
                            Log.d(TAG, "Add user parameters: $params")
                            return params
                        }

                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-Type"] = "application/x-www-form-urlencoded"
                            headers["Accept"] = "application/json"
                            return headers
                        }
                    }

            // Set timeout for the request
            stringRequest.retryPolicy =
                    com.android.volley.DefaultRetryPolicy(
                            10000, // 10 seconds timeout
                            1, // 1 retry
                            1.0f // No backoff multiplier
                    )

            // adding request to queue
            FitnessTrackerApplication.instance?.addToRequestQueue(stringRequest)
            Log.d(TAG, "Add user request sent to queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error in addUser: ${e.message}")
            Toast.makeText(this, "Error adding user: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
