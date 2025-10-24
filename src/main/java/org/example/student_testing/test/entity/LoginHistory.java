package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginHistory {

    private Integer id;

    private String username;

    private LocalDateTime loginTime;

    private String ipAddress;

    private String fullName;
    private String role;
    private String userAgent;
}
