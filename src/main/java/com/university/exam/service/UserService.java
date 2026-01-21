package com.university.exam.service;

import com.university.exam.model.User;
import com.university.exam.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            
            // Save user (password should be hashed in production)
            return userRepository.save(user);
        });
    }
    
    /**
     * Authenticate user (Thread-safe operation)
     */
    public CompletableFuture<Optional<User>> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // In production, use BCrypt or similar for password verification
                if (user.getPassword().equals(password)) {
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
}
