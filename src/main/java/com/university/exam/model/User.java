package com.university.exam.model;

public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole role;
    private long createdAt;
    
    public enum UserRole {
        TEACHER, STUDENT
    }
    
    public User() {}
    
    public User(String id, String username, String password, String email, 
                String fullName, UserRole role, long createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    // For response without password
    public User withoutPassword() {
        User copy = new User();
        copy.id = this.id;
        copy.username = this.username;
        copy.email = this.email;
        copy.fullName = this.fullName;
        copy.role = this.role;
        copy.createdAt = this.createdAt;
        return copy;
    }
}
