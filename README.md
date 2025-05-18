````markdown
# ✍️ Essay Grader Mobile App

**Essay Grader** is a modern Android mobile application that allows users to submit English essays and receive AI-powered feedback and scoring. It’s designed for students, educators, or anyone looking to improve their writing.

## 📱 Features

- ✅ User registration & login
- ✍️ Submit essays directly or upload from PDF
- 📊 Get scores from 0 to 100
- 🧠 Receive constructive feedback (up to 200 words)
- 🧾 Word counter and essay length validation
- 🧷 Download feedback as PDF
- 🕓 View and manage submission history
- 🗑 Delete past submissions
- 🔒 Change password securely
- 🎨 Clean dark-themed UI using `#121212` and `#EDF2F4`

## 🛠 Tech Stack

- **Frontend**: Kotlin + Android SDK
- **Backend**: Python (Flask)
- **Database**: MySQL
- **AI Integration**: OpenAI API (GPT-4)
- **PDF Handling**: Android PdfDocument & PDFBox
- **Libraries**:
  - Volley (network requests)
  - PDFBox-Android (PDF reading)
  - AndroidX libraries

## 📷 Screenshots

| Login | Essay Submission | Feedback Report |
|-------|------------------|-----------------|
| ![login](screenshots/login.png) | ![submit](screenshots/submit.png) | ![feedback](screenshots/feedback.png) |

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Python 3.8+
- MySQL
- OpenAI API Key

### Setup Instructions

1. **Clone the repo**

```bash
git clone https://github.com/your-username/essay-grader-app.git
cd essay-grader-app
````

2. **Run backend server**

   * Set up `.env` for API keys and DB credentials
   * Start Flask server

```bash
cd server/
pip install -r requirements.txt
python app.py
```

3. **Run Android app**

   * Open project in Android Studio
   * Set `BASE_URL` in Kotlin code to your server IP
   * Build & run on emulator or device

## ⚙️ Configuration

Set your word limit or feedback size in the server:

```python
MAX_ESSAY_WORDS = 500
MAX_OUTPUT_WORDS = 200
```

## 🧪 Future Enhancements

* AI-generated grammar tips
* Essay topic recommendations
* Multi-language support
* In-app essay editor with spellcheck

---

Made with ❤️ using Kotlin + Python

```

---

Let me know if you want to include deployment instructions, API route documentation, or contributor credits!
```
