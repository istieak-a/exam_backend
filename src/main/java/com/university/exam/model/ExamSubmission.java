package com.university.exam.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "exam_submissions")
public class ExamSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "exam_title")
    private String examTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type")
    private Exam.ExamType examType;

    @Column(name = "max_score")
    private int maxScore;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name")
    private String studentName;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SubmissionAnswer> submissionAnswers = new ArrayList<>();

    @Column(name = "mcq_score")
    private int mcqScore;

    @Column(name = "essay_score")
    private Integer essayScore;

    @Column(name = "total_score")
    private int totalScore;

    @Column(name = "submitted_at")
    private long submittedAt;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    @Column(name = "tab_switch_count")
    private Integer tabSwitchCount;

    @Column(name = "focus_loss_count")
    private Integer focusLossCount;

    @Column(name = "violation_terminated")
    private Boolean violationTerminated;

    @Column(name = "camera_violation_count")
    private Integer cameraViolationCount;

    @Column(name = "camera_terminated")
    private Boolean cameraTerminated;

    @Column(name = "proctoring_video_path")
    private String proctoringVideoPath;

    public enum SubmissionStatus {
        SUBMITTED, GRADED_MCQ, FULLY_GRADED
    }

    public ExamSubmission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public Exam.ExamType getExamType() { return examType; }
    public void setExamType(Exam.ExamType examType) { this.examType = examType; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public List<SubmissionAnswer> getSubmissionAnswers() { return submissionAnswers; }
    public void setSubmissionAnswers(List<SubmissionAnswer> submissionAnswers) {
        if (this.submissionAnswers == null) {
            this.submissionAnswers = new ArrayList<>();
        }
        this.submissionAnswers.clear();
        if (submissionAnswers != null) {
            for (SubmissionAnswer sa : submissionAnswers) {
                sa.setSubmission(this);
                this.submissionAnswers.add(sa);
            }
        }
    }

    /**
     * Frontend contract preserved: answers map of questionId(string) -> answer.
     */
    @Transient
    public Map<String, String> getAnswers() {
        if (submissionAnswers == null) return new LinkedHashMap<>();
        Map<String, String> result = new LinkedHashMap<>();
        for (SubmissionAnswer sa : submissionAnswers) {
            if (sa.getQuestionId() != null) {
                result.put(sa.getQuestionId().toString(), sa.getAnswer());
            }
        }
        return result;
    }

    public void setAnswers(Map<String, String> answers) {
        if (this.submissionAnswers == null) {
            this.submissionAnswers = new ArrayList<>();
        }
        // Merge into existing rows (keep awardedMarks already assigned)
        Map<Long, SubmissionAnswer> existing = new HashMap<>();
        for (SubmissionAnswer sa : this.submissionAnswers) {
            if (sa.getQuestionId() != null) {
                existing.put(sa.getQuestionId(), sa);
            }
        }
        this.submissionAnswers.clear();
        if (answers == null) {
            return;
        }
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            Long qid = parseLongOrNull(entry.getKey());
            if (qid == null) continue;
            SubmissionAnswer row = existing.get(qid);
            if (row == null) {
                row = new SubmissionAnswer();
                row.setQuestionId(qid);
            }
            row.setSubmission(this);
            row.setAnswer(entry.getValue());
            this.submissionAnswers.add(row);
        }
    }

    @Transient
    public Map<String, Integer> getQuestionGrades() {
        if (submissionAnswers == null) return new LinkedHashMap<>();
        Map<String, Integer> result = new LinkedHashMap<>();
        for (SubmissionAnswer sa : submissionAnswers) {
            if (sa.getQuestionId() != null && sa.getAwardedMarks() != null) {
                result.put(sa.getQuestionId().toString(), sa.getAwardedMarks());
            }
        }
        return result;
    }

    public void setQuestionGrades(Map<String, Integer> questionGrades) {
        if (this.submissionAnswers == null || questionGrades == null) {
            return;
        }
        Map<Long, SubmissionAnswer> byId = new HashMap<>();
        for (SubmissionAnswer sa : this.submissionAnswers) {
            if (sa.getQuestionId() != null) {
                byId.put(sa.getQuestionId(), sa);
            }
        }
        for (Map.Entry<String, Integer> entry : questionGrades.entrySet()) {
            Long qid = parseLongOrNull(entry.getKey());
            if (qid == null) continue;
            SubmissionAnswer row = byId.get(qid);
            if (row == null) {
                row = new SubmissionAnswer();
                row.setSubmission(this);
                row.setQuestionId(qid);
                this.submissionAnswers.add(row);
                byId.put(qid, row);
            }
            row.setAwardedMarks(entry.getValue());
        }
    }

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

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public int getTabSwitchCount() { return tabSwitchCount != null ? tabSwitchCount : 0; }
    public void setTabSwitchCount(int tabSwitchCount) { this.tabSwitchCount = tabSwitchCount; }

    public int getFocusLossCount() { return focusLossCount != null ? focusLossCount : 0; }
    public void setFocusLossCount(int focusLossCount) { this.focusLossCount = focusLossCount; }

    public boolean isViolationTerminated() { return Boolean.TRUE.equals(violationTerminated); }
    public void setViolationTerminated(boolean violationTerminated) { this.violationTerminated = violationTerminated; }

    public int getCameraViolationCount() { return cameraViolationCount != null ? cameraViolationCount : 0; }
    public void setCameraViolationCount(int cameraViolationCount) { this.cameraViolationCount = cameraViolationCount; }

    public boolean isCameraTerminated() { return Boolean.TRUE.equals(cameraTerminated); }
    public void setCameraTerminated(boolean cameraTerminated) { this.cameraTerminated = cameraTerminated; }

    public String getProctoringVideoPath() { return proctoringVideoPath; }
    public void setProctoringVideoPath(String proctoringVideoPath) { this.proctoringVideoPath = proctoringVideoPath; }

    @Transient
    public boolean isProctoringFlagged() {
        return isCameraTerminated() || getCameraViolationCount() > 0;
    }

    private static Long parseLongOrNull(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
