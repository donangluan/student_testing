package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UniqueTestRequest {

    private String testName;
    private Integer topicId;
    private Integer questionCount;
    private List<String> studentUsername;
    private Integer numberOfQuestions;
    private String createdBy;
    private Integer durationMinutes;
    private String testType;
    private String accessCode;

    private LocalDateTime startTime; // Bổ sung
    private LocalDateTime endTime;
}
