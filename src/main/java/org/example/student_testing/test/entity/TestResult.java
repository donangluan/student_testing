package org.example.student_testing.test.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TestResult {
    private Integer id;
    private Integer testId;
    private String studentUsername;
    private Integer score;
    private BigDecimal percentile;
    private Long rankId;
    private LocalDateTime completedAt;

}
