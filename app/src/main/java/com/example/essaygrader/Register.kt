package com.example.essaygrader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.widget.Toast
import android.content.Intent
import android.util.Log



class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val url = "http://10.0.2.2:5000/register"
            Log.d("Register", "Attempting to register at $url")

            val jsonBody = JSONObject()
            jsonBody.put("name", etName.text.toString())
            jsonBody.put("email", etEmail.text.toString())
            jsonBody.put("password", etPassword.text.toString())

            val password = etPassword.text.toString().trim()

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length > 20) {
                Toast.makeText(this, "Password must not exceed 20 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!password.any { it.isUpperCase() }) {
                Toast.makeText(this, "Password must include at least one uppercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!password.any { it.isLowerCase() }) {
                Toast.makeText(this, "Password must include at least one lowercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    Log.d("Register", "Success: $response")
                    Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        val errorJson = String(error.networkResponse.data)
                        try {
                            val jsonObject = JSONObject(errorJson)
                            val message = jsonObject.optString("message", "Registration failed")
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            )

            val email = etEmail.text.toString().trim()
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Volley.newRequestQueue(this).add(request)
        }


        tvLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
}
