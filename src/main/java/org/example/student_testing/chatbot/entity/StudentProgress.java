package org.example.student_testing.chatbot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentProgress {

    private Integer id;

    private Integer studentId;

    private String topic;

    private BigDecimal accuracy;

    private LocalDateTime lastPracticed;

    private Integer practiceCount;
}
