package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassDTO {

    private Integer classId;
    private String className;
    private LocalDateTime createdAt;
}
