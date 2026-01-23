# API Quick Start Guide

## 🚀 Step-by-Step Setup & Testing

### Step 1: Start the Server

```bash
cd /Users/istieak/Documents/Work/exam_backend
mvn spring-boot:run
```

**Wait for:** `Exam Management System Started Successfully!`  
**Server URL:** http://localhost:8080

---

## 📝 Step 2: Create Accounts

### A. Create a Teacher Account

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -c teacher-cookies.txt \
  -d '{
    "username": "teacher1",
    "password": "pass123",
    "email": "teacher@university.edu",
    "fullName": "Professor Smith",
    "role": "TEACHER"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "some-uuid",
    "username": "teacher1",
    "email": "teacher@university.edu",
    "fullName": "Professor Smith",
    "role": "TEACHER"
  }
}
```

### B. Create a Student Account

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -c student-cookies.txt \
  -d '{
    "username": "student1",
    "password": "pass123",
    "email": "student@university.edu",
    "fullName": "John Doe",
    "role": "STUDENT"
  }'
```

**Note:** The `-c` flag saves the session cookie to a file for subsequent requests.

---

## 🔐 Step 3: Login (If Needed)

If you need to login later or lost your session:

### Teacher Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c teacher-cookies.txt \
  -d '{
    "username": "teacher1",
    "password": "pass123"
  }'
```

### Student Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c student-cookies.txt \
  -d '{
    "username": "student1",
    "password": "pass123"
  }'
```

---

## 📚 Step 4: Create an Exam (Teacher Only)

### Important: Calculate Timestamps

**Get current time in milliseconds:**
```bash
# On Mac/Linux
date +%s000

# Or use this to get 2 hours from now:
echo $(($(date +%s) * 1000 + 7200000))
```

**Example timestamps:**
- Start: `1737590400000` (2 hours from now)
- End: `1737594000000` (3 hours from now)

### Create MCQ Exam

```bash
curl -X POST http://localhost:8080/api/exams \
  -H "Content-Type: application/json" \
  -b teacher-cookies.txt \
  -d '{
    "title": "Java Programming Midterm",
    "course": "CS101 - Introduction to Programming",
    "description": "Covers chapters 1-5: basics, OOP, and collections",
    "examType": "MCQ",
    "durationMinutes": 60,
    "totalMarks": 30,
    "passingMarks": 18,
    "startDateTime": 1737590400000,
    "endDateTime": 1737594000000,
    "status": "PUBLISHED",
    "questions": [
      {
        "type": "MCQ",
        "questionText": "What is the output of: System.out.println(2 + 2);",
        "options": ["2", "22", "4", "Error"],
        "correctAnswer": "4",
        "marks": 10,
        "questionOrder": 1
      },
      {
        "type": "MCQ",
        "questionText": "Which keyword is used for inheritance in Java?",
        "options": ["implements", "extends", "inherits", "super"],
        "correctAnswer": "extends",
        "marks": 10,
        "questionOrder": 2
      },
      {
        "type": "MCQ",
        "questionText": "What is the size of int in Java?",
        "options": ["8 bits", "16 bits", "32 bits", "64 bits"],
        "correctAnswer": "32 bits",
        "marks": 10,
        "questionOrder": 3
      }
    ]
  }'
```

### Create CQ (Creative Question) Exam

```bash
curl -X POST http://localhost:8080/api/exams \
  -H "Content-Type: application/json" \
  -b teacher-cookies.txt \
  -d '{
    "title": "Database Design Final",
    "course": "CS201 - Database Systems",
    "description": "Design and normalization problems",
    "examType": "CQ",
    "durationMinutes": 120,
    "totalMarks": 50,
    "passingMarks": 25,
    "startDateTime": 1737590400000,
    "endDateTime": 1737597600000,
    "status": "PUBLISHED",
    "questions": [
      {
        "type": "CQ",
        "questionText": "Design a normalized database schema for a library management system. Include at least 5 tables with proper relationships.",
        "marks": 25,
        "questionOrder": 1
      },
      {
        "type": "CQ",
        "questionText": "Explain the differences between 2NF and 3NF with examples.",
        "marks": 25,
        "questionOrder": 2
      }
    ]
  }'
```

