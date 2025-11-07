package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TestSubmissionDTO {
    private Integer submissionId;
    private Integer testId;
    private String studentUsername;
    private LocalDateTime submittedAt;
    private Integer totalAnswered;
    private Integer correctCount;
    private Double score;
    private String testName;
    private Boolean graded;

    private Boolean hasSubmitted;
    private LocalDateTime lastAnswered;

}
