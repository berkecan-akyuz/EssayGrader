package com.example.essaygrader

data class Submission(
    val id: Int,
    val essay: String,
    val score: Int,
    val feedback: String,
    val submitted_at: String
)
