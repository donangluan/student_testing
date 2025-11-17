package org.example.student_testing.test.entity;

import lombok.Data;

@Data

public class TestCriteria {

    private Integer criteriaId;
    private Integer testId;
    private Integer topicId;
    private Integer difficultyId;
    private Integer questionCount;


    private String topicName;
    private String difficultyName;
}
