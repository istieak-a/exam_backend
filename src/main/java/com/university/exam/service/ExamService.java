package com.university.exam.service;

import com.university.exam.model.Exam;
import com.university.exam.model.ExamSubmission;
import com.university.exam.model.Question;
import com.university.exam.model.User;
import com.university.exam.repository.ExamRepository;
import com.university.exam.repository.SubmissionRepository;
import com.university.exam.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ExamService {
    
    private final ExamRepository examRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    
    public ExamService(ExamRepository examRepository, 
                       SubmissionRepository submissionRepository,
                       UserRepository userRepository) {
        this.examRepository = examRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Validate exam fields and business rules
     */
    private void validateExam(Exam exam) {
        Map<String, String> errors = new HashMap<>();
        
        // Required fields
        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            errors.put("title", "Title is required");
        }
        if (exam.getCourse() == null || exam.getCourse().trim().isEmpty()) {
            errors.put("course", "Course is required");
        }
        if (exam.getExamType() == null) {
            errors.put("examType", "Exam type is required (MCQ or CQ)");
        }
        if (exam.getStartDateTime() == null) {
            errors.put("startDateTime", "Start date/time is required");
        }
        if (exam.getEndDateTime() == null) {
            errors.put("endDateTime", "End date/time is required");
        }
        if (exam.getQuestions() == null || exam.getQuestions().isEmpty()) {
            errors.put("questions", "At least one question is required");
        }
        
        // Marks validation
        if (exam.getTotalMarks() <= 0) {
            errors.put("totalMarks", "Total marks must be greater than 0");
        }
        if (exam.getPassingMarks() <= 0) {
            errors.put("passingMarks", "Passing marks must be greater than 0");
        }
        if (exam.getPassingMarks() > exam.getTotalMarks()) {
            errors.put("passingMarks", "Passing marks cannot exceed total marks");
        }
        
        // Duration validation
        if (exam.getDurationMinutes() <= 0) {
            errors.put("durationMinutes", "Duration must be greater than 0");
        }
        
        // DateTime validation
        if (exam.getStartDateTime() != null && exam.getEndDateTime() != null) {
            if (exam.getStartDateTime() >= exam.getEndDateTime()) {
                errors.put("endDateTime", "End date/time must be after start date/time");
            }
        }
        
        // Questions validation
        if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
            int calculatedTotal = exam.getQuestions().stream()
                    .mapToInt(Question::getMarks)
                    .sum();
            
            if (calculatedTotal != exam.getTotalMarks()) {
                errors.put("totalMarks", "Total marks (" + exam.getTotalMarks() + 
                    ") must equal sum of question marks (" + calculatedTotal + ")");
            }
            
            // Validate individual questions
            for (int i = 0; i < exam.getQuestions().size(); i++) {
                Question q = exam.getQuestions().get(i);
                if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) {
                    errors.put("questions[" + i + "].text", "Question text is required");
                }
                if (q.getMarks() <= 0) {
                    errors.put("questions[" + i + "].marks", "Question marks must be greater than 0");
                }
                
                // MCQ specific validation
                if (q.getType() == Question.QuestionType.MCQ) {
                    if (q.getOptions() == null || q.getOptions().size() < 2) {
                        errors.put("questions[" + i + "].options", "MCQ must have at least 2 options");
                    }
                    if (q.getCorrectAnswer() == null || q.getCorrectAnswer().trim().isEmpty()) {
                        errors.put("questions[" + i + "].correctAnswer", "MCQ must have a correct answer");
                    }
                }
                
                // CQ should not have options
                if (q.getType() == Question.QuestionType.CQ) {
                    if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                        errors.put("questions[" + i + "].options", "CQ questions should not have options");
                    }
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }
    
    /**
     * Calculate current status based on datetime
     */
    private Exam.ExamStatus calculateCurrentStatus(Exam exam) {
        if (exam.getStartDateTime() == null || exam.getEndDateTime() == null) {
            return exam.getStatus();
        }
        
        long now = System.currentTimeMillis();
        
        if (exam.getStatus() == Exam.ExamStatus.DRAFT) {
            return Exam.ExamStatus.DRAFT;
        }
        
        if (exam.getStatus() == Exam.ExamStatus.PUBLISHED || exam.getStatus() == Exam.ExamStatus.ACTIVE) {
            if (now >= exam.getStartDateTime() && now <= exam.getEndDateTime()) {
                return Exam.ExamStatus.ACTIVE;
            } else if (now > exam.getEndDateTime()) {
                return Exam.ExamStatus.COMPLETED;
            }
        }
        
        return exam.getStatus();
    }
    
    /**
     * Update exam status if needed
     */
    private Exam updateExamStatus(Exam exam) {
        Exam.ExamStatus newStatus = calculateCurrentStatus(exam);
        if (newStatus != exam.getStatus()) {
            exam.setStatus(newStatus);
            return examRepository.save(exam);
        }
        return exam;
    }
    
    /**
     * Create a new exam (Teacher only)
     */
    public CompletableFuture<Exam> createExam(Exam exam, String teacherId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> teacher = userRepository.findById(teacherId);
            if (teacher.isEmpty() || teacher.get().getRole() != User.UserRole.TEACHER) {
                throw new RuntimeException("Only teachers can create exams");
            }
            
            // Validate exam
            validateExam(exam);
            
            exam.setTeacherId(teacherId);
            exam.setTeacherName(teacher.get().getFullName());
            
            // Set question order if not set
            if (exam.getQuestions() != null) {
                for (int i = 0; i < exam.getQuestions().size(); i++) {
                    if (exam.getQuestions().get(i).getQuestionOrder() == 0) {
                        exam.getQuestions().get(i).setQuestionOrder(i + 1);
                    }
                }
            }
            
            return examRepository.save(exam);
        });
    }
    
    /**
     * Update exam
     */
    public CompletableFuture<Exam> updateExam(String examId, Exam updatedExam, String teacherId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Exam> existingExam = examRepository.findById(examId);
            
            if (existingExam.isEmpty()) {
                throw new RuntimeException("Exam not found");
            }
            
            if (!existingExam.get().getTeacherId().equals(teacherId)) {
                throw new RuntimeException("Unauthorized: You can only update your own exams");
            }
            
            // Check if exam has submissions
            if (submissionRepository.hasSubmissions(examId)) {
                throw new RuntimeException("Cannot update exam: Students have already submitted");
            }
            
            // Validate updated exam
            validateExam(updatedExam);
            
            updatedExam.setId(examId);
            updatedExam.setTeacherId(teacherId);
            updatedExam.setTeacherName(existingExam.get().getTeacherName());
            updatedExam.setCreatedAt(existingExam.get().getCreatedAt());
            
            // Set question order if not set
            if (updatedExam.getQuestions() != null) {
                for (int i = 0; i < updatedExam.getQuestions().size(); i++) {
                    if (updatedExam.getQuestions().get(i).getQuestionOrder() == 0) {
                        updatedExam.getQuestions().get(i).setQuestionOrder(i + 1);
                    }
                }
            }
            
            return examRepository.save(updatedExam);
        });
    }
    
    /**
     * Delete exam
     */
    public CompletableFuture<Void> deleteExam(String examId, String teacherId) {
        return CompletableFuture.runAsync(() -> {
            Optional<Exam> exam = examRepository.findById(examId);
            
            if (exam.isEmpty()) {
                throw new RuntimeException("Exam not found");
            }
            
            if (!exam.get().getTeacherId().equals(teacherId)) {
                throw new RuntimeException("Unauthorized: You can only delete your own exams");
            }
            
            // Check if exam has submissions
            if (submissionRepository.hasSubmissions(examId)) {
                throw new RuntimeException("Cannot delete exam: Students have already submitted");
            }
            
            examRepository.delete(examId);
        });
    }
    
    /**
     * Get all published exams (for students)
     */
    public List<Exam> getPublishedExams() {
        List<Exam> exams = examRepository.findPublished();
        // Update status for each exam
        return exams.stream()
                .map(this::updateExamStatus)
                .toList();
    }
    
    /**
     * Get teacher's exams
     */
    public List<Exam> getTeacherExams(String teacherId) {
        List<Exam> exams = examRepository.findByTeacherId(teacherId);
        // Update status for each exam
        return exams.stream()
                .map(this::updateExamStatus)
                .toList();
    }
    
    /**
     * Get exam by ID (sanitize for students - hide correct answers)
     */
    public Optional<Exam> getExamById(String examId, boolean isTeacher) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        
        if (examOpt.isEmpty()) {
            return examOpt;
        }
        
        Exam exam = updateExamStatus(examOpt.get());
        
        if (!isTeacher) {
            // Hide correct answers for students
            List<Question> sanitizedQuestions = exam.getQuestions().stream()
                    .map(q -> {
                        Question copy = new Question();
                        copy.setId(q.getId());
                        copy.setExamId(q.getExamId());
                        copy.setType(q.getType());
                        copy.setQuestionText(q.getQuestionText());
                        copy.setOptions(q.getOptions());
                        copy.setMarks(q.getMarks());
                        copy.setQuestionOrder(q.getQuestionOrder());
                        // Don't include correctAnswer
                        return copy;
                    })
                    .toList();
            
            exam.setQuestions(sanitizedQuestions);
        }
        
        return Optional.of(exam);
    }
    
    /**
     * Submit exam with auto-grading for MCQ questions
     */
    public CompletableFuture<ExamSubmission> submitExam(
            String examId, String studentId, Map<String, String> answers) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Check if already submitted
            Optional<ExamSubmission> existing = 
                submissionRepository.findByExamAndStudent(examId, studentId);
            if (existing.isPresent()) {
                throw new RuntimeException("You have already submitted this exam");
            }
            
            Optional<Exam> examOpt = examRepository.findById(examId);
            if (examOpt.isEmpty()) {
                throw new RuntimeException("Exam not found");
            }
            
            Exam exam = examOpt.get();
            
            // Validate submission timing
            long now = System.currentTimeMillis();
            if (exam.getStartDateTime() != null && now < exam.getStartDateTime()) {
                throw new RuntimeException("Exam has not started yet");
            }
            if (exam.getEndDateTime() != null && now > exam.getEndDateTime()) {
                throw new RuntimeException("Exam has ended");
            }
            
            Optional<User> studentOpt = userRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                throw new RuntimeException("Student not found");
            }
            
            User student = studentOpt.get();
            
            // Auto-grade MCQ questions
            int mcqScore = 0;
            boolean hasCQ = false;
            
            for (Question question : exam.getQuestions()) {
                if (question.getType() == Question.QuestionType.MCQ) {
                    String studentAnswer = answers.get(question.getId());
                    if (studentAnswer != null && 
                        studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                        mcqScore += question.getMarks();
                    }
                } else {
                    hasCQ = true;
                }
            }
            
            ExamSubmission submission = new ExamSubmission();
            submission.setExamId(examId);
            submission.setStudentId(studentId);
            submission.setStudentName(student.getFullName());
            submission.setAnswers(answers);
            submission.setMcqScore(mcqScore);
            
            if (hasCQ) {
                submission.setStatus(ExamSubmission.SubmissionStatus.GRADED_MCQ);
                submission.setTotalScore(mcqScore); // Partial score
            } else {
                submission.setStatus(ExamSubmission.SubmissionStatus.FULLY_GRADED);
                submission.setTotalScore(mcqScore);
            }
            
            return submissionRepository.save(submission);
        });
    }
    
    /**
     * Grade essay questions (Teacher only)
     */
    public CompletableFuture<ExamSubmission> gradeEssay(
            String submissionId, int essayScore, String teacherId) {
        
        return CompletableFuture.supplyAsync(() -> {
            Optional<ExamSubmission> submissionOpt = submissionRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                throw new RuntimeException("Submission not found");
            }
            
            ExamSubmission submission = submissionOpt.get();
            
            // Verify teacher owns the exam
            Optional<Exam> examOpt = examRepository.findById(submission.getExamId());
            if (examOpt.isEmpty() || !examOpt.get().getTeacherId().equals(teacherId)) {
                throw new RuntimeException("Unauthorized: You can only grade your own exams");
            }
            
            submission.setEssayScore(essayScore);
            submission.setTotalScore(submission.getMcqScore() + essayScore);
            submission.setStatus(ExamSubmission.SubmissionStatus.FULLY_GRADED);
            
            return submissionRepository.save(submission);
        });
    }
    
    /**
     * Get submissions for an exam (Teacher only)
     */
    public List<ExamSubmission> getExamSubmissions(String examId, String teacherId) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty() || !examOpt.get().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        return submissionRepository.findByExamId(examId);
    }
    
    /**
     * Get student's submissions
     */
    public List<ExamSubmission> getStudentSubmissions(String studentId) {
        return submissionRepository.findByStudentId(studentId);
    }
    
    /**
     * Get specific submission
     */
    public Optional<ExamSubmission> getSubmission(String submissionId) {
        return submissionRepository.findById(submissionId);
    }
}
