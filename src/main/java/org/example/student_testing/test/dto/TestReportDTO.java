package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestReportDTO {

    private String testName;

    private String studentUsername;

    private String studentEmail;

    private Double score;

        private Integer totalQuestions;

        private String duration;

        private LocalDateTime submissionTime;


}
