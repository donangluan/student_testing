package org.example.student_testing.test.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentAnswer {

    private Integer id;

    private Integer testId;

    private String studentUsername;

    private Integer questionId;

    private String selectedOption;

    private LocalDateTime answeredAt;


}
