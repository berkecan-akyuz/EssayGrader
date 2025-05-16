package com.example.essaygrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userId = intent.getIntExtra("userId", -1)
        Log.d("Dashboard", "Received userId: $userId")

        findViewById<Button>(R.id.btnSubmitEssay).setOnClickListener {
            val intent = Intent(this, Submit::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
            val intent = Intent(this, History::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

}

