package org.example.student_testing.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.student_testing.test.dto.TestSessionDTO;
import org.example.student_testing.test.mapper.TestSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    @Autowired
    private TestSessionMapper testSessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void saveOrUpdateSession(TestSessionDTO sessionDTO) {
        try {
            // Chuyển đổi Map sang JSON String trước khi lưu vào DB
            if (sessionDTO.getAnswersMap() != null) {
                String answersJson = objectMapper.writeValueAsString(sessionDTO.getAnswersMap());
                sessionDTO.setAnswersJson(answersJson);
            } else {
                sessionDTO.setAnswersJson(null);
            }

            // Sử dụng một phương thức thống nhất (vì bạn dùng UPSERT)
            testSessionMapper.insertSession(sessionDTO);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting answers map to JSON", e);
        }
    }

    // -----------------------------------------------------------------
    // 2. CHUYỂN ĐỔI KHI ĐỌC (GET)
    // -----------------------------------------------------------------
    public Optional<TestSessionDTO> getSession(Integer testId, String studentUsername) {
        TestSessionDTO sessionDTO = testSessionMapper.findSession(testId, studentUsername);

        if (sessionDTO != null) {
            // Chuyển đổi JSON String sang Map sau khi đọc từ DB
            String answersJson = sessionDTO.getAnswersJson();
            if (answersJson != null && !answersJson.isEmpty()) {
                try {
                    Map<Integer, String> answersMap = objectMapper.readValue(
                            answersJson,
                            new TypeReference<Map<Integer, String>>() {}
                    );
                    sessionDTO.setAnswersMap(answersMap); // Thiết lập lại Map cho DTO
                } catch (JsonProcessingException e) {
                    // Nếu lỗi khi parse JSON, coi như Map trống
                    System.err.println("Error parsing answers JSON for session: " + e.getMessage());
                    sessionDTO.setAnswersMap(new HashMap<>());
                }
            } else {
                sessionDTO.setAnswersMap(new HashMap<>());
            }
            return Optional.of(sessionDTO);
        }
        return Optional.empty();
    }




    @Transactional
    public void clearSession(Integer testId, String studentUsername) {
        testSessionMapper.deleteSession(testId, studentUsername);
    }



}
