package com.university.exam.repository;

import com.university.exam.model.ExamSubmission;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionRepository {
    
    private final MongoTemplate mongoTemplate;
    
    public SubmissionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    public List<ExamSubmission> findAll() {
        return mongoTemplate.findAll(ExamSubmission.class);
    }
    
    public Optional<ExamSubmission> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, ExamSubmission.class));
    }
    
    public List<ExamSubmission> findByExamId(String examId) {
        Query query = new Query(Criteria.where("examId").is(examId));
        return mongoTemplate.find(query, ExamSubmission.class);
    }
    
    public List<ExamSubmission> findByExamIdIn(Collection<String> examIds) {
        Query query = new Query(Criteria.where("examId").in(examIds));
        return mongoTemplate.find(query, ExamSubmission.class);
    }
    
    public Page<ExamSubmission> findByExamIdIn(Collection<String> examIds, Pageable pageable) {
        Query query = new Query(Criteria.where("examId").in(examIds));
        long total = mongoTemplate.count(query, ExamSubmission.class);
        
        query.with(pageable);
        List<ExamSubmission> submissions = mongoTemplate.find(query, ExamSubmission.class);
        
        return new PageImpl<>(submissions, pageable, total);
    }
    
    public List<ExamSubmission> findByStudentId(String studentId) {
        Query query = new Query(Criteria.where("studentId").is(studentId));
        return mongoTemplate.find(query, ExamSubmission.class);
    }
    
    public Page<ExamSubmission> findByStudentId(String studentId, Pageable pageable) {
        Query query = new Query(Criteria.where("studentId").is(studentId));
        long total = mongoTemplate.count(query, ExamSubmission.class);
        
        query.with(pageable);
        List<ExamSubmission> submissions = mongoTemplate.find(query, ExamSubmission.class);
        
        return new PageImpl<>(submissions, pageable, total);
    }
    
    public Optional<ExamSubmission> findByExamAndStudent(String examId, String studentId) {
        Query query = new Query(Criteria.where("examId").is(examId).and("studentId").is(studentId));
        return Optional.ofNullable(mongoTemplate.findOne(query, ExamSubmission.class));
    }
    
    public ExamSubmission save(ExamSubmission submission) {
        if (submission.getId() == null || submission.getId().isEmpty()) {
            submission.setId(new ObjectId().toString());
            submission.setSubmittedAt(System.currentTimeMillis());
        }
        mongoTemplate.save(submission);
        return submission;
    }
    
    public void delete(String id) {
        findById(id).ifPresent(existing -> mongoTemplate.remove(existing));
    }
    
    public boolean hasSubmissions(String examId) {
        Query query = new Query(Criteria.where("examId").is(examId));
        return mongoTemplate.exists(query, ExamSubmission.class);
    }
    
    public int countByExamId(String examId) {
        Query query = new Query(Criteria.where("examId").is(examId));
        Long count = mongoTemplate.count(query, ExamSubmission.class);
        return count != null ? count.intValue() : 0;
    }
}
