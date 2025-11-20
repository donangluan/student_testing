package org.example.student_testing.test.dto;

import lombok.Data;

@Data
public class ProctorAlertDTO {

    private String studentUsername;

    private Integer testId;

    private String violationType;

    private String message;

    private String time;
}
