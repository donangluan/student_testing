package org.example.student_testing.test.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TestResultDTO {


    private Integer id;
    private Integer testId;
    private String studentUsername;
    private Integer score;
    private BigDecimal percentile;
    private Long rankId;
    private LocalDateTime completedAt;

}
