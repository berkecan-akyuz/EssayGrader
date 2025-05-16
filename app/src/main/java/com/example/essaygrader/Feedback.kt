package com.example.essaygrader

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream


class Feedback : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvFeedback = findViewById<TextView>(R.id.tvFeedback)

        val score = intent.getIntExtra("score", 0)
        val feedback = intent.getStringExtra("feedback") ?: "No feedback received."

        tvScore.text = "Score: $score/100"
        tvFeedback.text = "Feedback: $feedback"

        val btnDownload = findViewById<Button>(R.id.btnDownloadReport)
        btnDownload.setOnClickListener {
            val score = intent.getIntExtra("score", 0)
            val feedback = intent.getStringExtra("feedback") ?: "No feedback available"
            val essay = intent.getStringExtra("essay") ?: "No essay text available"
            val wordCount = essay.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size

            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40
            val maxWidth = pageWidth - 2 * margin
            val lineSpacing = 22f

            val paint = Paint().apply {
                textSize = 14f
                color = Color.BLACK
            }

            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var y = margin.toFloat()

            fun startNewPage() {
                pdfDocument.finishPage(page)
                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = margin.toFloat()
            }

            fun drawWrappedText(text: String) {
                val words = text.split(" ")
                var line = ""
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    val lineWidth = paint.measureText(testLine)
                    if (lineWidth > maxWidth) {
                        if (y + lineSpacing > pageHeight - margin) startNewPage()
                        canvas.drawText(line, margin.toFloat(), y, paint)
                        y += lineSpacing
                        line = word
                    } else {
                        line = testLine
                    }
                }
                if (line.isNotEmpty()) {
                    if (y + lineSpacing > pageHeight - margin) startNewPage()
                    canvas.drawText(line, margin.toFloat(), y, paint)
                    y += lineSpacing
                }
            }

            // 📝 Header
            canvas.drawText("Essay Feedback Report", margin.toFloat(), y, paint)
            y += 30f

            // ✍️ Essay
            canvas.drawText("Essay:", margin.toFloat(), y, paint)
            y += 20f
            drawWrappedText(essay)
            y += 20f

            // 🔢 Word Count
            canvas.drawText("Word Count: $wordCount", margin.toFloat(), y, paint)
            y += 30f

            // 📊 Score
            canvas.drawText("Score: $score/100", margin.toFloat(), y, paint)
            y += 30f

            // 🧠 Feedback
            canvas.drawText("Feedback:", margin.toFloat(), y, paint)
            y += 20f
            drawWrappedText(feedback)

            pdfDocument.finishPage(page)

            try {
                val fileName = "EssayFeedback_${System.currentTimeMillis()}.pdf"
                val file = File(getExternalFilesDir(null), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(this, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        }



        val btnBack = findViewById<Button>(R.id.btnBackToDashboard)
        btnBack.setOnClickListener {
            NavigationHelper.goToDashboard(this);
        }
    }
}
