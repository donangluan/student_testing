package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class AnswerResultDTO {

    private String questionContent;
    private String selectedOption;
    private String correctOption;
}
