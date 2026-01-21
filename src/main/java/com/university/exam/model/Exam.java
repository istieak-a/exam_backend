package com.university.exam.model;

import java.util.List;

public class Exam {
    private String id;
    private String title;
    private String description;
    private String teacherId;
    private String teacherName;
    private List<Question> questions;
    private int totalMarks;
    private int durationMinutes;
    private long createdAt;
    private ExamStatus status;
    
    public enum ExamStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
    
    public Exam() {}
    
    public Exam(String id, String title, String description, String teacherId,
                String teacherName, List<Question> questions, int totalMarks,
                int durationMinutes, long createdAt, ExamStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.questions = questions;
        this.totalMarks = totalMarks;
        this.durationMinutes = durationMinutes;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }
}
