package com.university.exam.repository;

import com.university.exam.model.Exam;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ExamRepository {
    
    private final MongoTemplate mongoTemplate;
    
    public ExamRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    public List<Exam> findAll() {
        return mongoTemplate.findAll(Exam.class);
    }
    
    public Optional<Exam> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Exam.class));
    }
    
    public List<Exam> findByTeacherId(String teacherId) {
        Query query = new Query(Criteria.where("teacherId").is(teacherId));
        return mongoTemplate.find(query, Exam.class);
    }
    
    public Page<Exam> findByTeacherId(String teacherId, Pageable pageable) {
        Query query = new Query(Criteria.where("teacherId").is(teacherId));
        long total = mongoTemplate.count(query, Exam.class);
        
        query.with(pageable);
        List<Exam> exams = mongoTemplate.find(query, Exam.class);
        
        return new PageImpl<>(exams, pageable, total);
    }
    
    public List<Exam> findPublished() {
        Query query = new Query(Criteria.where("status").is(Exam.ExamStatus.PUBLISHED));
        return mongoTemplate.find(query, Exam.class);
    }
    
    public Page<Exam> findPublished(Pageable pageable) {
        Query query = new Query(Criteria.where("status").is(Exam.ExamStatus.PUBLISHED));
        long total = mongoTemplate.count(query, Exam.class);
        
        System.out.println("🔍 Searching for published exams with status: " + Exam.ExamStatus.PUBLISHED);
        System.out.println("📊 Total published exams found: " + total);
        
        query.with(pageable);
        List<Exam> exams = mongoTemplate.find(query, Exam.class);
        
        System.out.println("📚 Exams in current page: " + exams.size());
        for (Exam exam : exams) {
            System.out.println("  - " + exam.getTitle() + " (Status: " + exam.getStatus() + ")");
        }
        
        return new PageImpl<>(exams, pageable, total);
    }
    
    public List<Exam> findByStatus(Exam.ExamStatus status) {
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.find(query, Exam.class);
    }
    
    public List<Exam> findByCourse(String course) {
        Query query = new Query(Criteria.where("course").regex("^" + course + "$", "i"));
        return mongoTemplate.find(query, Exam.class);
    }
    
    public List<Exam> findByExamType(Exam.ExamType examType) {
        Query query = new Query(Criteria.where("examType").is(examType));
        return mongoTemplate.find(query, Exam.class);
    }
    
    public List<Exam> findActive() {
        long now = System.currentTimeMillis();
        Query query = new Query(
                Criteria.where("startDateTime").lte(now)
                        .and("endDateTime").gte(now)
                        .and("status").in(Exam.ExamStatus.ACTIVE, Exam.ExamStatus.PUBLISHED)
        );
        query.with(Sort.by(Sort.Direction.DESC, "startDateTime"));
        return mongoTemplate.find(query, Exam.class);
    }
    
    public Exam save(Exam exam) {
        if (exam.getId() == null || exam.getId().isEmpty()) {
            exam.setId(new ObjectId().toString());
            exam.setCreatedAt(System.currentTimeMillis());
        }
        exam.setUpdatedAt(System.currentTimeMillis());
        mongoTemplate.save(exam);
        return exam;
    }
    
    public void delete(String id) {
        findById(id).ifPresent(existing -> mongoTemplate.remove(existing));
    }
}
