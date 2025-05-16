package com.example.essaygrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.essaygrader.Dashboard
import com.example.essaygrader.Register
import org.json.JSONObject

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔐 Auto-login check
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        val userName = prefs.getString("userName", null)
        val userEmail = prefs.getString("userEmail", null)

        if (userId != -1 && userName != null && userEmail != null) {
            SessionManager.userId = userId
            SessionManager.userName = userName
            SessionManager.userEmail = userEmail

            NavigationHelper.goToDashboard(this)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = "http://10.0.2.2:5000/login"
            val jsonBody = JSONObject()
            jsonBody.put("email", email)
            jsonBody.put("password", password)

            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    if (response.getString("status") == "success") {
                        Toast.makeText(this, "Welcome ${response.getString("name")}!", Toast.LENGTH_SHORT).show()

                        // ✅ Save session to SharedPreferences
                        with(prefs.edit()) {
                            putInt("userId", response.getInt("id"))
                            putString("userName", response.getString("name"))
                            putString("userEmail", response.getString("email"))
                            apply()
                        }

                        // ✅ Also update SessionManager
                        SessionManager.userId = response.getInt("id")
                        SessionManager.userName = response.getString("name")
                        SessionManager.userEmail = response.getString("email")

                        NavigationHelper.goToDashboard(this)
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        val errorJson = String(error.networkResponse.data)
                        try {
                            val jsonObject = JSONObject(errorJson)
                            val message = jsonObject.optString("message", "Login failed")
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Volley.newRequestQueue(this).add(request)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }
}
