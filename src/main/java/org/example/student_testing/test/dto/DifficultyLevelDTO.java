package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class DifficultyLevelDTO {

    private Integer difficultyId;

    private String levelCode;

    private Integer pointValue;

    private String description;
}
