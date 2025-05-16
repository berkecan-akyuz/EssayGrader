package com.example.essaygrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView


class Submit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit)

        val etEssayText = findViewById<EditText>(R.id.etEssayText)
        val btnSubmitEssay = findViewById<Button>(R.id.btnSubmitEssay)
        val btnBackToDashboard = findViewById<Button>(R.id.btnBackToDashboard)
        val tvWordLimit = findViewById<TextView>(R.id.tvWordLimit)


        val userId = intent.getIntExtra("userId", -1)

        btnBackToDashboard.setOnClickListener {
            NavigationHelper.goToDashboard(this);
        }

        var maxEssayWords = 0;
        val configUrl = "http://10.0.2.2:5000/config"
        val configRequest = JsonObjectRequest(
            Request.Method.GET, configUrl, null,
            { response ->
                maxEssayWords = response.optInt("max_essay_words", 500)
                tvWordLimit.text = "0 / $maxEssayWords words"  // update UI if needed
            },
            { error ->
                Log.e("Config", "Failed to load config", error)
            }
        )
        Volley.newRequestQueue(this).add(configRequest)

        btnSubmitEssay.setOnClickListener {
            val essayText = etEssayText.text.toString().trim()
            val wordCount = essayText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

            if (essayText.isEmpty()) {
                Toast.makeText(this, "Please enter your essay", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (wordCount > maxEssayWords) {
                Toast.makeText(this, "Essay exceeds the $maxEssayWords word limit", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val url = "http://10.0.2.2:5000/submit"
            val jsonBody = JSONObject().apply {
                put("user_id", userId)
                put("essay", essayText)
            }

            Log.d("Submit", "Submitting essay to $url")

            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    Log.d("Submit", "Server response: $response")

                    if (response.getString("status") == "success") {
                        val score = response.getInt("score")
                        val feedback = response.getString("feedback")

                        val intent = Intent(this, Feedback::class.java).apply {
                            putExtra("score", score)
                            putExtra("feedback", feedback)
                            putExtra("essay", essayText)
                        }

                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Grading failed", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    Log.e("Submit", "Error: ${error.message}", error)
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(this).add(request)
        }

        findViewById<Button>(R.id.btnUploadFile).setOnClickListener {
            filePickerLauncher.launch("*/*")
        }



        etEssayText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val wordCount = s.toString().trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                tvWordLimit.text = "$wordCount / $maxEssayWords words"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val mimeType = contentResolver.getType(uri)
            val essayText = when (mimeType) {
                "text/plain" -> contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() ?: "" }
                "application/pdf" -> extractPdfText(uri)
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extractDocxText(uri)
                else -> "Unsupported file type."
            }

            findViewById<EditText>(R.id.etEssayText).setText(essayText)
        }
    }

    private fun extractDocxText(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val document = org.apache.poi.xwpf.usermodel.XWPFDocument(inputStream)
            val extractor = org.apache.poi.xwpf.extractor.XWPFWordExtractor(document)
            val text = extractor.text
            extractor.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            "Error reading .docx file."
        }
    }

    private fun extractPdfText(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val pdf = com.tom_roush.pdfbox.pdmodel.PDDocument.load(inputStream)
            val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
            val text = stripper.getText(pdf)
            pdf.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            "Error reading PDF file."
        }
    }



}

