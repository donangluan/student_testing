package org.example.student_testing.test.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TestDTO  implements Serializable {


    private Integer testId;

    private String testName;

    private String testType;

    private String createdBy;

    private String accessCode;

    private LocalDateTime createdAt;

    private Integer topicId;
    private Integer durationMinutes;

    private String topicName;
    private String courseName;
    private Integer resultId;
    private boolean isPublished;


    private LocalDateTime startTime;


    private LocalDateTime endTime;

    private Boolean isDynamic = false;


    private Integer maxAttempts;


    private List<TestCriteriaDTO> criteriaList = new ArrayList<>();


    public List<TestCriteriaDTO> getCriteriaList() {
        return criteriaList;
    }


    public void setCriteriaList(List<TestCriteriaDTO> criteriaList) {
        this.criteriaList = criteriaList;
    }


}
