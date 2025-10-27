package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestDTO {


    private Integer testId;

    private String testName;

    private String testType;

    private String createdBy;


    private LocalDateTime createdAt;

    private Integer topicId;
    private Integer durationMinutes;

    private String topicName;
    private String courseName;
    private Integer resultId; // ID của kết quả đã nộp

}
