package org.example.student_testing.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";





    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Gửi prompt đến AI và nhận lại nội dung trả về (text hoặc JSON).
     * @param prompt Nội dung yêu cầu gửi đến AI
     * @param history Danh sách hội thoại trước đó (nếu có)
     * @return Nội dung trả về từ AI
     */
    public String chat(String prompt, List<String> history) {
        String fullPrompt = prompt;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = ENDPOINT + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String rawText = (String) parts.get(0).get("text");

            System.out.println("✅ Nội dung trả về từ Gemini:");
            System.out.println(rawText);

            return rawText;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Gemini: " + e.getMessage(), e);
        }
    }

    /**
     * Tách phần JSON ra khỏi văn bản trả về từ AI.
     * @param rawText Văn bản gốc từ AI (có thể chứa cả text và JSON)
     * @return Chuỗi JSON đã được tách ra
     */
    public String extractJsonFromText(String rawText) {
        int start = -1, end = -1, braceCount = 0;
        for (int i = 0; i < rawText.length(); i++) {
            char c = rawText.charAt(i);
            if (start == -1 && (c == '{' || c == '[')) {
                start = i;
                braceCount = 1;
                continue;
            }
            if (start != -1) {
                if (c == '{' || c == '[') braceCount++;
                if (c == '}' || c == ']') braceCount--;
                if (braceCount == 0) {
                    end = i + 1;
                    break;
                }
            }
        }
        if (start != -1 && end != -1) {
            return rawText.substring(start, end).trim();
        } else {
            throw new RuntimeException("Không tìm thấy đoạn JSON hợp lệ trong văn bản AI");
        }
    }

    /**
     * Phân tích chuỗi JSON thành danh sách câu hỏi AI đã sinh.
     * @param json Chuỗi JSON chứa mảng "questions"
     * @return Danh sách câu hỏi đã parse thành object
     */
    public List<AiGeneratedQuestion> parseQuestionsFromJson(String json) {
        List<AiGeneratedQuestion> result = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode questionsNode = root.get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                throw new RuntimeException("Không tìm thấy mảng 'questions' trong JSON");
            }

            for (JsonNode node : questionsNode) {
                AiGeneratedQuestion q = new AiGeneratedQuestion();
                q.setQuestionContent(node.get("content").asText());

                Map<String, String> optionsMap = new LinkedHashMap<>();
                optionsMap.put("A", node.get("optionA").asText());
                optionsMap.put("B", node.get("optionB").asText());
                optionsMap.put("C", node.get("optionC").asText());
                optionsMap.put("D", node.get("optionD").asText());

                q.setOptions(mapper.writeValueAsString(optionsMap)); // lưu vào DB
                q.setOptionsMap(optionsMap);                         // dùng cho Thymeleaf

                q.setCorrectAnswer(node.get("correctAnswer").asText());
                q.setDifficulty(node.get("difficulty").asText());
                q.setTopic(node.get("topic").asText());
                q.setStatus("PENDING");
                q.setCreatedAt(LocalDateTime.now());

                result.add(q);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi phân tích JSON từ AI", e);
        }
        return result;
    }

    @PostConstruct
    public void checkKey() {
        System.out.println("API key loaded: [" + apiKey + "]");
    }

    @PostConstruct
    public void listModels() {
        String url = "https://generativelanguage.googleapis.com/v1/models?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("✅ Danh sách model được phép dùng:");
            System.out.println(response.getBody());
        } catch (Exception e) {
            System.out.println("❌ Không thể lấy danh sách model: " + e.getMessage());
        }
    }
}
