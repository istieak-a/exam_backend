package com.university.exam.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.university.exam.model.ExamSubmission;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SubmissionRepository {
    
    private static final String SUBMISSIONS_FILE = "submissions.txt";
    private final FileRepository<ExamSubmission> fileRepository;
    
    public SubmissionRepository(FileRepository<ExamSubmission> fileRepository) {
        this.fileRepository = fileRepository;
    }
    
    public List<ExamSubmission> findAll() {
        return fileRepository.readAll(SUBMISSIONS_FILE, new TypeReference<List<ExamSubmission>>() {});
    }
    
    public Optional<ExamSubmission> findById(String id) {
        return findAll().stream()
                .filter(submission -> submission.getId().equals(id))
                .findFirst();
    }
    
    public List<ExamSubmission> findByExamId(String examId) {
        return findAll().stream()
                .filter(submission -> submission.getExamId().equals(examId))
                .collect(Collectors.toList());
    }
    
    public List<ExamSubmission> findByStudentId(String studentId) {
        return findAll().stream()
                .filter(submission -> submission.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }
    
    public Optional<ExamSubmission> findByExamAndStudent(String examId, String studentId) {
        return findAll().stream()
                .filter(submission -> submission.getExamId().equals(examId) 
                        && submission.getStudentId().equals(studentId))
                .findFirst();
    }
    
    public ExamSubmission save(ExamSubmission submission) {
        List<ExamSubmission> submissions = findAll();
        
        if (submission.getId() == null || submission.getId().isEmpty()) {
            // New submission
            submission.setId(UUID.randomUUID().toString());
            submission.setSubmittedAt(System.currentTimeMillis());
            submissions.add(submission);
        } else {
            // Update existing submission
            submissions = submissions.stream()
                    .map(s -> s.getId().equals(submission.getId()) ? submission : s)
                    .collect(Collectors.toList());
        }
        
        fileRepository.writeAll(SUBMISSIONS_FILE, submissions);
        return submission;
    }
    
    public void delete(String id) {
        List<ExamSubmission> submissions = findAll().stream()
                .filter(submission -> !submission.getId().equals(id))
                .collect(Collectors.toList());
        fileRepository.writeAll(SUBMISSIONS_FILE, submissions);
    }
}
