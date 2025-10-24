package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class LoginHistoryDTO {

    private Integer id;

    private String username;

    private LocalDateTime loginTime;

    private String ipAddress;

    private String fullName;
    private String role;
    private String userAgent;



}
