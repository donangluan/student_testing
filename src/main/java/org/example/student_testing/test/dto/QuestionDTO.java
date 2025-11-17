package org.example.student_testing.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionDTO {


    private Integer questionId;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @NotBlank(message = "Đáp án A không được để trống")
    private String optionA;

    @NotBlank(message = "Đáp án B không được để trống")
    private String optionB;

    @NotBlank(message = "Đáp án C không được để trống")
    private String optionC;

    @NotBlank(message = "Đáp án D không được để trống")
    private String optionD;

    @NotBlank(message = "Phải chọn đáp án đúng")
    private String correctOption;

    @NotNull(message = "Phải chọn độ khó")
    private Integer difficultyId;

    @NotNull(message = "Phải chọn chủ đề")
    private Integer topicId;


    private String createdBy;

    private String topicName;
    private Integer orderNo;
    private String source;
    private LocalDateTime createdAt;

    private String description;
}
