package com.university.exam.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.university.exam.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class UserRepository {
    
    private static final String USERS_FILE = "users.txt";
    private final FileRepository<User> fileRepository;
    
    public UserRepository(FileRepository<User> fileRepository) {
        this.fileRepository = fileRepository;
    }
    
    public List<User> findAll() {
        return fileRepository.readAll(USERS_FILE, new TypeReference<List<User>>() {});
    }
    
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }
    
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
    
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
    
    public List<User> findByRole(User.UserRole role) {
        return findAll().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }
    
    public User save(User user) {
        List<User> users = findAll();
        
        if (user.getId() == null || user.getId().isEmpty()) {
            // New user
            user.setId(UUID.randomUUID().toString());
            user.setCreatedAt(System.currentTimeMillis());
            users.add(user);
        } else {
            // Update existing user
            users = users.stream()
                    .map(u -> u.getId().equals(user.getId()) ? user : u)
                    .collect(Collectors.toList());
        }
        
        fileRepository.writeAll(USERS_FILE, users);
        return user;
    }
    
    public void delete(String id) {
        List<User> users = findAll().stream()
                .filter(user -> !user.getId().equals(id))
                .collect(Collectors.toList());
        fileRepository.writeAll(USERS_FILE, users);
    }
    
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
    
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
