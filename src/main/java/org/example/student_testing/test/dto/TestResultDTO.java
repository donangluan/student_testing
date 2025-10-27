package org.example.student_testing.test.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TestResultDTO {


    private Integer id;
    private Integer testId;
    private String studentUsername;
        private Double score;
    private Double percentile;
    private String testName;
    private String testType;
    private String  rankCode;
    private LocalDateTime completedAt;

}
