package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class TopicDTO {

    private Integer topicId;
    private String topicName;
    private Integer courseId;

    private String courseName;
}
