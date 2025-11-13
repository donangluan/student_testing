package org.example.student_testing.test.dto;

import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class ClassDTO {

    private Integer classId;
    private String className;
    private Integer courseId;
    private String courseName;
    private String teacherUsername;
    private String teacherName;
    private String description;
    private Date endDate;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
}
