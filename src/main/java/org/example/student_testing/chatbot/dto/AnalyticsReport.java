package org.example.student_testing.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnalyticsReport {

    private Integer id;

    private Integer teacherId;

    private Integer testId;

    private String reportType;

    private String insights;

    private LocalDateTime createdAt;
}
