package com.example.fitnesstrackingmobileapp


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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

    //edittext and spinner
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var  buttonAddUser: Button
    lateinit var  buttonViewUser: Button
    lateinit var  buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //getting it from xml
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        //adding a click listener to button
        buttonAddUser=findViewById(R.id.buttonAddUser)
        //buttonViewUser = findViewById(R.id.buttonViewUsers)
        buttonLogin=findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener(){
         //   val intent = Intent(applicationContext, LoginActivity::class.java)
         //   startActivity(intent)
            loginUser()

        }

        buttonAddUser.setOnClickListener {
            // Navigate to register screen
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

       //buttonAddUser .setOnClickListener { addUser() }

        //in the second button click
        //opening the activity to display all the artist
        //it will give error as we dont have this activity so remove this part for now to run the app
//        buttonViewUser.setOnClickListener {
//            val intent = Intent(applicationContext, ViewUsersActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    private fun loginUser() {
        val username = username?.text.toString()

        val password = password?.text.toString()
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(applicationContext, "Username and password cannot be empty", Toast.LENGTH_LONG).show()
            return
        }
        //creating volley string request
        val stringRequest = @SuppressLint("SuspiciousIndentation")
        object : StringRequest(Request.Method.POST, EndPoints.URL_LOGIN_USER,
            Response.Listener<String> { response ->

                try {

                    val obj = JSONObject(response)
                    val error = obj.getBoolean("error")
                    if (!error) {
                        // Login successful
                        val message = obj.getString("message")

                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()

                        // Proceed to the next activity or update UI
                        val intent = Intent(this, HomeScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Login failed
                        val message = obj.getString("message")
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "JSON parsing error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(volleyError: VolleyError) {

                    Toast.makeText(applicationContext, "Unsuccessful Login", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, HomeScreenActivity::class.java)
                    startActivity(intent)
                    finish()

                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("UserName", username)
                params.put("Password", password)
                return params

            }

        }
        @Override
        @Throws(AuthFailureError::class)
        fun  getHeaders():Map<String, String> {
            val   headers =  HashMap<String, String>();
            headers.put("Content-Type", "application/json; charset=utf-8");
            return headers
        }
     //   stringRequest.setRetryPolicy(DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
    }

   //adding a new record to database

    private fun addUser() {

        //getting the record values
        val username = username?.text.toString()

        val password = password?.text.toString()
        //creating volley string request

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(applicationContext, "Username and password cannot be empty", Toast.LENGTH_LONG).show()
            return
        }
        val stringRequest = object : StringRequest(Request.Method.POST, EndPoints.URL_ADD_USER,
            Response.Listener<String> { response ->

                   try {

                        val obj = JSONObject(response)
                        Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()

                 } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(volleyError: VolleyError) {

                    Toast.makeText(applicationContext, volleyError.message, Toast.LENGTH_LONG).show()
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("UserName", username)
                params.put("Password", password)
                return params
            }
        }
        @Override
        @Throws(AuthFailureError::class)
         fun  getHeaders():Map<String, String> {
            val   headers =  HashMap<String, String>();
            headers.put("Content-Type", "application/json; charset=utf-8");
            return headers
        }
    //    stringRequest.setRetryPolicy(DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
    }
}