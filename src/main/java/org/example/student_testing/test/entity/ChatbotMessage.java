package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatbotMessage {

    private Integer id;

    private String username;

    private String role;

    private String message;

    private String response;

    private LocalDateTime createdAt;
}
