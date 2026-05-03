package com.university.exam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "submission_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "question_id"}))
public class SubmissionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonIgnore
    private ExamSubmission submission;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "awarded_marks")
    private Integer awardedMarks;

    public SubmissionAnswer() {}

    public SubmissionAnswer(ExamSubmission submission, Long questionId, String answer, Integer awardedMarks) {
        this.submission = submission;
        this.questionId = questionId;
        this.answer = answer;
        this.awardedMarks = awardedMarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExamSubmission getSubmission() { return submission; }
    public void setSubmission(ExamSubmission submission) { this.submission = submission; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Integer getAwardedMarks() { return awardedMarks; }
    public void setAwardedMarks(Integer awardedMarks) { this.awardedMarks = awardedMarks; }
}
