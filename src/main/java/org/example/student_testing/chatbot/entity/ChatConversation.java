package org.example.student_testing.chatbot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversation {

    private Integer id;

    private Integer userId;

    private String userType;

    private LocalDateTime createdAt;

}
