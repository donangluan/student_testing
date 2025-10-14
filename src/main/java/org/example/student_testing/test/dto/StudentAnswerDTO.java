package org.example.student_testing.test.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentAnswerDTO {

    private Integer id;

    private Integer testId;

    private String studentUsername;

    private Integer questionId;

    private String selectedOption;

    private LocalDateTime answeredAt;
}
