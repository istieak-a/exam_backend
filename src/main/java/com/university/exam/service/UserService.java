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

    public CompletableFuture<User> registerUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getCreatedAt() == 0L) {
                user.setCreatedAt(System.currentTimeMillis());
            }

            return userRepository.save(user);
        });
    }

    public CompletableFuture<Optional<User>> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (passwordEncoder.matches(password, user.getPassword())) {
                    return Optional.of(user);
                }
            }

            return Optional.empty();
        });
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
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

    public CompletableFuture<User> updateProfile(Long userId, Map<String, String> profileData) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            if (profileData.containsKey("fullName")) {
                user.setFullName(profileData.get("fullName"));
            }
            if (profileData.containsKey("email")) {
                String newEmail = profileData.get("email");
                Optional<User> existingUser = userRepository.findByEmailIgnoreCase(newEmail);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    throw new RuntimeException("Email already taken");
                }
                user.setEmail(newEmail);
            }
            if (profileData.containsKey("password")) {
                user.setPassword(passwordEncoder.encode(profileData.get("password")));
            }

            return userRepository.save(user);
        });
    }

    public CompletableFuture<Boolean> changePassword(Long userId, String currentPassword, String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Incorrect current password");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return true;
        });
    }
}
