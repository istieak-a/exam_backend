package com.university.exam.service;

import com.university.exam.model.ChatMessage;
import com.university.exam.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public CompletableFuture<ChatMessage> sendGlobalMessage(ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            message.setType(ChatMessage.MessageType.GLOBAL);
            message.setReceiverId(null);
            message.setReceiverName(null);
            if (message.getTimestamp() == 0L) {
                message.setTimestamp(System.currentTimeMillis());
            }

            ChatMessage savedMessage = messageRepository.save(message);

            messagingTemplate.convertAndSend("/topic/global", savedMessage);

            return savedMessage;
        });
    }

    public CompletableFuture<ChatMessage> sendPrivateMessage(ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            message.setType(ChatMessage.MessageType.PRIVATE);
            if (message.getTimestamp() == 0L) {
                message.setTimestamp(System.currentTimeMillis());
            }

            ChatMessage savedMessage = messageRepository.save(message);

            if (message.getReceiverId() != null) {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(message.getReceiverId()),
                        "/queue/messages",
                        savedMessage);
            }

            if (message.getSenderId() != null) {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(message.getSenderId()),
                        "/queue/messages",
                        savedMessage);
            }

            return savedMessage;
        });
    }

    public List<ChatMessage> getGlobalMessages(int limit) {
        return getGlobalMessages(limit, -1);
    }

    public List<ChatMessage> getGlobalMessages(int limit, long before) {
        Pageable page = PageRequest.of(0, Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "timestamp"));
        if (before > 0) {
            return messageRepository.findByTypeAndTimestampLessThanOrderByTimestampDesc(
                    ChatMessage.MessageType.GLOBAL, before, page);
        }
        return messageRepository.findByTypeOrderByTimestampDesc(ChatMessage.MessageType.GLOBAL, page);
    }

    public List<ChatMessage> getPrivateMessages(Long userId1, Long userId2, int limit) {
        Pageable page = PageRequest.of(0, Math.max(limit, 1));
        return messageRepository.findPrivateMessages(userId1, userId2, page);
    }

    public List<ChatMessage> getUserConversations(Long userId) {
        return messageRepository.findUserConversations(userId);
    }
}
