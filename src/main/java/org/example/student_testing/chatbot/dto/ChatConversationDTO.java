package org.example.student_testing.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversationDTO {

    private Integer id;

    private Integer userId;

    private String userType;

    private LocalDateTime createdAt;
}
