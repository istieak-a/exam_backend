package com.university.exam.repository;

import com.university.exam.model.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    private final MongoTemplate mongoTemplate;

    public MessageRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<ChatMessage> findAll() {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(query, ChatMessage.class);
    }

    public List<ChatMessage> findGlobalMessages(int limit) {
        Query query = new Query(Criteria.where("type").is(ChatMessage.MessageType.GLOBAL))
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(limit);
        return mongoTemplate.find(query, ChatMessage.class);
    }

    public List<ChatMessage> findGlobalMessages(int limit, long beforeTimestamp) {
        Criteria criteria = Criteria.where("type").is(ChatMessage.MessageType.GLOBAL);
        if (beforeTimestamp > 0) {
            criteria.and("timestamp").lt(beforeTimestamp);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .limit(limit);
        return mongoTemplate.find(query, ChatMessage.class);
    }

    public List<ChatMessage> findPrivateMessages(String userId1, String userId2, int limit) {
        Criteria participants = new Criteria().orOperator(
                new Criteria().andOperator(
                        Criteria.where("senderId").is(userId1),
                        Criteria.where("receiverId").is(userId2)),
                new Criteria().andOperator(
                        Criteria.where("senderId").is(userId2),
                        Criteria.where("receiverId").is(userId1)));
        Query query = new Query(
                Criteria.where("type").is(ChatMessage.MessageType.PRIVATE)
                        .andOperator(participants))
                .with(Sort.by(Sort.Direction.DESC, "timestamp")).limit(limit);
        return mongoTemplate.find(query, ChatMessage.class);
    }

    public List<ChatMessage> findUserConversations(String userId) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("senderId").is(userId),
                Criteria.where("receiverId").is(userId)).and("type").is(ChatMessage.MessageType.PRIVATE);
        Query query = new Query(criteria).with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(query, ChatMessage.class);
    }

    public ChatMessage save(ChatMessage message) {
        if (message.getId() == null || message.getId().isEmpty()) {
            message.setId(new ObjectId().toString());
            message.setTimestamp(System.currentTimeMillis());
        }

        mongoTemplate.save(message);
        return message;
    }
}
