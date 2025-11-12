package org.example.student_testing.test.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class QuestionViewDTO {

    private Integer questionId;
    private String content;
    private String correctOption;
    private String selectedOption;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private Map<String, String> options = new HashMap<>();
}
