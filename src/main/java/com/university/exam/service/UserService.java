package com.university.exam.service;

import com.university.exam.model.User;
import com.university.exam.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Register a new user (Thread-safe operation)
     */
    public CompletableFuture<User> registerUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            // Validate username uniqueness
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new RuntimeException("Username already exists");
            }

            // Validate email uniqueness
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            // Hash password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            return userRepository.save(user);
        });
    }

    /**
     * Authenticate user (Thread-safe operation)
     */
    public CompletableFuture<Optional<User>> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Verify password using BCrypt
                if (passwordEncoder.matches(password, user.getPassword())) {
                    return Optional.of(user);
                }
            }

            return Optional.empty();
        });
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAllTeachers() {
        return userRepository.findByRole(User.UserRole.TEACHER);
    }

    public List<User> findAllStudents() {
        return userRepository.findByRole(User.UserRole.STUDENT);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Update user profile (Thread-safe operation)
     */
    public CompletableFuture<User> updateProfile(String userId, Map<String, String> profileData) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            // Update allowed fields
            if (profileData.containsKey("fullName")) {
                user.setFullName(profileData.get("fullName"));
            }
            if (profileData.containsKey("email")) {
                String newEmail = profileData.get("email");
                // Check if email is already taken by another user
                Optional<User> existingUser = userRepository.findByEmail(newEmail);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    throw new RuntimeException("Email already taken");
                }
                user.setEmail(newEmail);
            }
            if (profileData.containsKey("password")) {
                // Hash new password
                user.setPassword(passwordEncoder.encode(profileData.get("password")));
            }

            return userRepository.save(user);
        });
    }

    /**
     * Change user password (Thread-safe operation)
     */
    public CompletableFuture<Boolean> changePassword(String userId, String currentPassword, String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Incorrect current password");
            }

            // Update with new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return true;
        });
    }
}
