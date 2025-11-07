package org.example.student_testing.chatbot.dto;

import lombok.Data;

@Data
public class AnswerExplanationRequestDTO {

    private Integer questionId;
    private String questionContent;

    private String optionA;

    private String optionB;

    private  String optionC;

    private String optionD;

    private String studentAnswer;

    private String correctAnswer;
}