**Success Response:**
```json
{
  "success": true,
  "message": "Exam created successfully",
  "data": {
    "id": "exam-uuid-here",
    "title": "Java Programming Midterm",
    "course": "CS101",
    ...
  }
}
```

**Save the exam ID from the response!**

---

## 👀 Step 5: View Available Exams (Student)

```bash
curl -X GET http://localhost:8080/api/exams/published \
  -H "Content-Type: application/json" \
  -b student-cookies.txt
```

---

## ✍️ Step 6: Submit an Exam (Student)

**Note:** Replace `{examId}` with actual exam ID from Step 4.

### Submit MCQ Exam

```bash
curl -X POST http://localhost:8080/api/exams/{examId}/submit \
  -H "Content-Type: application/json" \
  -b student-cookies.txt \
  -d '{
    "question-id-1": "4",
    "question-id-2": "extends",
    "question-id-3": "32 bits"
  }'
```

**Note:** Use the actual question IDs from the exam. You can get them by fetching the exam details first:

```bash
curl -X GET http://localhost:8080/api/exams/{examId} \
  -b student-cookies.txt
```

### Submit CQ Exam

```bash
curl -X POST http://localhost:8080/api/exams/{examId}/submit \
  -H "Content-Type: application/json" \
  -b student-cookies.txt \
  -d '{
    "question-id-1": "Here is my database schema design...",
    "question-id-2": "2NF removes partial dependencies while 3NF..."
  }'
```

---

## 📊 Step 7: View Results & Grade (Teacher)

### View Submissions for an Exam

```bash
curl -X GET http://localhost:8080/api/exams/{examId}/submissions \
  -b teacher-cookies.txt
```

### Grade CQ Questions

**For CQ exams, teacher must manually grade essay answers:**

```bash
curl -X POST http://localhost:8080/api/exams/submissions/{submissionId}/grade \
  -H "Content-Type: application/json" \
  -b teacher-cookies.txt \
  -d '{
    "essayScore": 35
  }'
```

---

## 🔍 Additional Useful Endpoints

### Check Your Session

```bash
curl -X GET http://localhost:8080/api/auth/session \
  -b teacher-cookies.txt
```

### Get Your Exams (Teacher)

```bash
curl -X GET http://localhost:8080/api/exams/my-exams \
  -b teacher-cookies.txt
```

### Get Your Submissions (Student)

```bash
curl -X GET http://localhost:8080/api/exams/my-submissions \
  -b student-cookies.txt
```

### View Specific Exam Details

```bash
curl -X GET http://localhost:8080/api/exams/{examId} \
  -b cookies.txt
```

### Update an Exam (Teacher)

```bash
curl -X PUT http://localhost:8080/api/exams/{examId} \
  -H "Content-Type: application/json" \
  -b teacher-cookies.txt \
  -d '{
    "title": "Updated Title",
    "course": "CS101",
    "examType": "MCQ",
    "durationMinutes": 90,
    "totalMarks": 40,
    "passingMarks": 20,
    "startDateTime": 1737590400000,
    "endDateTime": 1737594000000,
    "status": "PUBLISHED",
    "questions": [...]
  }'
```

### Delete an Exam (Teacher)

```bash
curl -X DELETE http://localhost:8080/api/exams/{examId} \
  -b teacher-cookies.txt
```

### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

---

## ⚠️ Common Issues & Solutions

### 1. "Access denied: Teachers only"
**Problem:** Using student cookies for teacher-only endpoint  
**Solution:** Use the correct cookie file (`teacher-cookies.txt`)

### 2. "Validation failed" errors
**Problem:** Missing required fields or invalid data  
**Solution:** Check that all required fields are present:
- `title`, `course`, `examType`, `durationMinutes`
- `totalMarks`, `passingMarks`
- `startDateTime`, `endDateTime`
- `questions` array with at least one question

