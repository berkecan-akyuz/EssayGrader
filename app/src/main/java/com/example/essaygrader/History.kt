package com.example.essaygrader

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request

class History : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val userId = SessionManager.userId  // use SessionManager if available
        val rvHistory = findViewById<RecyclerView>(R.id.rvEssayHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        val submissions = mutableListOf<Submission>()
        val adapter = SubmissionAdapter(submissions, this)
        rvHistory.adapter = adapter

        val url = "http://10.0.2.2:5000/history/$userId"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val historyArray = response.getJSONArray("history")
                submissions.clear()
                for (i in 0 until historyArray.length()) {
                    val obj = historyArray.getJSONObject(i)
                    submissions.add(
                        Submission(
                            obj.getInt("id"),
                            obj.getString("essay"),
                            obj.getInt("score"),
                            obj.getString("feedback"),
                            obj.getString("submitted_at")
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            },
            { error ->
                Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(request)

        findViewById<Button>(R.id.btnBackToDashboard).setOnClickListener {
            NavigationHelper.goToDashboard(this)
        }
    }
}
