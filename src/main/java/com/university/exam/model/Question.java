package com.university.exam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    @JsonIgnore
    private Exam exam;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("optionOrder ASC")
    @JsonIgnore
    private List<MCQOption> mcqOptions = new ArrayList<>();

    @Column(name = "correct_answer")
    private String correctAnswer;

    private int marks;

    @Column(name = "question_order")
    private int questionOrder;

    public enum QuestionType {
        MCQ, CQ
    }

    public Question() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    /**
     * JSON-friendly view of the parent exam's id, kept for frontend compatibility.
     */
    @Transient
    public Long getExamId() {
        return exam != null ? exam.getId() : null;
    }

    public void setExamId(Long examId) {
        // No-op: relationship is managed via setExam(...). Kept for JSON deserialization compatibility.
    }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<MCQOption> getMcqOptions() { return mcqOptions; }
    public void setMcqOptions(List<MCQOption> mcqOptions) {
        if (this.mcqOptions == null) {
            this.mcqOptions = new ArrayList<>();
        }
        this.mcqOptions.clear();
        if (mcqOptions != null) {
            for (MCQOption opt : mcqOptions) {
                opt.setQuestion(this);
                this.mcqOptions.add(opt);
            }
        }
    }

    /**
     * Frontend contract: questions expose a plain `options: string[]`.
     */
    @Transient
    public List<String> getOptions() {
        if (mcqOptions == null || mcqOptions.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList<>(mcqOptions.size());
        for (MCQOption opt : mcqOptions) {
            result.add(opt.getOptionText());
        }
        return result;
    }

    public void setOptions(List<String> options) {
        if (this.mcqOptions == null) {
            this.mcqOptions = new ArrayList<>();
        }
        this.mcqOptions.clear();
        if (options == null) {
            return;
        }
        for (int i = 0; i < options.size(); i++) {
            String text = options.get(i);
            MCQOption opt = new MCQOption();
            opt.setQuestion(this);
            opt.setOptionText(text);
            opt.setOptionOrder(i + 1);
            opt.setCorrect(text != null && correctAnswer != null
                    && text.trim().equalsIgnoreCase(correctAnswer.trim()));
            this.mcqOptions.add(opt);
        }
    }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
        if (mcqOptions != null && correctAnswer != null) {
            for (MCQOption opt : mcqOptions) {
                opt.setCorrect(opt.getOptionText() != null
                        && opt.getOptionText().trim().equalsIgnoreCase(correctAnswer.trim()));
            }
        }
    }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public int getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }
}
