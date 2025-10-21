package org.example.student_testing.student.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentProfile {

    private String studentId;
    private String address;
    private String phone;
    private String avatarUrl    ;
    private String schoolName;
    private String className;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
}
