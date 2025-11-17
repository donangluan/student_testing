package org.example.student_testing.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCriteriaDTO {

    private Integer criteriaId;
    private Integer testId;
    private Integer topicId;
    private Integer difficultyId;
    private Integer questionCount;


    private String topicName;
    private String difficultyName;




}
