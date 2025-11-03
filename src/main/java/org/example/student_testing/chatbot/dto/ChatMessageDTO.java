package org.example.student_testing.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {

    private Integer id;

    private Integer conversationId;

    private String role;

    private String content;

    private String metadata;

    private LocalDateTime createdAt;
}
