package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class TestQuestionDTO {

    private Integer id;

    private Integer testId;

    private String studentUsername;

    private Integer questionId;

    private Integer orderNo;

    private Integer difficultyId;

    private String source;

    private String assignedBy;
}
