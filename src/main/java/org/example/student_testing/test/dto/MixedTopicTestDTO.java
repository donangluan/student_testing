package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MixedTopicTestDTO {

    private String testName;
    private String createdBy;
    private List<Integer> selectedCourseIds;
    private Map<Integer, Integer> topicDistribution = new HashMap<>();

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String accessCode;

}


