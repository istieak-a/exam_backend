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
     * Create a new exam (Teacher only)
     */
    public CompletableFuture<Exam> createExam(Exam exam, String teacherId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> teacher = userRepository.findById(teacherId);
            if (teacher.isEmpty() || teacher.get().getRole() != User.UserRole.TEACHER) {
                throw new RuntimeException("Only teachers can create exams");
            }
            
            exam.setTeacherId(teacherId);
            exam.setTeacherName(teacher.get().getFullName());
            
            // Calculate total marks
            int totalMarks = exam.getQuestions().stream()
                    .mapToInt(Question::getMarks)
                    .sum();
            exam.setTotalMarks(totalMarks);
            
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
            
            updatedExam.setId(examId);
            updatedExam.setTeacherId(teacherId);
            updatedExam.setCreatedAt(existingExam.get().getCreatedAt());
            
            // Recalculate total marks
            int totalMarks = updatedExam.getQuestions().stream()
                    .mapToInt(Question::getMarks)
                    .sum();
            updatedExam.setTotalMarks(totalMarks);
            
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
            
            examRepository.delete(examId);
        });
    }
    
    /**
     * Get all published exams (for students)
     */
    public List<Exam> getPublishedExams() {
        return examRepository.findPublished();
    }
    
    /**
     * Get teacher's exams
     */
    public List<Exam> getTeacherExams(String teacherId) {
        return examRepository.findByTeacherId(teacherId);
    }
    
    /**
     * Get exam by ID (sanitize for students - hide correct answers)
     */
    public Optional<Exam> getExamById(String examId, boolean isTeacher) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        
        if (examOpt.isPresent() && !isTeacher) {
            Exam exam = examOpt.get();
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
                        copy.setOrderIndex(q.getOrderIndex());
                        // Don't include correctAnswer
                        return copy;
                    })
                    .toList();
            
            exam.setQuestions(sanitizedQuestions);
        }
        
        return examOpt;
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
            
            Optional<User> studentOpt = userRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                throw new RuntimeException("Student not found");
            }
            
            Exam exam = examOpt.get();
            User student = studentOpt.get();
            
            // Auto-grade MCQ questions
            int mcqScore = 0;
            boolean hasEssay = false;
            
            for (Question question : exam.getQuestions()) {
                if (question.getType() == Question.QuestionType.MCQ) {
                    String studentAnswer = answers.get(question.getId());
                    if (studentAnswer != null && 
                        studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                        mcqScore += question.getMarks();
                    }
                } else {
                    hasEssay = true;
                }
            }
            
            ExamSubmission submission = new ExamSubmission();
            submission.setExamId(examId);
            submission.setStudentId(studentId);
            submission.setStudentName(student.getFullName());
            submission.setAnswers(answers);
            submission.setMcqScore(mcqScore);
            
            if (hasEssay) {
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
