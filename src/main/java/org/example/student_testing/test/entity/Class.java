package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Class {

    private Integer classId;
    private String className;
    private Integer courseId;
    private String courseName;
    private String teacherUsername;
    private String teacherName;
    private String description;
    private LocalDate endDate;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
}
