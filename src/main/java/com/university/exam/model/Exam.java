package com.university.exam.model;

import java.util.List;

public class Exam {
    private String id;
    private String title;
    private String course;
    private String description;
    private String teacherId;
    private String teacherName;
    private List<Question> questions;
    private int totalMarks;
    private int passingMarks;
    private int durationMinutes;
    private Long startDateTime;
    private Long endDateTime;
    private ExamType examType;
    private long createdAt;
    private long updatedAt;
    private ExamStatus status;
    
    public enum ExamStatus {
        DRAFT, PUBLISHED, ACTIVE, COMPLETED, ARCHIVED
    }
    
    public enum ExamType {
        MCQ, CQ
    }
    
    public Exam() {}
    
    public Exam(String id, String title, String course, String description, String teacherId,
                String teacherName, List<Question> questions, int totalMarks, int passingMarks,
                int durationMinutes, Long startDateTime, Long endDateTime, ExamType examType,
                long createdAt, long updatedAt, ExamStatus status) {
        this.id = id;
        this.title = title;
        this.course = course;
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.questions = questions;
        this.totalMarks = totalMarks;
        this.passingMarks = passingMarks;
        this.durationMinutes = durationMinutes;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.examType = examType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    
    public int getPassingMarks() { return passingMarks; }
    public void setPassingMarks(int passingMarks) { this.passingMarks = passingMarks; }
    
    public Long getStartDateTime() { return startDateTime; }
    public void setStartDateTime(Long startDateTime) { this.startDateTime = startDateTime; }
    
    public Long getEndDateTime() { return endDateTime; }
    public void setEndDateTime(Long endDateTime) { this.endDateTime = endDateTime; }
    
    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
