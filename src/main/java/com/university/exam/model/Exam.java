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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String course;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "teacher_name")
    private String teacherName;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("questionOrder ASC")
    private List<Question> questions = new ArrayList<>();

    @Column(name = "total_marks")
    private int totalMarks;

    @Column(name = "passing_marks")
    private int passingMarks;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "start_date_time")
    private Long startDateTime;

    @Column(name = "end_date_time")
    private Long endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type")
    private ExamType examType;

    @Column(name = "created_at")
    private long createdAt;

    @Column(name = "updated_at")
    private long updatedAt;

    @Enumerated(EnumType.STRING)
    private ExamStatus status;

    public enum ExamStatus {
        DRAFT, PUBLISHED, ACTIVE, COMPLETED, ARCHIVED
    }

    public enum ExamType {
        MCQ, CQ
    }

    public Exam() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        this.questions.clear();
        if (questions != null) {
            for (Question q : questions) {
                q.setExam(this);
                this.questions.add(q);
            }
        }
    }

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
