package com.university.exam.controller;

import com.university.exam.model.ApiResponse;
import com.university.exam.model.Exam;
import com.university.exam.model.ExamSubmission;
import com.university.exam.service.ExamService;
import com.university.exam.service.ValidationException;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    
    private final ExamService examService;
    
    public ExamController(ExamService examService) {
        this.examService = examService;
    }
    
    /**
     * Handle ValidationException with structured error response
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            ValidationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.putAll(ex.getErrors());
        
        return ResponseEntity.badRequest().body(
            ApiResponse.error(ex.getMessage(), response)
        );
    }
    
    /**
     * Create new exam (Teacher only)
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<Exam>>> createExam(
            @RequestBody Exam exam,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"TEACHER".equals(role)) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Access denied: Teachers only")
                )
            );
        }
        
        return examService.createExam(exam, userId)
                .thenApply(savedExam -> ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("Exam created successfully", savedExam)
                ))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof ValidationException) {
                        ValidationException valEx = (ValidationException) ex.getCause();
                        ApiResponse<Exam> errorResponse = new ApiResponse<>(false, valEx.getMessage(), null);
                        return ResponseEntity.badRequest().body(errorResponse);
                    }
                    return ResponseEntity.badRequest().body(
                        ApiResponse.error(ex.getMessage())
                    );
                });
    }
    
    /**
     * Update exam (Teacher only)
     */
    @PutMapping("/{examId}")
    public CompletableFuture<ResponseEntity<ApiResponse<Exam>>> updateExam(
            @PathVariable String examId,
            @RequestBody Exam exam,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"TEACHER".equals(role)) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Access denied: Teachers only")
                )
            );
        }
        
        return examService.updateExam(examId, exam, userId)
                .thenApply(updatedExam -> ResponseEntity.ok(
                    ApiResponse.success("Exam updated successfully", updatedExam)
                ))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof ValidationException) {
                        ValidationException valEx = (ValidationException) ex.getCause();
                        ApiResponse<Exam> errorResponse = new ApiResponse<>(false, valEx.getMessage(), null);
                        return ResponseEntity.badRequest().body(errorResponse);
                    }
                    String message = ex.getMessage();
                    if (message != null && message.contains("already submitted")) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            ApiResponse.error(message)
                        );
                    }
                    return ResponseEntity.badRequest().body(
                        ApiResponse.error(message)
                    );
                });
    }
    
    /**
     * Delete exam (Teacher only)
     */
    @DeleteMapping("/{examId}")
    public CompletableFuture<ResponseEntity<ApiResponse<Object>>> deleteExam(
            @PathVariable String examId,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"TEACHER".equals(role)) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Access denied: Teachers only")
                )
            );
        }
        
        return examService.deleteExam(examId, userId)
                .thenApply(v -> ResponseEntity.ok(
                    ApiResponse.<Object>success("Exam deleted successfully", null)
                ))
                .exceptionally(ex -> {
                    String message = ex.getMessage();
                    if (message != null && message.contains("already submitted")) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            ApiResponse.error(message)
                        );
                    }
                    return ResponseEntity.badRequest().body(
                        ApiResponse.error(message)
                    );
                });
    }
    
    /**
     * Get published exams (for students) - with pagination
     */
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublishedExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session) {
        
        String role = (String) session.getAttribute("role");
        
        if (!"STUDENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Access denied: Students only")
            );
        }
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Exam> examsPage = examService.getPublishedExams(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", examsPage.getContent());
        response.put("currentPage", examsPage.getNumber());
        response.put("totalItems", examsPage.getTotalElements());
        response.put("totalPages", examsPage.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("Published exams retrieved", response)
        );
    }
    
    /**
     * Get teacher's exams - with pagination
     */
    @GetMapping("/my-exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"TEACHER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Access denied: Teachers only")
            );
        }
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Exam> examsPage = examService.getTeacherExams(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", examsPage.getContent());
        response.put("currentPage", examsPage.getNumber());
        response.put("totalItems", examsPage.getTotalElements());
        response.put("totalPages", examsPage.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("Your exams retrieved", response)
        );
    }
    
    /**
     * Get exam by ID
     */
    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<Exam>> getExam(
            @PathVariable String examId,
            HttpSession session) {
        
        String role = (String) session.getAttribute("role");
        boolean isTeacher = "TEACHER".equals(role);
        
        Optional<Exam> examOpt = examService.getExamById(examId, isTeacher);
        
        if (examOpt.isPresent()) {
            return ResponseEntity.ok(
                ApiResponse.success("Exam retrieved", examOpt.get())
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Exam not found")
            );
        }
    }
    
    /**
     * Submit exam (Student only)
     */
    @PostMapping("/{examId}/submit")
    public CompletableFuture<ResponseEntity<ApiResponse<ExamSubmission>>> submitExam(
            @PathVariable String examId,
            @RequestBody Map<String, String> answers,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"STUDENT".equals(role)) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Access denied: Students only")
                )
            );
        }
        
        return examService.submitExam(examId, userId, answers)
                .thenApply(submission -> ResponseEntity.ok(
                    ApiResponse.success("Exam submitted successfully", submission)
                ))
                .exceptionally(ex -> ResponseEntity.badRequest().body(
                    ApiResponse.error(ex.getMessage())
                ));
    }
    
    /**
     * Grade essay questions (Teacher only)
     */
    @PostMapping("/submissions/{submissionId}/grade")
    public CompletableFuture<ResponseEntity<ApiResponse<ExamSubmission>>> gradeEssay(
            @PathVariable String submissionId,
            @RequestBody Map<String, Integer> gradeData,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"TEACHER".equals(role)) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Access denied: Teachers only")
                )
            );
        }
        
        int essayScore = gradeData.getOrDefault("essayScore", 0);
        
        return examService.gradeEssay(submissionId, essayScore, userId)
                .thenApply(submission -> ResponseEntity.ok(
                    ApiResponse.success("Essay graded successfully", submission)
                ))
                .exceptionally(ex -> ResponseEntity.badRequest().body(
                    ApiResponse.error(ex.getMessage())
                ));
    }

    /**
     * Get all submissions for teacher's exams - with pagination
     */
    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"TEACHER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Access denied: Teachers only")
            );
        }
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ExamSubmission> submissionsPage = examService.getAllSubmissionsForTeacher(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", submissionsPage.getContent());
        response.put("currentPage", submissionsPage.getNumber());
        response.put("totalItems", submissionsPage.getTotalElements());
        response.put("totalPages", submissionsPage.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("All submissions retrieved", response)
        );
    }
    
    /**
     * Get student's submissions - with pagination
     */
    @GetMapping("/my-submissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null || !"STUDENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Access denied: Students only")
            );
        }
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ExamSubmission> submissionsPage = examService.getStudentSubmissions(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", submissionsPage.getContent());
        response.put("currentPage", submissionsPage.getNumber());
        response.put("totalItems", submissionsPage.getTotalElements());
        response.put("totalPages", submissionsPage.getTotalPages());
        
        return ResponseEntity.ok(
            ApiResponse.success("Your submissions retrieved", response)
        );
    }
    
    /**
     * Get specific submission details
     */
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<ExamSubmission>> getSubmission(
            @PathVariable String submissionId,
            HttpSession session) {
        
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Not authenticated")
            );
        }
        
        Optional<ExamSubmission> submissionOpt = examService.getSubmission(submissionId);
        
        if (submissionOpt.isPresent()) {
            return ResponseEntity.ok(
                ApiResponse.success("Submission retrieved", submissionOpt.get())
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Submission not found")
            );
        }
    }
}
