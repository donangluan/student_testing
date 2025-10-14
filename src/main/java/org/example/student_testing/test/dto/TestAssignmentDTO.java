package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestAssignmentDTO {

    private Integer assignmentId;
    private Integer testId;

    private String studentUsername;

    private LocalDateTime assignedAt;
}
