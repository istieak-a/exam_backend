package com.university.exam.repository;

import com.university.exam.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    
    private final MongoTemplate mongoTemplate;
    
    public UserRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }
    
    public Optional<User> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, User.class));
    }
    
    public Optional<User> findByUsername(String username) {
        Query query = new Query(Criteria.where("username").regex("^" + username + "$", "i"));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }
    
    public Optional<User> findByEmail(String email) {
        Query query = new Query(Criteria.where("email").regex("^" + email + "$", "i"));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }
    
    public List<User> findByRole(User.UserRole role) {
        Query query = new Query(Criteria.where("role").is(role));
        return mongoTemplate.find(query, User.class);
    }
    
    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(new ObjectId().toString());
            user.setCreatedAt(System.currentTimeMillis());
        }
        mongoTemplate.save(user);
        return user;
    }
    
    public void delete(String id) {
        findById(id).ifPresent(existing -> mongoTemplate.remove(existing));
    }
    
    public boolean existsByUsername(String username) {
        Query query = new Query(Criteria.where("username").regex("^" + username + "$", "i"));
        return mongoTemplate.exists(query, User.class);
    }
    
    public boolean existsByEmail(String email) {
        Query query = new Query(Criteria.where("email").regex("^" + email + "$", "i"));
        return mongoTemplate.exists(query, User.class);
    }
}
