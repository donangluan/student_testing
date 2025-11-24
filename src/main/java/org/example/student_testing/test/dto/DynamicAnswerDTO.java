package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class DynamicAnswerDTO {

    private Integer testId;
    private String studentUsername;
    private Integer questionId;
    private String selectedOption;
    private Integer currentDifficulty;
    private Integer topicId;
    private String testType;
    private String accessCode;
}
