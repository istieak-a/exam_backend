package com.university.exam.controller;

import com.university.exam.model.ApiResponse;
import com.university.exam.model.User;
import com.university.exam.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * User Signup (Teacher or Student)
     */
    @PostMapping("/signup")
    public CompletableFuture<ResponseEntity<ApiResponse<User>>> signup(
            @RequestBody User user) {
        
        return userService.registerUser(user)
                .thenApply(savedUser -> {
                    User userWithoutPassword = savedUser.withoutPassword();
                    return ResponseEntity.ok(
                        ApiResponse.success("User registered successfully", userWithoutPassword)
                    );
                })
                .exceptionally(ex -> {
                    return ResponseEntity.badRequest().body(
                        ApiResponse.error(ex.getMessage())
                    );
                });
    }
    
    /**
     * User Login
     */
    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> login(
            @RequestBody Map<String, String> credentials,
            HttpSession session) {
        
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        return userService.login(username, password)
                .thenApply(userOpt -> {
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        // Store user in session
                        session.setAttribute("userId", user.getId());
                        session.setAttribute("username", user.getUsername());
                        session.setAttribute("role", user.getRole().toString());
                        
                        Map<String, Object> data = new HashMap<>();
                        data.put("user", user.withoutPassword());
                        data.put("sessionId", session.getId());
                        
                        return ResponseEntity.ok(
                            ApiResponse.success("Login successful", data)
                        );
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                            ApiResponse.error("Invalid username or password")
                        );
                    }
                });
    }
    
    /**
     * Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
    
    /**
     * Get current user session
     */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("No active session")
            );
        }
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            Map<String, Object> data = new HashMap<>();
            data.put("user", userOpt.get().withoutPassword());
            data.put("sessionId", session.getId());
            
            return ResponseEntity.ok(
                ApiResponse.success("Session active", data)
            );
        } else {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Invalid session")
            );
        }
    }
    
    /**
     * Get all teachers (for student to view)
     */
    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<User>>> getAllTeachers() {
        List<User> teachers = userService.findAllTeachers();
        List<User> teachersWithoutPassword = teachers.stream()
                .map(User::withoutPassword)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("Teachers retrieved", teachersWithoutPassword)
        );
    }
    
    /**
     * Get all students (for teacher to view)
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getAllStudents(HttpSession session) {
        String role = (String) session.getAttribute("role");
        
        if (!"TEACHER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Access denied: Teachers only")
            );
        }
        
        List<User> students = userService.findAllStudents();
        List<User> studentsWithoutPassword = students.stream()
                .map(User::withoutPassword)
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success("Students retrieved", studentsWithoutPassword)
        );
    }
}
