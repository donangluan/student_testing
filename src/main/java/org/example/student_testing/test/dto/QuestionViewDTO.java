package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class QuestionViewDTO {

    private Integer questionId;
    private String content;
    private String correctOption;
    private String selectedOption;
}
