package com.university.exam.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.university.exam.model.MCQOption;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MCQOptionRepository {
    private final FileRepository<MCQOption> fileRepository;
    private static final String FILE_NAME = "mcqoptions.txt";
    
    public MCQOptionRepository(FileRepository<MCQOption> fileRepository) {
        this.fileRepository = fileRepository;
    }
    
    public List<MCQOption> findByQuestionId(String questionId) {
        return fileRepository.readAll(FILE_NAME, new TypeReference<List<MCQOption>>() {}).stream()
                .filter(option -> option.getQuestionId().equals(questionId))
                .collect(Collectors.toList());
    }
    
    public MCQOption save(MCQOption option) {
        List<MCQOption> options = fileRepository.readAll(FILE_NAME, new TypeReference<List<MCQOption>>() {});
        
        if (option.getId() == null || option.getId().isEmpty()) {
            option.setId(java.util.UUID.randomUUID().toString());
            options.add(option);
        } else {
            options.removeIf(o -> o.getId().equals(option.getId()));
            options.add(option);
        }
        
        fileRepository.writeAll(FILE_NAME, options);
        return option;
    }
    
    public void deleteByQuestionId(String questionId) {
        List<MCQOption> options = fileRepository.readAll(FILE_NAME, new TypeReference<List<MCQOption>>() {});
        options.removeIf(option -> option.getQuestionId().equals(questionId));
        fileRepository.writeAll(FILE_NAME, options);
    }
    
    public void deleteByExamId(String examId, List<String> questionIds) {
        List<MCQOption> options = fileRepository.readAll(FILE_NAME, new TypeReference<List<MCQOption>>() {});
        options.removeIf(option -> questionIds.contains(option.getQuestionId()));
        fileRepository.writeAll(FILE_NAME, options);
    }
}
