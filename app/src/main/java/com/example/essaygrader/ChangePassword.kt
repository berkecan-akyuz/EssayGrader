package com.example.essaygrader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ChangePassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val etCurrent = findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmNewPassword)
        val btnUpdate = findViewById<Button>(R.id.btnUpdatePassword)

        btnUpdate.setOnClickListener {
            val current = etCurrent.text.toString().trim()
            val newPass = etNew.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length > 20) {
                Toast.makeText(this, "Password must not exceed 20 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!newPass.any { it.isUpperCase() }) {
                Toast.makeText(this, "Password must include at least one uppercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!newPass.any { it.isLowerCase() }) {
                Toast.makeText(this, "Password must include at least one lowercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = "http://10.0.2.2:5000/change_password"
            val body = JSONObject().apply {
                put("user_id", SessionManager.userId)
                put("current_password", current)
                put("new_password", newPass)
            }

            val request = JsonObjectRequest(
                Request.Method.POST, url, body,
                { response ->
                    val status = response.getString("status")
                    if (status == "success") {
                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                        NavigationHelper.goToDashboard(this)
                        finish()
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(this).add(request)
        }

        val btnCancel = findViewById<Button>(R.id.btnCancel)
        btnCancel.setOnClickListener{
            NavigationHelper.goToDashboard(this)
        }
    }
}
