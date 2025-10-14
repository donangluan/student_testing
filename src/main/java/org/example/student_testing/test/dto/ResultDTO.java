package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResultDTO {

    private String studentUsername;
    private String fullName;
    private String testName;
    private Double score;
    private Double percentile;
    private String rank;
    private Integer testId;
    private LocalDateTime submittedAt;


}
