package com.example.essaygrader

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val etName = findViewById<EditText>(R.id.etUserName)
        val etEmail = findViewById<EditText>(R.id.etUserEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 1️⃣ Fill fields from SessionManager
        etName.setText(SessionManager.userName)
        etEmail.setText(SessionManager.userEmail)


        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            startActivity(Intent(this, ChangePassword::class.java))
        }

        btnLogout.setOnClickListener {
            // 🔁 Clear session memory
            SessionManager.userId = -1
            SessionManager.userName = null
            SessionManager.userEmail = null

            // 🧹 Clear stored session (prevents auto-login)
            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 🔄 Restart at login cleanly
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        findViewById<Button>(R.id.btnBackFromSettings).setOnClickListener {
            NavigationHelper.goToDashboard(this)
            finish()
        }
    }
}
