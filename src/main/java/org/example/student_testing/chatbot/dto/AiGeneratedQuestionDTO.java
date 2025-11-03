package org.example.student_testing.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AiGeneratedQuestionDTO {


    private Integer id;
    private Integer teacherId;

    private String questionContent;

    private String options;

    private String correctAnswer;

    private String difficulty;

    private String topic;

    private String status;
    private Integer topicId;

    private LocalDateTime createdAt;

    private Map<String, String> optionsMap;
    private String source;
    private Integer courseId;
    private String courseName;
}
