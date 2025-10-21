package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TestSubmissionDTO {
    private String testName;
    private Integer testId;
    private String studentUsername;
    private boolean hasSubmitted;
    private int totalAnswered;
    private int correctCount;
    private LocalDateTime lastAnswered;
}
