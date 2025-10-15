package org.example.student_testing.test.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TestResult {
    private Integer testId;
    private String studentUsername;
    private Double score;
    private Double percentile;
    private String rankCode;
    private LocalDateTime completedAt;

    private String fullName;
    private String testName;


}
