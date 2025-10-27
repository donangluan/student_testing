package org.example.student_testing.test.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmissionViewDTO {

    private Integer resultId;
    private String studentUsername;
    private Double score;
    private String rankCode;
    private LocalDateTime completedAt;

    private List<QuestionViewDTO> questions;
}
