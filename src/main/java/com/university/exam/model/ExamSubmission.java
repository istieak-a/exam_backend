package com.university.exam.model;

import java.util.Map;

public class ExamSubmission {
    private String id;
    private String examId;
    private String studentId;
    private String studentName;
    private Map<String, String> answers; // questionId -> answer
    private int mcqScore;
    private Integer essayScore; // Nullable, set by teacher
    private int totalScore;
    private long submittedAt;
    private SubmissionStatus status;
    
    public enum SubmissionStatus {
        SUBMITTED, GRADED_MCQ, FULLY_GRADED
    }
    
    public ExamSubmission() {}
    
    public ExamSubmission(String id, String examId, String studentId, String studentName,
                          Map<String, String> answers, int mcqScore, Integer essayScore,
                          int totalScore, long submittedAt, SubmissionStatus status) {
        this.id = id;
        this.examId = examId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.answers = answers;
        this.mcqScore = mcqScore;
        this.essayScore = essayScore;
        this.totalScore = totalScore;
        this.submittedAt = submittedAt;
        this.status = status;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public Map<String, String> getAnswers() { return answers; }
    public void setAnswers(Map<String, String> answers) { this.answers = answers; }
    
    public int getMcqScore() { return mcqScore; }
    public void setMcqScore(int mcqScore) { this.mcqScore = mcqScore; }
    
    public Integer getEssayScore() { return essayScore; }
    public void setEssayScore(Integer essayScore) { this.essayScore = essayScore; }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public long getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(long submittedAt) { this.submittedAt = submittedAt; }
    
    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }
}
