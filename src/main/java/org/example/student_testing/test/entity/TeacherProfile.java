package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherProfile {

    private String teacherId;
    private String department;

    private String phone;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String username;
    private String fullName;

    private String email;
}
