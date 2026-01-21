package com.university.exam.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.university.exam.model.Exam;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ExamRepository {
    
    private static final String EXAMS_FILE = "exams.txt";
    private final FileRepository<Exam> fileRepository;
    
    public ExamRepository(FileRepository<Exam> fileRepository) {
        this.fileRepository = fileRepository;
    }
    
    public List<Exam> findAll() {
        return fileRepository.readAll(EXAMS_FILE, new TypeReference<List<Exam>>() {});
    }
    
    public Optional<Exam> findById(String id) {
        return findAll().stream()
                .filter(exam -> exam.getId().equals(id))
                .findFirst();
    }
    
    public List<Exam> findByTeacherId(String teacherId) {
        return findAll().stream()
                .filter(exam -> exam.getTeacherId().equals(teacherId))
                .collect(Collectors.toList());
    }
    
    public List<Exam> findPublished() {
        return findAll().stream()
                .filter(exam -> exam.getStatus() == Exam.ExamStatus.PUBLISHED)
                .collect(Collectors.toList());
    }
    
    public Exam save(Exam exam) {
        List<Exam> exams = findAll();
        
        if (exam.getId() == null || exam.getId().isEmpty()) {
            // New exam
            exam.setId(UUID.randomUUID().toString());
            exam.setCreatedAt(System.currentTimeMillis());
            exams.add(exam);
        } else {
            // Update existing exam
            exams = exams.stream()
                    .map(e -> e.getId().equals(exam.getId()) ? exam : e)
                    .collect(Collectors.toList());
        }
        
        fileRepository.writeAll(EXAMS_FILE, exams);
        return exam;
    }
    
    public void delete(String id) {
        List<Exam> exams = findAll().stream()
                .filter(exam -> !exam.getId().equals(id))
                .collect(Collectors.toList());
        fileRepository.writeAll(EXAMS_FILE, exams);
    }
}
