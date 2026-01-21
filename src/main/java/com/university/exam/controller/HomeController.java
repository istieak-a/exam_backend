package com.university.exam.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "running");
        response.put("application", "Exam Management System API");
        response.put("version", "1.0.0");
        response.put("message", "Welcome to the Exam Management System Backend!");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Authentication", "/api/auth/*");
        endpoints.put("Exams", "/api/exams/*");
        endpoints.put("Chat", "/api/chat/*");
        endpoints.put("WebSocket", "/ws");
        response.put("endpoints", endpoints);
        
        Map<String, String> documentation = new HashMap<>();
        documentation.put("API Docs", "See README.md for complete API documentation");
        documentation.put("Quick Start", "POST /api/auth/signup to create an account");
        documentation.put("Login", "POST /api/auth/login to authenticate");
        response.put("documentation", documentation);
        
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }
}
