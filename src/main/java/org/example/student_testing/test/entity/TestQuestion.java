package org.example.student_testing.test.entity;

import lombok.Data;

@Data
public class TestQuestion {

    private Integer id;

    private Integer testId;

    private String studentUsername;

    private Integer questionId;

    private Integer orderNo;

    private Integer difficultyId;


}
