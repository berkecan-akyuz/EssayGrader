package com.example.essaygrader

import android.content.Context
import android.content.Intent

object NavigationHelper {
    fun goToDashboard(context: Context) {
        val intent = Intent(context, Dashboard::class.java)
        intent.putExtra("userId", SessionManager.userId)
        intent.putExtra("userName", SessionManager.userName)
        intent.putExtra("userEmail", SessionManager.userEmail)
        context.startActivity(intent)
    }
}
