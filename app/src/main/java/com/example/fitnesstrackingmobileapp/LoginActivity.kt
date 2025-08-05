package com.example.fitnesstrackingmobileapp

import android.annotation.SuppressLint
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

            buttonLogin.setOnClickListener() {
                //   val intent = Intent(applicationContext, LoginActivity::class.java)
                //   startActivity(intent)
                loginUser()
            }

            buttonAddUser.setOnClickListener {
                // Navigate to register screen
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            // buttonAddUser .setOnClickListener { addUser() }

            // in the second button click
            // opening the activity to display all the artist
            // it will give error as we dont have this activity so remove this part for now to run
            // the
            // app
            //        buttonViewUser.setOnClickListener {
            //            val intent = Intent(applicationContext, ViewUsersActivity::class.java)
            //            startActivity(intent)
            //            finish()
            //        }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_LONG).show()
        }
    }

    private fun loginUser() {
        try {
            val usernameText = username.text.toString()
            val passwordText = password.text.toString()

            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(
                                applicationContext,
                                "Username and password cannot be empty",
                                Toast.LENGTH_LONG
                        )
                        .show()
                return
            }

            Log.d(TAG, "Attempting login for user: $usernameText")

            // creating volley string request
            val stringRequest =
                    @SuppressLint("SuspiciousIndentation")
                    object :
                            StringRequest(
                                    Request.Method.POST,
                                    EndPoints.URL_LOGIN_USER,
                                    Response.Listener<String> { response ->
                                        try {
                                            Log.d(TAG, "Login response: $response")

                                            val obj = JSONObject(response)
                                            val error = obj.getBoolean("error")
                                            if (!error) {
                                                // Login successful
                                                val message = obj.getString("message")

                                                Log.d(TAG, "Login successful: $message")

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
                                            }
                                        } catch (e: JSONException) {
                                            Log.e(TAG, "JSON parsing error: ${e.message}")
                                            e.printStackTrace()
                                            Toast.makeText(
                                                            applicationContext,
                                                            "JSON parsing error: ${e.message}",
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                    },
                                    object : Response.ErrorListener {
                                        override fun onErrorResponse(volleyError: VolleyError) {
                                            Log.e(TAG, "Volley error: ${volleyError.message}")

                                            Toast.makeText(
                                                            applicationContext,
                                                            "Network error: ${volleyError.message}",
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()

                                            // For testing purposes, navigate to home screen even on
                                            // error
                                            // Remove this in production
                                            Log.d(
                                                    TAG,
                                                    "Navigating to home screen due to network error (for testing)"
                                            )
                                            navigateToHomeScreen()
                                        }
                                    }
                            ) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params.put("UserName", usernameText)
                            params.put("Password", passwordText)
                            return params
                        }
                    }

            // adding request to queue
            VolleySingleton.instance?.addToRequestQueue(stringRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error in loginUser: ${e.message}")
            Toast.makeText(this, "Error during login: ${e.message}", Toast.LENGTH_LONG).show()
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
            val usernameText = username.text.toString()
            val passwordText = password.text.toString()

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

            val stringRequest =
                    object :
                            StringRequest(
                                    Request.Method.POST,
                                    EndPoints.URL_ADD_USER,
                                    Response.Listener<String> { response ->
                                        try {
                                            val obj = JSONObject(response)
                                            Toast.makeText(
                                                            applicationContext,
                                                            obj.getString("message"),
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        } catch (e: JSONException) {
                                            Log.e(
                                                    TAG,
                                                    "JSON parsing error in addUser: ${e.message}"
                                            )
                                            e.printStackTrace()
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
                                                            volleyError.message,
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                    }
                            ) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params.put("UserName", usernameText)
                            params.put("Password", passwordText)
                            return params
                        }
                    }

            // adding request to queue
            VolleySingleton.instance?.addToRequestQueue(stringRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error in addUser: ${e.message}")
            Toast.makeText(this, "Error adding user: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
