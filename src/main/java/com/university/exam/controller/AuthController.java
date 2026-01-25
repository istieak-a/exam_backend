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
public class AuthController {

        private final UserService userService;

        public AuthController(UserService userService) {
                this.userService = userService;
        }

        /**
         * User Signup (Teacher or Student)
         */
        @PostMapping("/signup")
        public CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> signup(
                        @RequestBody User user,
                        HttpSession session) {

                return userService.registerUser(user)
                                .thenApply(savedUser -> {
                                        User userWithoutPassword = savedUser.withoutPassword();

                                        // Create session immediately after signup for smoother UX
                                        session.setAttribute("userId", userWithoutPassword.getId());
                                        session.setAttribute("username", userWithoutPassword.getUsername());
                                        session.setAttribute("role", userWithoutPassword.getRole().toString());

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("user", userWithoutPassword);
                                        data.put("sessionId", session.getId());

                                        return ResponseEntity.status(HttpStatus.CREATED).body(
                                                        ApiResponse.success("User registered successfully", data));
                                })
                                .exceptionally(ex -> {
                                        return ResponseEntity.badRequest().body(
                                                        ApiResponse.error(ex.getMessage()));
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
                                                                ApiResponse.success("Login successful", data));
                                        } else {
                                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                                                ApiResponse.error("Invalid username or password"));
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
        @GetMapping({ "/session", "/me" })
        public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(HttpSession session) {
                String userId = (String) session.getAttribute("userId");

                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ApiResponse.error("No active session"));
                }

                Optional<User> userOpt = userService.findById(userId);
                if (userOpt.isPresent()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("user", userOpt.get().withoutPassword());
                        data.put("sessionId", session.getId());

                        return ResponseEntity.ok(
                                        ApiResponse.success("Session active", data));
                } else {
                        session.invalidate();
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ApiResponse.error("Invalid session"));
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
                                ApiResponse.success("Teachers retrieved", teachersWithoutPassword));
        }

        /**
         * Get all students (for teacher to view)
         */
        @GetMapping("/students")
        public ResponseEntity<ApiResponse<List<User>>> getAllStudents(HttpSession session) {
                String role = (String) session.getAttribute("role");

                if (!"TEACHER".equals(role)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                        ApiResponse.error("Access denied: Teachers only"));
                }

                List<User> students = userService.findAllStudents();
                List<User> studentsWithoutPassword = students.stream()
                                .map(User::withoutPassword)
                                .toList();

                return ResponseEntity.ok(
                                ApiResponse.success("Students retrieved", studentsWithoutPassword));
        }

        /**
         * Change password
         */
        @PostMapping("/change-password")
        public CompletableFuture<ResponseEntity<ApiResponse<Object>>> changePassword(
                        @RequestBody Map<String, String> passwordData,
                        HttpSession session) {

                String userId = (String) session.getAttribute("userId");
                String currentPassword = passwordData.get("currentPassword");
                String newPassword = passwordData.get("newPassword");

                if (userId == null) {
                        return CompletableFuture.completedFuture(
                                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                                        ApiResponse.error("Not authenticated")));
                }

                if (currentPassword == null || newPassword == null) {
                        return CompletableFuture.completedFuture(
                                        ResponseEntity.badRequest().body(
                                                        ApiResponse.error("Current and new passwords are required")));
                }

                return userService.changePassword(userId, currentPassword, newPassword)
                                .thenApply(success -> ResponseEntity.ok(
                                                ApiResponse.success("Password changed successfully", null)))
                                .exceptionally(ex -> ResponseEntity.badRequest().body(
                                                ApiResponse.error(ex.getCause().getMessage())));
        }

        /**
         * Update user profile
         */
        @PutMapping("/profile")
        public CompletableFuture<ResponseEntity<ApiResponse<User>>> updateProfile(
                        @RequestBody Map<String, String> profileData,
                        HttpSession session) {

                String userId = (String) session.getAttribute("userId");

                if (userId == null) {
                        return CompletableFuture.completedFuture(
                                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                                        ApiResponse.error("Not authenticated")));
                }

                return userService.updateProfile(userId, profileData)
                                .thenApply(updatedUser -> ResponseEntity.ok(
                                                ApiResponse.success("Profile updated successfully",
                                                                updatedUser.withoutPassword())))
                                .exceptionally(ex -> ResponseEntity.badRequest().body(
                                                ApiResponse.error(ex.getMessage())));
        }
}
