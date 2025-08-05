package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TestLoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TestLoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        try {
            val username = findViewById<EditText>(R.id.username)
            val password = findViewById<EditText>(R.id.password)
            val buttonLogin = findViewById<Button>(R.id.buttonLogin)
            val buttonAddUser = findViewById<Button>(R.id.buttonAddUser)

            buttonLogin.setOnClickListener {
                Log.d(TAG, "Login button clicked")

                val usernameText = username.text.toString()
                val passwordText = password.text.toString()

                Log.d(TAG, "Username: $usernameText, Password: $passwordText")

                if (usernameText.isEmpty() || passwordText.isEmpty()) {
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_LONG)
                            .show()
                    return@setOnClickListener
                }

                // For testing, accept any non-empty credentials
                Log.d(TAG, "Test login successful")
                Toast.makeText(this, "Test login successful!", Toast.LENGTH_LONG).show()

                // Navigate to home screen
                try {
                    val intent = Intent(this, HomeScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to HomeScreenActivity: ${e.message}")
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            buttonAddUser.setOnClickListener {
                Log.d(TAG, "Create Account button clicked")
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
