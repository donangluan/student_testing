package org.example.student_testing.test.entity;

import lombok.Data;

@Data
public class DifficultyLevel {

    private Integer difficultyId;

    private String levelCode;

    private Integer pointValue;

    private String description;

}
