package com.example.essaygrader


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import java.io.File
import java.io.FileOutputStream



class SubmissionAdapter(
    private val submissions: MutableList<Submission>,
    private val context: Context
) : RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder>() {

    class SubmissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvScore: TextView = view.findViewById(R.id.tvItemScore)
        val tvFeedback: TextView = view.findViewById(R.id.tvItemFeedback)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val btnDelete: TextView = view.findViewById(R.id.btnDelete)
        val btnViewEssay: Button = view.findViewById(R.id.btnViewEssay)
        val btnDownloadEssay: Button = view.findViewById(R.id.btnDownloadEssay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
        return SubmissionViewHolder(view)
    }


    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        val submission = submissions[position]
        holder.tvScore.text = "Score: ${submission.score}"
        holder.tvFeedback.text = submission.feedback
        holder.tvDate.text = submission.submitted_at

        holder.btnViewEssay.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_view_essay, null)
            val essayTextView = dialogView.findViewById<TextView>(R.id.tvEssayContent)
            val wordCountTextView = dialogView.findViewById<TextView>(R.id.tvWordCount)
            val closeBtn = dialogView.findViewById<Button>(R.id.btnCloseDialog)

            val essay = submission.essay
            essayTextView.text = essay

            val wordCount = essay.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            wordCountTextView.text = "Word Count: $wordCount"

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            closeBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }





        holder.btnDownloadEssay.setOnClickListener {
            val score = submission.score
            val feedback = submission.feedback
            val essay = submission.essay
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
                val file = File(context.getExternalFilesDir(null), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(context, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        }


        holder.btnDelete.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val submissionId = submissions[currentPosition].id
                val url = "http://10.0.2.2:5000/delete/$submissionId"

                val request = object : StringRequest(
                    Method.DELETE, url,
                    Response.Listener {
                        Toast.makeText(context, "Submission deleted", Toast.LENGTH_SHORT).show()
                        submissions.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(context, "Failed to delete: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                ) {}

                Volley.newRequestQueue(context).add(request)
            }
        }

    }

    override fun getItemCount(): Int = submissions.size
}
