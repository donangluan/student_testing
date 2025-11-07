package org.example.student_testing.chatbot.dto;

import lombok.Data;

@Data
public class QuestionAnalysisDTO {
    private Integer orderNo;

    private String content;

    private String difficulty;

    private String topic;

    private double correctRate;

}
