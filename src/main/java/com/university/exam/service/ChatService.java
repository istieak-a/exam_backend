package com.university.exam.service;

import com.university.exam.model.ChatMessage;
import com.university.exam.repository.MessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send global message to all users (Thread-safe)
     */
    public CompletableFuture<ChatMessage> sendGlobalMessage(ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            message.setType(ChatMessage.MessageType.GLOBAL);
            message.setReceiverId(null);
            message.setReceiverName(null);

            // Save to file
            ChatMessage savedMessage = messageRepository.save(message);

            // Broadcast to all connected clients via WebSocket
            messagingTemplate.convertAndSend("/topic/global", savedMessage);

            return savedMessage;
        });
    }

    /**
     * Send private message to specific user (Thread-safe)
     */
    public CompletableFuture<ChatMessage> sendPrivateMessage(ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            message.setType(ChatMessage.MessageType.PRIVATE);

            // Save to file
            ChatMessage savedMessage = messageRepository.save(message);

            // Send to specific user via WebSocket
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId(),
                    "/queue/messages",
                    savedMessage);

            // Also send to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId(),
                    "/queue/messages",
                    savedMessage);

            return savedMessage;
        });
    }

    /**
     * Get recent global messages
     */
    public List<ChatMessage> getGlobalMessages(int limit) {
        return getGlobalMessages(limit, -1);
    }

    public List<ChatMessage> getGlobalMessages(int limit, long before) {
        if (before > 0) {
            return messageRepository.findGlobalMessages(limit, before);
        }
        return messageRepository.findGlobalMessages(limit);
    }

    /**
     * Get private conversation between two users
     */
    public List<ChatMessage> getPrivateMessages(String userId1, String userId2, int limit) {
        return messageRepository.findPrivateMessages(userId1, userId2, limit);
    }

    /**
     * Get all conversations for a user
     */
    public List<ChatMessage> getUserConversations(String userId) {
        return messageRepository.findUserConversations(userId);
    }
}
