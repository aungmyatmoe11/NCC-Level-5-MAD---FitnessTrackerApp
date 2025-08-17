package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    // UI Components
    private lateinit var id: EditText
    private lateinit var password: EditText
    private lateinit var buttonAddUser: Button
    private lateinit var buttonLogin: Button
    private lateinit var idLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_login)

            // Initialize UI components
            id = findViewById(R.id.email)
            password = findViewById(R.id.password)
            idLayout = findViewById(R.id.emailLayout)
            passwordLayout = findViewById(R.id.passwordLayout)

            // Initialize buttons
            buttonAddUser = findViewById(R.id.buttonAddUser)
            buttonLogin = findViewById(R.id.buttonLogin)

            // Set up real-time validation
            setupRealTimeValidation()

            // Set up password toggle functionality
            setupPasswordToggle()

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

    /** Real-time validation setup လုပ်ဆောင်ချက် User input ကို real-time စစ်ဆေးပြီး errorြသခြင်း */
    private fun setupRealTimeValidation() {
        // ID validation
        id.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateId(s.toString().trim())
                        clearErrorStyling()
                    }
                }
        )

        // Password validation
        password.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validatePassword(s.toString().trim())
                        clearErrorStyling()
                    }
                }
        )
    }

    /**
     * Password toggle လုပ်ဆောင်ချက်ကို setup လုပ်ခြင်း ပါဝါစ်ဝါဒ်ကိုြသခြင်း/ဖျောက်ထားခြင်း
     * ပြောင်းလဲနိုင်စေရန်
     */
    private fun setupPasswordToggle() {
        // Password toggle icon ကို click လုပ်တဲ့အခါ လုပ်ဆောင်မယ့် function
        passwordLayout.setEndIconOnClickListener { togglePasswordVisibility() }

        // Password field ကို စတင်တဲ့အခါ ဖျောက်ထားပြီးသား အနေအထားဖြင့် စတင်မည်
        password.transformationMethod = PasswordTransformationMethod.getInstance()

        // Initial password toggle icon ကို set လုပ်ခြင်း
        restorePasswordToggleIcon()
    }

    /**
     * Password toggle icon ကို ပြန်လည်ထိန်းထားခြင်း Validation error ဖြစ်နေစဉ် icon
     * ပျောက်မသွားစေရန်
     */
    private fun restorePasswordToggleIcon() {
        // Current password visibility state ကို စစ်ဆေးပြီး သင့်တော်သော icon ကို set လုပ်ခြင်း
        val currentTransformation = password.transformationMethod

        if (currentTransformation is PasswordTransformationMethod) {
            // Password ဖျောက်ထားနေပါက visibility off icon ကို set လုပ်ခြင်း
            passwordLayout.endIconDrawable = getDrawable(R.drawable.baseline_visibility_off_24)
        } else {
            // Password ြသနေပါက visibility icon ကို set လုပ်ခြင်း
            passwordLayout.endIconDrawable = getDrawable(R.drawable.baseline_visibility_24)
        }
    }

    /** Password ကိုြသခြင်း/ဖျောက်ထားခြင်း ပြောင်းလဲခြင်း */
    private fun togglePasswordVisibility() {
        val currentTransformation = password.transformationMethod

        if (currentTransformation is PasswordTransformationMethod) {
            // Password ကိုြသမည် (ဖျောက်ထားခြင်းမှြသခြင်းသို့ ပြောင်းလဲခြင်း)
            password.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            // Password ကို ဖျောက်ထားမည် (ြသခြင်းမှ ဖျောက်ထားခြင်းသို့ ပြောင်းလဲခြင်း)
            password.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        // Password toggle icon ကို ပြန်လည်ထိန်းထားခြင်း
        restorePasswordToggleIcon()

        // Cursor position ကို ထိန်းထားရန်
        val selection = password.selectionEnd
        password.setSelection(selection)
    }

    /** ID validation လုပ်ဆောင်ချက် */
    private fun validateId(idText: String): Boolean {
        return when {
            idText.isEmpty() -> {
                idLayout.error = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(idText).matches() -> {
                idLayout.error = "Please enter a valid email address"
                false
            }
            else -> {
                idLayout.error = null
                true
            }
        }
    }

    /** Password validation လုပ်ဆောင်ချက် */
    private fun validatePassword(passwordText: String): Boolean {
        return when {
            passwordText.isEmpty() -> {
                passwordLayout.error = "Password is required"
                restorePasswordToggleIcon()
                false
            }
            passwordText.length < 6 -> {
                passwordLayout.error = "Password must be at least 6 characters"
                restorePasswordToggleIcon()
                false
            }
            else -> {
                passwordLayout.error = null
                restorePasswordToggleIcon()
                true
            }
        }
    }

    /** Show error with red input styling and Toast message */
    private fun showError(message: String) {
        // Show Toast message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Make input fields red to indicate error
        idLayout.boxStrokeColor = getColor(android.R.color.holo_red_dark)
        passwordLayout.boxStrokeColor = getColor(android.R.color.holo_red_dark)
    }

    /** Clear error styling from input fields */
    private fun clearErrorStyling() {
        // Reset to app's primary color from colors.xml
        idLayout.boxStrokeColor = getColor(R.color.primary)
        passwordLayout.boxStrokeColor = getColor(R.color.primary)
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
            val idText = id.text.toString().trim()
            val passwordText = password.text.toString().trim()

            // Clear any existing error styling
            clearErrorStyling()

            // Validate input fields
            val isIdValid = validateId(idText)
            val isPasswordValid = validatePassword(passwordText)

            if (!isIdValid || !isPasswordValid) {
                showError("Please fix the validation errors above")
                return
            }

            // Check network connectivity first
            if (!NetworkUtils.isNetworkAvailable(this)) {
                showError("No internet connection. Please check your network settings.")
                return
            }

            Log.d(TAG, "Attempting login for email: $idText")
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
                                                    // Fallback to email if user object parsing
                                                    // fails
                                                    UserSession.saveUserSession(
                                                            applicationContext,
                                                            idText, // Fallback to email
                                                            idText, // Fallback to email
                                                            idText // Use email as fallback
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

                                                // Show error with red styling and toast
                                                showError(message)

                                                // Re-enable login button
                                                buttonLogin.isEnabled = true
                                                buttonLogin.text = "Sign In"
                                            }
                                        } catch (e: JSONException) {
                                            Log.e(TAG, "JSON parsing error: ${e.message}")
                                            e.printStackTrace()

                                            // Show error with red styling and toast
                                            showError("Invalid server response. Please try again.")

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

                                            // Show error with red styling and toast
                                            showError(errorMessage)

                                            // Re-enable login button
                                            buttonLogin.isEnabled = true
                                            buttonLogin.text = "Sign In"
                                        }
                                    }
                            ) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params["Email"] = idText
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

            // Show error with red styling and toast
            showError("Error during login: ${e.message}")

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
}
