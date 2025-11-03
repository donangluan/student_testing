package org.example.student_testing.chatbot.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateAiTestByCourseRequest {
    private String testName;
    private Integer courseId;
    private String teacherUsername;
    private List<String> studentUsernames;


    private Map<String, Integer> topicDistribution;
}
