package org.example.student_testing.student.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String roleCode;
    private LocalDateTime  createdAt;
    private LocalDateTime  updatedAt;


}
