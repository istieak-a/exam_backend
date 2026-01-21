# Exam Management System - Backend

A comprehensive Java-based backend system for university exam management with real-time chat functionality.

## 🎯 Features

### Core Features
- **User Management**: Separate Teacher and Student roles with authentication
- **Exam Management**: Create, edit, and publish exams with MCQ and Essay questions
- **Auto-Grading**: Automatic marking for MCQ questions
- **Manual Grading**: Teachers can grade essay questions
- **Real-Time Chat**: Both global chat room and one-on-one messaging
- **File-Based Storage**: All data stored in text files (no external database required)

### Technical Highlights
- ✅ **Threading**: Uses `CompletableFuture` for async operations
- ✅ **Thread Safety**: `ReentrantReadWriteLock` for concurrent file access
- ✅ **Networking**: RESTful APIs with Spring Boot
- ✅ **WebSocket**: Real-time bidirectional communication
- ✅ **Session Management**: HTTP session-based authentication
- ✅ **CORS**: Pre-configured for React frontend integration

## 📋 Requirements

- Java 17 or higher
- Maven 3.6+
- Port 8080 available

## 🚀 Getting Started

### 1. Build the Project

```bash
cd exam_backend
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

### 3. Data Storage

All data is stored in the `./data` directory:
- `users.txt` - User accounts
- `exams.txt` - Exam definitions
- `submissions.txt` - Student submissions
- `messages.txt` - Chat messages

## 📚 API Documentation

### Authentication APIs

#### 1. **Signup** (Teacher/Student)
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "email": "john@university.edu",
  "fullName": "John Doe",
  "role": "TEACHER"  // or "STUDENT"
}
```

#### 2. **Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

#### 3. **Logout**
```http
POST /api/auth/logout
```

#### 4. **Get Session**
```http
GET /api/auth/session
```

#### 5. **Get All Teachers**
```http
GET /api/auth/teachers
```

#### 6. **Get All Students** (Teacher only)
```http
GET /api/auth/students
```

---

### Exam Management APIs

#### 1. **Create Exam** (Teacher only)
```http
POST /api/exams
Content-Type: application/json

{
  "title": "Midterm Exam",
  "description": "Computer Science Midterm",
  "durationMinutes": 60,
  "status": "PUBLISHED",
  "questions": [
    {
      "type": "MCQ",
      "questionText": "What is 2+2?",
      "options": ["3", "4", "5", "6"],
      "correctAnswer": "4",
      "marks": 10,
      "orderIndex": 1
    },
    {
      "type": "ESSAY",
      "questionText": "Explain the concept of threading",
      "marks": 20,
      "orderIndex": 2
    }
  ]
}
```

#### 2. **Update Exam** (Teacher only)
```http
PUT /api/exams/{examId}
Content-Type: application/json
```

#### 3. **Delete Exam** (Teacher only)
```http
DELETE /api/exams/{examId}
```

#### 4. **Get My Exams** (Teacher)
```http
GET /api/exams/my-exams
```

#### 5. **Get Published Exams** (Student)
```http
GET /api/exams/published
```

#### 6. **Get Exam by ID**
```http
GET /api/exams/{examId}
```
*Note: Correct answers are hidden for students*

#### 7. **Submit Exam** (Student only)
```http
POST /api/exams/{examId}/submit
Content-Type: application/json

{
  "questionId1": "4",
  "questionId2": "Essay answer text..."
}
```

#### 8. **Grade Essay** (Teacher only)
```http
POST /api/exams/submissions/{submissionId}/grade
Content-Type: application/json

{
  "essayScore": 15
}
```

#### 9. **Get Exam Submissions** (Teacher)
```http
GET /api/exams/{examId}/submissions
```

#### 10. **Get My Submissions** (Student)
```http
GET /api/exams/my-submissions
```

#### 11. **Get Submission Details**
```http
GET /api/exams/submissions/{submissionId}
```

---

### Chat APIs

#### WebSocket Connection
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Subscribe to global chat
    stompClient.subscribe('/topic/global', function(message) {
        console.log('Global message:', JSON.parse(message.body));
    });
    
    // Subscribe to private messages
    stompClient.subscribe('/user/queue/messages', function(message) {
        console.log('Private message:', JSON.parse(message.body));
    });
});

// Send global message
stompClient.send("/app/chat.global", {}, JSON.stringify({
    senderId: "userId",
    senderName: "John Doe",
    senderRole: "TEACHER",
    content: "Hello everyone!"
}));

// Send private message
stompClient.send("/app/chat.private", {}, JSON.stringify({
    senderId: "userId1",
    senderName: "John Doe",
    senderRole: "TEACHER",
    receiverId: "userId2",
    receiverName: "Jane Smith",
    content: "Hi Jane!"
}));
```

#### REST APIs (Alternative to WebSocket)

#### 1. **Send Global Message**
```http
POST /api/chat/global
Content-Type: application/json

{
  "senderId": "userId",
  "senderName": "John Doe",
  "senderRole": "TEACHER",
  "content": "Hello everyone!"
}
```

#### 2. **Send Private Message**
```http
POST /api/chat/private
Content-Type: application/json

