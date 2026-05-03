package com.university.exam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/**
 * Session Configuration backed by JDBC (MySQL) so sessions survive restarts.
 */
@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 86400)
public class SessionConfig {
}
