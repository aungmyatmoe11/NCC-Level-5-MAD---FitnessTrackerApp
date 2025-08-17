// RegisterActivity.kt
package com.example.fitnesstrackingmobileapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSubmit: Button
    private lateinit var emailLayout: TextInputLayout
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_register)

            // Initialize views
            etUsername = findViewById(R.id.registerUsername)
            etPassword = findViewById(R.id.registerPassword)
            etEmail = findViewById(R.id.registerEmail)
            btnSubmit = findViewById(R.id.buttonRegisterSubmit)
            emailLayout = findViewById(R.id.emailLayout)
            usernameLayout = findViewById(R.id.usernameLayout)
            passwordLayout = findViewById(R.id.passwordLayout)

            // Set up email validation
            setupEmailValidation()

            // Set up custom password toggle functionality
            setupPasswordToggle()

            btnSubmit.setOnClickListener {
                Log.d(TAG, "Register button clicked")
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val email = etEmail.text.toString().trim()

                // Validate all fields
                if (!validateAllFields(username, password, email)) {
                    return@setOnClickListener
                }

                performRegistration(username, password, email)
            }

            Log.d(TAG, "RegisterActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing register screen", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupEmailValidation() {
        // Real-time email validation
        etEmail.addTextChangedListener(
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
                        validateEmail(s.toString().trim())
                    }
                }
        )

        // Real-time username validation
        etUsername.addTextChangedListener(
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
                        validateUsername(s.toString().trim())
                    }
                }
        )

        // Real-time password validation
        etPassword.addTextChangedListener(
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
        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        // Initial password toggle icon ကို set လုပ်ခြင်း
        restorePasswordToggleIcon()
    }

    /**
     * Password toggle icon ကို ပြန်လည်ထိန်းထားခြင်း Validation error ဖြစ်နေစဉ် icon
     * ပျောက်မသွားစေရန်
     */
    private fun restorePasswordToggleIcon() {
        // Current password visibility state ကို စစ်ဆေးပြီး သင့်တော်သော icon ကို set လုပ်ခြင်း
        val currentTransformation = etPassword.transformationMethod

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
        val currentTransformation = etPassword.transformationMethod

        if (currentTransformation is PasswordTransformationMethod) {
            // Password ကိုြသမည် (ဖျောက်ထားခြင်းမှြသခြင်းသို့ ပြောင်းလဲခြင်း)
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            // Password ကို ဖျောက်ထားမည် (ြသခြင်းမှ ဖျောက်ထားခြင်းသို့ ပြောင်းလဲခြင်း)
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        // Password toggle icon ကို ပြန်လည်ထိန်းထားခြင်း
        restorePasswordToggleIcon()

        // Cursor position ကို ထိန်းထားရန်
        val selection = etPassword.selectionEnd
        etPassword.setSelection(selection)
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                emailLayout.error = "Email is required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailLayout.error = "Please enter a valid email address"
                false
            }
            email.length < 5 -> {
                emailLayout.error = "Email is too short"
                false
            }
            email.length > 100 -> {
                emailLayout.error = "Email is too long"
                false
            }
            !email.contains("@") -> {
                emailLayout.error = "Email must contain @ symbol"
                false
            }
            !email.contains(".") -> {
                emailLayout.error = "Email must contain a domain"
                false
            }
            email.startsWith("@") || email.endsWith("@") -> {
                emailLayout.error = "Invalid email format"
                false
            }
            email.startsWith(".") || email.endsWith(".") -> {
                emailLayout.error = "Email cannot start or end with a dot"
                false
            }
            email.contains("..") -> {
                emailLayout.error = "Email cannot contain consecutive dots"
                false
            }
            else -> {
                emailLayout.error = null
                true
            }
        }
    }

    private fun validateUsername(username: String): Boolean {
        return when {
            username.isEmpty() -> {
                usernameLayout.error = "Username is required"
                false
            }
            username.length < 3 -> {
                usernameLayout.error = "Username must be at least 3 characters"
                false
            }
            username.length > 20 -> {
                usernameLayout.error = "Username must be less than 20 characters"
                false
            }
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                usernameLayout.error = "Username can only contain letters, numbers, and underscores"
                false
            }
            else -> {
                usernameLayout.error = null
                true
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        return when {
            password.isEmpty() -> {
                passwordLayout.error = "Password is required"
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                false
            }
            password.length < 6 -> {
                passwordLayout.error = "Password must be at least 6 characters"
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                false
            }
            password.length > 50 -> {
                passwordLayout.error = "Password must be less than 50 characters"
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                false
            }
            !password.matches(Regex(".*[A-Z].*")) -> {
                passwordLayout.error = "Password must contain at least one uppercase letter"
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                false
            }
            !password.matches(Regex(".*[a-z].*")) -> {
                passwordLayout.error = "Password must contain at least one lowercase letter"
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                false
            }
            !password.matches(Regex(".*\\d.*")) -> {
                passwordLayout.error = "Password must contain at least one number"
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                false
            }
            else -> {
                passwordLayout.error = null
                // Password toggle icon ကို ထိန်းထားရန်
                restorePasswordToggleIcon()
                true
            }
        }
    }

    private fun validateAllFields(username: String, password: String, email: String): Boolean {
        val isUsernameValid = validateUsername(username)
        val isPasswordValid = validatePassword(password)
        val isEmailValid = validateEmail(email)

        if (!isUsernameValid || !isPasswordValid || !isEmailValid) {
            Toast.makeText(this, "Please fix the validation errors above", Toast.LENGTH_SHORT)
                    .show()
            return false
        }

        return true
    }

    private fun performRegistration(username: String, password: String, email: String) {
        try {
            Log.d(TAG, "Attempting registration for user: $username")
            Log.d(TAG, "API URL: ${EndPoints.URL_ADD_USER}")

            // Show loading indicator
            btnSubmit.isEnabled = false
            btnSubmit.text = "Creating Account..."

            val req =
                    object :
                            StringRequest(
                                    Request.Method.POST,
                                    EndPoints.URL_ADD_USER,
                                    Response.Listener<String> { response ->
                                        try {
                                            Log.d(TAG, "Registration response received: $response")
                                            val obj = JSONObject(response)
                                            val message = obj.getString("message")
                                            val error = obj.getBoolean("error")

                                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                                            if (!error) {
                                                Log.d(TAG, "Registration successful")
                                                finish() // go back to login on success
                                            } else {
                                                Log.w(TAG, "Registration failed: $message")
                                            }

                                            // Re-enable button
                                            btnSubmit.isEnabled = true
                                            btnSubmit.text = "Create Account"
                                        } catch (e: JSONException) {
                                            Log.e(TAG, "JSON parsing error: ${e.message}")
                                            e.printStackTrace()
                                            Toast.makeText(
                                                            this,
                                                            "Invalid server response. Please try again.",
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()

                                            // Re-enable button
                                            btnSubmit.isEnabled = true
                                            btnSubmit.text = "Create Account"
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
                                                            this@RegisterActivity,
                                                            errorMessage,
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()

                                            // Re-enable button
                                            btnSubmit.isEnabled = true
                                            btnSubmit.text = "Create Account"
                                        }
                                    }
                            ) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val params =
                                    mapOf(
                                            "UserName" to username,
                                            "Password" to password,
                                            "Email" to email
                                    )
                            Log.d(TAG, "Registration parameters: $params")
                            return params
                        }

                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-Type"] = "application/x-www-form-urlencoded"
                            headers["Accept"] = "application/json"
                            Log.d(TAG, "Registration headers: $headers")
                            return headers
                        }
                    }

            // Set timeout for the request
            req.retryPolicy =
                    com.android.volley.DefaultRetryPolicy(
                            10000, // 10 seconds timeout
                            1, // 1 retry
                            1.0f // No backoff multiplier
                    )

            FitnessTrackerApplication.instance?.addToRequestQueue(req)
            Log.d(TAG, "Registration request sent to queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error in performRegistration: ${e.message}")
            Toast.makeText(this, "Error during registration: ${e.message}", Toast.LENGTH_LONG)
                    .show()

            // Re-enable button
            btnSubmit.isEnabled = true
            btnSubmit.text = "Create Account"
        }
    }
}
