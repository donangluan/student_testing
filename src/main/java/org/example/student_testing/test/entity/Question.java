package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Question {

    private Integer questionId;

    private String content;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctOption;

    private Integer difficultyId;

    private Integer topicId;

    private String createdBy;

    private LocalDateTime createdAt;
    private String topicName;
    private Integer orderNo;
    private String source;

    private String description;

}
