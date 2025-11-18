package org.example.student_testing.test.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class TestSessionDTO {

    // Các trường lưu trong DB (Mapper/JPA sẽ ánh xạ)
    private Integer id; // ID của session (nếu cần)
    private Integer testId;
    private String studentUsername;
    private Integer timeRemainingSeconds; // Thời gian còn lại (quan trọng cho tính năng tạm dừng)

    // ... (Các trường khác như testType, currentDifficulty, topicId...)

    private Integer lastQuestionId; // Câu hỏi cuối cùng đã xem/trả lời
    private String answersJson; // JSON string chứa Map<Integer (QuestionId), String (SelectedOption)>

    private Integer currentDifficulty;

    private Integer topicId;
    // -----------------------------------------------------
    // PHẦN LOGIC XỬ LÝ JSON TRONG DTO (HOẶC SERVICE)
    // Dùng transient để báo hiệu trường này không lưu vào DB
    private transient Map<Integer, String> answersMap;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 1. Chuyển JSON string thành Map khi đọc từ DB
    public Map<Integer, String> getAnswersMap() {
        if (this.answersMap == null) {
            if (this.answersJson != null && !this.answersJson.isEmpty()) {
                try {
                    this.answersMap = MAPPER.readValue(
                            this.answersJson,
                            new TypeReference<Map<Integer, String>>() {}
                    );
                } catch (IOException e) {
                    System.err.println("Lỗi parse JSON answers: " + e.getMessage());
                    this.answersMap = new HashMap<>();
                }
            } else {
                this.answersMap = new HashMap<>();
            }
        }
        return this.answersMap;
    }

    // 2. Chuyển Map thành JSON string trước khi lưu vào DB
    public void setAnswersMap(Map<Integer, String> answersMap) {
        this.answersMap = answersMap;
        try {
            this.answersJson = MAPPER.writeValueAsString(answersMap);
        } catch (IOException e) {
            System.err.println("Lỗi serialize JSON answers: " + e.getMessage());
            this.answersJson = "{}"; // Lưu rỗng nếu lỗi
        }
    }
}