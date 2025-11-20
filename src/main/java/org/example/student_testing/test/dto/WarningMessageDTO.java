package org.example.student_testing.test.dto;

import lombok.Data;

import java.util.List;

@Data
public class WarningMessageDTO {

    private Integer testId;

    private String message;

    private String sender;

    private List<String> targetUsernames;
}
