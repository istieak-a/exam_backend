package com.university.exam.repository;

import com.university.exam.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByTypeOrderByTimestampDesc(ChatMessage.MessageType type, Pageable pageable);

    List<ChatMessage> findByTypeAndTimestampLessThanOrderByTimestampDesc(
            ChatMessage.MessageType type, long beforeTimestamp, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.type = com.university.exam.model.ChatMessage$MessageType.PRIVATE " +
           "AND ((m.senderId = :a AND m.receiverId = :b) OR (m.senderId = :b AND m.receiverId = :a)) " +
           "ORDER BY m.timestamp DESC")
    List<ChatMessage> findPrivateMessages(@Param("a") Long a, @Param("b") Long b, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.type = com.university.exam.model.ChatMessage$MessageType.PRIVATE " +
           "AND (m.senderId = :uid OR m.receiverId = :uid) ORDER BY m.timestamp DESC")
    List<ChatMessage> findUserConversations(@Param("uid") Long uid);
}
