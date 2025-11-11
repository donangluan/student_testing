package org.example.student_testing.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorReponseDTO {

    private LocalDateTime timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;


}
