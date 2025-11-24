package org.example.student_testing.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AiGeneratedQuestionDTO {


    private Integer id;
    private Integer teacherId;

    private String content;

    private String options;

    private String correctAnswer;

    private String difficulty;

    private String topic;

    private String status;

    private Map<String, String> optionsMap;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private Integer courseId;
    private String courseName;
    private String source;
    private LocalDateTime createdAt;
    private Integer topicId;
    private String createdBy;

    private Integer officialQuestionId;
}
