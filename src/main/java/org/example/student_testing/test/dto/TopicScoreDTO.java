package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class TopicScoreDTO {
    private String topicName;
    private Double averageScore;
    private Integer topicId;
}
