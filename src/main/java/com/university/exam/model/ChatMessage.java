package com.university.exam.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
public class ChatMessage {
    @Id
    private String id;
    private String senderId;
    private String senderName;
    private String senderRole;
    private String receiverId; // null for global chat
    private String receiverName;
    private String content;
    private long timestamp;
    private MessageType type;
    
    public enum MessageType {
        GLOBAL, PRIVATE
    }
    
    public ChatMessage() {}
    
    public ChatMessage(String id, String senderId, String senderName, String senderRole,
                       String receiverId, String receiverName, String content,
                       long timestamp, MessageType type) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
}
