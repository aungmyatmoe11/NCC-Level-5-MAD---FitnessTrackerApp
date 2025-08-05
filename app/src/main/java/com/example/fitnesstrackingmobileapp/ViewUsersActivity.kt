package com.example.fitnesstrackingmobileapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class ViewUsersActivity : AppCompatActivity() {

    private var listView: ListView? = null
    private var userList: MutableList<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_artists)

        listView = findViewById(R.id.listViewArtists) as ListView
        userList = mutableListOf<User>()
        loadUsers()
    }

    private fun loadUsers() {
        val stringRequest = StringRequest(Request.Method.GET,
            EndPoints.URL_GET_USERS,
            Response.Listener<String> { s ->
                try {
                    val obj = JSONObject(s)
                    if (!obj.getBoolean("error")) {
                        val array = obj.getJSONArray("users")

                        for (i in 0..array.length() - 1) {
                            val objectArtist = array.getJSONObject(i)
                            val user = User(
                                objectArtist.getInt("UserID"),
                                objectArtist.getString("UserName"),
                                objectArtist.getString("Password")
                            )
                            userList!!.add(user)
                            val adapter = UsertList(this@ViewUsersActivity, userList!!)
                            listView!!.adapter = adapter
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { volleyError -> Toast.makeText(applicationContext, volleyError.message, Toast.LENGTH_LONG).show() })


        @Throws(AuthFailureError::class)
        fun  getHeaders():Map<String, String> {
            val   headers =  HashMap<String, String>();
            headers.put("Content-Type", "application/json; charset=utf-8");
            return headers
        }
        stringRequest.setRetryPolicy(DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        val requestQueue = Volley.newRequestQueue(this)

        requestQueue.add<String>(stringRequest)
    }
}