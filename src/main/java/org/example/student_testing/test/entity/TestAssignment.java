package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestAssignment {

    private Integer assignmentId;
    private Integer testId;

    private String studentUsername;

    private LocalDateTime assignedAt;

}
