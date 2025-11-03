package org.example.student_testing.chatbot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PracticeSession {

    private Integer id;

    private Integer studentId;
    private String topic;

    private String questionsJson;

    private Integer score;

    private LocalDateTime completedAt;
}