**Example error response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "course": "Course is required",
    "totalMarks": "Total marks (30) must equal sum of question marks (20)"
  }
}
```

### 3. "Cannot update exam: Students have already submitted"
**Problem:** Trying to update/delete exam after submissions exist  
**Solution:** This is by design - exams with submissions are locked

### 4. "Exam has not started yet"
**Problem:** Trying to submit before startDateTime  
**Solution:** Wait until the exam starts or update the startDateTime to current time

### 5. Session expired
**Problem:** Cookie expired or invalid  
**Solution:** Login again to get a new session

---

## 🧪 Complete Test Flow Example

```bash
# 1. Start server
mvn spring-boot:run

# 2. Create teacher account
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -c teacher.txt \
  -d '{"username":"prof","password":"pass123","email":"prof@uni.edu","fullName":"Professor","role":"TEACHER"}'

# 3. Create student account
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -c student.txt \
  -d '{"username":"john","password":"pass123","email":"john@uni.edu","fullName":"John Doe","role":"STUDENT"}'

# 4. Get current timestamp for exam times
START_TIME=$(($(date +%s) * 1000))
END_TIME=$(($(date +%s) * 1000 + 3600000))

# 5. Create exam (teacher)
curl -X POST http://localhost:8080/api/exams \
  -H "Content-Type: application/json" \
  -b teacher.txt \
  -d "{
    \"title\": \"Quick Test\",
    \"course\": \"CS101\",
    \"examType\": \"MCQ\",
    \"durationMinutes\": 60,
    \"totalMarks\": 10,
    \"passingMarks\": 6,
    \"startDateTime\": $START_TIME,
    \"endDateTime\": $END_TIME,
    \"status\": \"PUBLISHED\",
    \"questions\": [{
      \"type\": \"MCQ\",
      \"questionText\": \"What is 2+2?\",
      \"options\": [\"3\", \"4\", \"5\", \"6\"],
      \"correctAnswer\": \"4\",
      \"marks\": 10,
      \"questionOrder\": 1
    }]
  }"

# 6. View exams (student)
curl -X GET http://localhost:8080/api/exams/published -b student.txt

# 7. Submit exam (student) - replace EXAM_ID and QUESTION_ID
curl -X POST http://localhost:8080/api/exams/EXAM_ID/submit \
  -H "Content-Type: application/json" \
  -b student.txt \
  -d '{"QUESTION_ID": "4"}'

# 8. View submissions (teacher)
curl -X GET http://localhost:8080/api/exams/EXAM_ID/submissions -b teacher.txt
```

---

## 📱 Using Postman/Insomnia

If you prefer a GUI tool:

1. **Import these settings:**
   - Base URL: `http://localhost:8080`
   - Enable cookies/session management

2. **Follow this sequence:**
   - POST `/api/auth/signup` (creates account & saves session)
   - POST `/api/exams` (create exam)
   - GET `/api/exams/published` (view exams)
   - POST `/api/exams/{id}/submit` (submit)

3. **Important:** Make sure "Automatically follow redirects" and "Send cookies" are enabled

---

## 🎯 Key Points to Remember

1. **Session-based authentication** - Cookies are automatically managed
2. **All required fields must be present** - Server validates everything
3. **totalMarks must equal sum of question marks** - Server will reject mismatches
4. **startDateTime must be before endDateTime** - Validated on server
5. **MCQ questions need options and correctAnswer** - CQ questions don't
6. **Can't modify exam after submissions exist** - Protected by server
7. **Timestamps are in milliseconds** - Use `date +%s000` to get current time

---

## 🔗 Next Steps

1. ✅ Test authentication flow (signup/login)
2. ✅ Create a test exam with all required fields
3. ✅ Test validation by sending invalid data
4. ✅ Test submission flow
5. ✅ Test teacher grading flow
6. ✅ Integrate with your React frontend

**Server is running and ready at:** http://localhost:8080

**Check server status:** http://localhost:8080 (shows welcome JSON)

**Health check:** http://localhost:8080/health