{
  "senderId": "userId1",
  "senderName": "John Doe",
  "senderRole": "TEACHER",
  "receiverId": "userId2",
  "receiverName": "Jane Smith",
  "content": "Hi Jane!"
}
```

#### 3. **Get Global Messages**
```http
GET /api/chat/global?limit=50
```

#### 4. **Get Private Conversation**
```http
GET /api/chat/private/{otherUserId}?limit=50
```

#### 5. **Get All Conversations**
```http
GET /api/chat/conversations
```

---

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server Port
server.port=8080

# Data Storage Path
app.data.path=./data

# CORS (for React frontend)
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173

# Session Timeout
server.servlet.session.timeout=30m

# Thread Pool
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
```

## 📦 Project Structure

```
exam_backend/
├── src/main/java/com/university/exam/
│   ├── ExamManagementApplication.java      # Main application
│   ├── config/
│   │   ├── CorsConfig.java                 # CORS configuration
│   │   └── WebSocketConfig.java            # WebSocket configuration
│   ├── controller/
│   │   ├── AuthController.java             # Authentication endpoints
│   │   ├── ExamController.java             # Exam management endpoints
│   │   └── ChatController.java             # Chat endpoints
│   ├── service/
│   │   ├── UserService.java                # User business logic
│   │   ├── ExamService.java                # Exam business logic
│   │   └── ChatService.java                # Chat business logic
│   ├── repository/
│   │   ├── FileRepository.java             # Generic file operations
│   │   ├── UserRepository.java             # User data access
│   │   ├── ExamRepository.java             # Exam data access
│   │   ├── SubmissionRepository.java       # Submission data access
│   │   └── MessageRepository.java          # Message data access
│   └── model/
│       ├── User.java                       # User entity
│       ├── Exam.java                       # Exam entity
│       ├── Question.java                   # Question entity
│       ├── ExamSubmission.java             # Submission entity
│       ├── ChatMessage.java                # Message entity
│       └── ApiResponse.java                # Standard response wrapper
├── src/main/resources/
│   └── application.properties              # Configuration
├── pom.xml                                 # Maven dependencies
└── README.md                               # This file
```

## 🧵 Threading Implementation

### 1. **Async Operations with CompletableFuture**
```java
public CompletableFuture<User> registerUser(User user) {
    return CompletableFuture.supplyAsync(() -> {
        // Thread-safe user registration
        return userRepository.save(user);
    });
}
```

### 2. **Thread-Safe File Access**
```java
private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

public List<T> readAll(String fileName) {
    lock.readLock().lock();
    try {
        // Multiple threads can read simultaneously
        return readFromFile(fileName);
    } finally {
        lock.readLock().unlock();
    }
}

public void writeAll(String fileName, List<T> data) {
    lock.writeLock().lock();
    try {
        // Exclusive write access
        writeToFile(fileName, data);
    } finally {
        lock.writeLock().unlock();
    }
}
```

### 3. **Concurrent Exam Submissions**
Multiple students can submit exams simultaneously without data corruption thanks to thread-safe repository operations.

## 🌐 Networking Features

### 1. **RESTful Architecture**
- Standard HTTP methods (GET, POST, PUT, DELETE)
- JSON request/response format
- Session-based authentication

### 2. **WebSocket Communication**
- Real-time bidirectional messaging
- STOMP protocol over SockJS
- Broadcast and unicast messaging

### 3. **CORS Support**
- Pre-configured for React frontend
- Supports credentials (cookies/sessions)
- Configurable allowed origins

## 🎓 Beginner-Friendly Features

### 1. **Simple File-Based Storage**
- No database setup required
- Human-readable JSON format
- Easy to debug and inspect

### 2. **Clear Code Structure**
- Layered architecture (Controller → Service → Repository)
- Lombok annotations reduce boilerplate
- Comprehensive comments

### 3. **Error Handling**
- Consistent API response format
- Descriptive error messages
- Try-catch blocks with logging

## 🔐 Security Notes

**⚠️ For Production:**
- Hash passwords (use BCrypt)
- Implement JWT tokens instead of sessions
- Add input validation
- Use HTTPS
- Add rate limiting
- Sanitize file paths

## 🧪 Testing the API

### Using cURL

```bash
# Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","password":"pass123","email":"teacher@uni.edu","fullName":"Teacher One","role":"TEACHER"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"teacher1","password":"pass123"}'

# Create Exam (with session cookie)
curl -X POST http://localhost:8080/api/exams \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"title":"Test Exam","description":"Testing","durationMinutes":30,"status":"PUBLISHED","questions":[{"type":"MCQ","questionText":"2+2?","options":["3","4","5"],"correctAnswer":"4","marks":10,"orderIndex":1}]}'
```

### Using Postman
1. Import the API endpoints
2. Enable "Send cookies" in settings
3. Login first to get session cookie
4. Use the cookie for authenticated requests

## 🚧 Future Enhancements

- [ ] Add user profile pictures
- [ ] Implement exam timer with auto-submit
- [ ] Add exam analytics dashboard
- [ ] File upload for essay submissions
- [ ] Email notifications
- [ ] Exam scheduling
- [ ] Question bank management
- [ ] Student performance reports

## 📄 License

This project is created for educational purposes.

## 👥 Contributors

University Project Team

---

**Happy Coding! 🎉**
