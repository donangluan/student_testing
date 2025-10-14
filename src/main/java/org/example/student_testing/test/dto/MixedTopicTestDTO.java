package org.example.student_testing.test.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MixedTopicTestDTO {

    private String testName;
    private String createdBy;
    private Map<Integer, Integer> topicDistribution;
}
