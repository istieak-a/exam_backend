package com.university.exam.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.university.exam.model.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class MessageRepository {
    
    private static final String MESSAGES_FILE = "messages.txt";
    private final FileRepository<ChatMessage> fileRepository;
    
    public MessageRepository(FileRepository<ChatMessage> fileRepository) {
        this.fileRepository = fileRepository;
    }
    
    public List<ChatMessage> findAll() {
        return fileRepository.readAll(MESSAGES_FILE, new TypeReference<List<ChatMessage>>() {});
    }
    
    public List<ChatMessage> findGlobalMessages(int limit) {
        return findAll().stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.GLOBAL)
                .sorted((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<ChatMessage> findPrivateMessages(String userId1, String userId2, int limit) {
        return findAll().stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.PRIVATE)
                .filter(msg -> 
                    (msg.getSenderId().equals(userId1) && msg.getReceiverId().equals(userId2)) ||
                    (msg.getSenderId().equals(userId2) && msg.getReceiverId().equals(userId1))
                )
                .sorted((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<ChatMessage> findUserConversations(String userId) {
        return findAll().stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.PRIVATE)
                .filter(msg -> msg.getSenderId().equals(userId) || msg.getReceiverId().equals(userId))
                .sorted((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    public ChatMessage save(ChatMessage message) {
        if (message.getId() == null || message.getId().isEmpty()) {
            message.setId(UUID.randomUUID().toString());
            message.setTimestamp(System.currentTimeMillis());
        }
        
        fileRepository.append(MESSAGES_FILE, message);
        return message;
    }
}
