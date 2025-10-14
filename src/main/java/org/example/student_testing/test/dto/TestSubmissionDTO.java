package org.example.student_testing.test.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TestSubmissionDTO {

    private Integer testId; // ID bài kiểm tra
    private String studentUsername; // Tên đăng nhập học sinh
    private Map<Integer, String> answers;
}
