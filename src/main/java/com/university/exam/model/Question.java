package com.university.exam.model;

import java.util.List;

public class Question {
    private String id;
    private String examId;
    private QuestionType type;
    private String questionText;
    private List<String> options; // For MCQ
    private String correctAnswer; // For MCQ (option index or text)
    private int marks;
    private int questionOrder;
    
    public enum QuestionType {
        MCQ, CQ
    }
    
    public Question() {}
    
    public Question(String id, String examId, QuestionType type, String questionText,
                    List<String> options, String correctAnswer, int marks, int questionOrder) {
        this.id = id;
        this.examId = examId;
        this.type = type;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.questionOrder = questionOrder;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
    
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
    
    public int getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }
}
