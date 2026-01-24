package com.university.exam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

/**
 * Session Configuration for the Exam Management System
 * Uses MongoDB to store session data for persistence across server restarts
 */
@Configuration
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 86400) // 24 hours
public class SessionConfig {
    // Spring Session will automatically configure MongoDB session repository
}