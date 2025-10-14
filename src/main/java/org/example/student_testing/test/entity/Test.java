package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Test {

    private Integer testId;

    private String testName;

    private String testType;

    private String createdBy;

    private Integer topicId;
    private LocalDateTime createdAt;

}
