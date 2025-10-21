package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class ChatbotMessageDTO {

    private String username;
    private String role;
    private String message;
    private String response;
}
