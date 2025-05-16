from flask import Flask, request, jsonify
from flask_mysqldb import MySQL
from flask_cors import CORS
import bcrypt
import openai
import re
from openai import OpenAI

client = OpenAI(api_key="[REDACTED]") 

app = Flask(__name__)
CORS(app)

# ✅ Config goes here first
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'root'
app.config['MYSQL_PASSWORD'] = ''
app.config['MYSQL_DB'] = 'essaygraderdb'
app.config['MYSQL_CURSORCLASS'] = 'DictCursor'

mysql = MySQL(app)

# 🔧 Configurable limits
MAX_ESSAY_WORDS = 500
MAX_FEEDBACK_TOKENS = int(200 * 1.33)

@app.route('/config', methods=['GET'])
def get_config():
    return jsonify({
        "max_essay_words": MAX_ESSAY_WORDS
    })


def grade_essay_with_gpt(essay_text):
    prompt = f"""
You are an expert English essay grader. Read the following essay and provide:

1. A score from 0 to 100
2. Constructive feedback for improvement

Format your response exactly like this:
Score: <number>
Feedback: <feedback text>

Essay:
\"\"\"{essay_text}\"\"\"
"""

    response = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.7,
        max_tokens=MAX_FEEDBACK_TOKENS
    )

    reply = response.choices[0].message.content
    print("🎯 GPT Output:", reply)

    match = re.search(r"Score:\s*(\d+)\s*Feedback:\s*(.+)", reply, re.DOTALL)
    if match:
        score = int(match.group(1))
        feedback = match.group(2).strip()
        return score, feedback
    else:
        return 0, "Could not parse GPT output."


# ✅ Routes
@app.route('/register', methods=['POST'])
def register():
    print("⚡ /register hit")
    data = request.get_json()
    print("📩 Incoming JSON:", data)
    data = request.get_json()

    name = data.get('name')
    email = data.get('email')
    password = data.get('password')

    if not all([name, email, password]):
        return jsonify({'status': 'error', 'message': 'Missing fields'}), 400

    hashed_pw = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

    cur = mysql.connection.cursor()
    try:
        cur.execute("INSERT INTO users (name, email, password) VALUES (%s, %s, %s)", (name, email, hashed_pw))
        mysql.connection.commit()
        return jsonify({'status': 'success'})
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 409
    finally:
        cur.close()

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    print("LOGIN PAYLOAD:", data)

    email = data.get('email')
    password = data.get('password')

    if not all([email, password]):
        return jsonify({'status': 'error', 'message': 'Missing fields'}), 400

    cur = mysql.connection.cursor()
    cur.execute("SELECT id, name, password FROM users WHERE email = %s", (email,))
    user = cur.fetchone()
    cur.close()

    if user and bcrypt.checkpw(password.encode('utf-8'), user['password'].encode('utf-8')):
        return jsonify({
            'status': 'success',
            'id': user['id'],
            'name': user['name'],
            'email': email
        })

    return jsonify({'status': 'error', 'message': 'Invalid email or password'}), 401

@app.route('/submit', methods=['POST'])
@app.route('/submit', methods=['POST'])
def submit_essay():
    data = request.get_json()
    essay = data.get('essay')
    user_id = data.get('user_id')

    if not essay or not user_id:
        return jsonify({'status': 'error', 'message': 'Missing data'}), 400

    if len(essay.split()) > MAX_ESSAY_WORDS:
        return jsonify({'status': 'error', 'message': f'Essay too long (max {MAX_ESSAY_WORDS} words)'}), 400

    try:
        print("🧠 Calling GPT to grade...")
        score, feedback = grade_essay_with_gpt(essay)
        print(f"✅ Score: {score}, Feedback: {feedback[:200]}")
    except Exception as e:
        print("🔥 GPT Grading Failed:", str(e))
        return jsonify({'status': 'error', 'message': 'AI error: ' + str(e)}), 500

    cur = mysql.connection.cursor()
    cur.execute("INSERT INTO submissions (user_id, essay, score, feedback) VALUES (%s, %s, %s, %s)",
                (user_id, essay, score, feedback))
    mysql.connection.commit()
    cur.close()

    return jsonify({
        'status': 'success',
        'score': score,
        'feedback': feedback
    })


@app.route('/history/<int:user_id>', methods=['GET'])
def get_history(user_id):
    cur = mysql.connection.cursor()
    cur.execute("SELECT id, essay, score, feedback, submitted_at FROM submissions WHERE user_id = %s ORDER BY submitted_at DESC", (user_id,))
    submissions = cur.fetchall()
    cur.close()

    return jsonify({'status': 'success', 'history': submissions})
@app.route('/change_password', methods=['POST'])
def change_password():
    data = request.get_json()
    user_id = data.get('user_id')
    current_pw = data.get('current_password')
    new_pw = data.get('new_password')

    if not all([user_id, current_pw, new_pw]):
        return jsonify({'status': 'error', 'message': 'Missing data'}), 400

    cur = mysql.connection.cursor()
    cur.execute("SELECT password FROM users WHERE id = %s", (user_id,))
    user = cur.fetchone()

    if not user:
        cur.close()
        return jsonify({'status': 'error', 'message': 'User not found'}), 404

    stored_hash = user['password'].encode('utf-8')
    if not bcrypt.checkpw(current_pw.encode('utf-8'), stored_hash):
        cur.close()
        return jsonify({'status': 'error', 'message': 'Incorrect current password'}), 401

    new_hash = bcrypt.hashpw(new_pw.encode('utf-8'), bcrypt.gensalt())
    cur.execute("UPDATE users SET password = %s WHERE id = %s", (new_hash, user_id))
    mysql.connection.commit()
    cur.close()

    return jsonify({'status': 'success'})

@app.route('/delete/<int:submission_id>', methods=['DELETE'])
def delete_submission(submission_id):
    cur = mysql.connection.cursor()
    try:
        cur.execute("DELETE FROM submissions WHERE id = %s", (submission_id,))
        mysql.connection.commit()
        return jsonify({'status': 'success'})
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500
    finally:
        cur.close()


# ✅ NOW run app at the very end
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)


