// RegisterActivity.kt
package com.example.fitnesstrackingmobileapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.AuthFailureError
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSubmit: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.registerUsername)
        etPassword = findViewById(R.id.registerPassword)
        etEmail = findViewById(R.id.registerEmail)
        btnSubmit = findViewById(R.id.buttonRegisterSubmit)

        btnSubmit.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val email = etEmail.text.toString().trim()
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performRegistration(username, password, email)
        }
    }

    private fun performRegistration(username: String, password: String, email: String) {
        val req = object : StringRequest(Method.POST, EndPoints.URL_ADD_USER,
            Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_LONG).show()
                    if (!obj.getBoolean("error")) {
                        finish()  // go back to login on success
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String,String> = mapOf(
                "UserName" to username,
                "Password" to password,
                "Email" to email
            )
        }
        VolleySingleton.instance?.addToRequestQueue(req)
    }
}
