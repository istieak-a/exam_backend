package com.university.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExamManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExamManagementApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Exam Management System Started Successfully!");
        System.out.println("Server running at: http://localhost:8080");
        System.out.println("=================================================");
    }
}
