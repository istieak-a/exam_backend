package com.university.exam.controller;

import com.university.exam.model.ApiResponse;
import com.university.exam.model.ChatMessage;
import com.university.exam.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class ChatController {

        private final ChatService chatService;

        public ChatController(ChatService chatService) {
                this.chatService = chatService;
        }

        @MessageMapping("/chat.global")
        public void sendGlobalMessage(@Payload ChatMessage message) {
                chatService.sendGlobalMessage(message);
        }

        @MessageMapping("/chat.private")
        public void sendPrivateMessage(@Payload ChatMessage message) {
                chatService.sendPrivateMessage(message);
        }

        @GetMapping("/api/chat/global")
        @ResponseBody
        public ResponseEntity<ApiResponse<List<ChatMessage>>> getGlobalMessages(
                        @RequestParam(defaultValue = "50") int limit,
                        @RequestParam(required = false) Long before,
                        HttpSession session) {

                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ApiResponse.error("Not authenticated"));
                }

                List<ChatMessage> messages = chatService.getGlobalMessages(limit, before != null ? before : -1);

                return ResponseEntity.ok(
                                ApiResponse.success("Global messages retrieved", messages));
        }

        @GetMapping("/api/chat/private/{otherUserId}")
        @ResponseBody
        public ResponseEntity<ApiResponse<List<ChatMessage>>> getPrivateMessages(
                        @PathVariable Long otherUserId,
                        @RequestParam(defaultValue = "50") int limit,
                        HttpSession session) {

                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ApiResponse.error("Not authenticated"));
                }

                List<ChatMessage> messages = chatService.getPrivateMessages(userId, otherUserId, limit);
                return ResponseEntity.ok(
                                ApiResponse.success("Private messages retrieved", messages));
        }

        @GetMapping("/api/chat/conversations")
        @ResponseBody
        public ResponseEntity<ApiResponse<List<ChatMessage>>> getUserConversations(HttpSession session) {
                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ApiResponse.error("Not authenticated"));
                }

                List<ChatMessage> messages = chatService.getUserConversations(userId);
                return ResponseEntity.ok(
                                ApiResponse.success("Conversations retrieved", messages));
        }

        @PostMapping("/api/chat/global")
        @ResponseBody
        public CompletableFuture<ResponseEntity<ApiResponse<ChatMessage>>> postGlobalMessage(
                        @RequestBody ChatMessage message,
                        HttpSession session) {

                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return CompletableFuture.completedFuture(
                                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                                        ApiResponse.error("Not authenticated")));
                }

                return chatService.sendGlobalMessage(message)
                                .thenApply(savedMessage -> ResponseEntity.ok(
                                                ApiResponse.success("Message sent", savedMessage)))
                                .exceptionally(ex -> ResponseEntity.badRequest().body(
                                                ApiResponse.error(ex.getMessage())));
        }

        @PostMapping("/api/chat/private")
        @ResponseBody
        public CompletableFuture<ResponseEntity<ApiResponse<ChatMessage>>> postPrivateMessage(
                        @RequestBody ChatMessage message,
                        HttpSession session) {

                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                        return CompletableFuture.completedFuture(
                                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                                        ApiResponse.error("Not authenticated")));
                }

                return chatService.sendPrivateMessage(message)
                                .thenApply(savedMessage -> ResponseEntity.ok(
                                                ApiResponse.success("Message sent", savedMessage)))
                                .exceptionally(ex -> ResponseEntity.badRequest().body(
                                                ApiResponse.error(ex.getMessage())));
        }
}
