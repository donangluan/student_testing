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

    private String topicName;
    private String courseName;
}
