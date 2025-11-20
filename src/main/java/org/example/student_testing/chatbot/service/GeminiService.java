package org.example.student_testing.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import org.example.student_testing.chatbot.dto.AnswerExplanationRequestDTO;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class.getName());



    private final ObjectMapper mapper = new ObjectMapper();

    @Retry(name = "geminiRetry")
    @CircuitBreaker(name = "geminiCB", fallbackMethod = "fallbackGenerateContent")
    public String chat(String prompt, List<String> history) {
        String fullPrompt = prompt;



        logger.info(">>> [GEMINI API CALL] Sending prompt to Gemini. Prompt length: " + fullPrompt.length());

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

            if (response.getBody() == null || !response.getBody().containsKey("candidates")) {
                logger.error("Gemini API returned empty or invalid body for prompt: {}", fullPrompt);

                throw new AiServiceException("Phản hồi từ AI không hợp lệ, không tìm thấy nội dung.");
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String rawText = (String) parts.get(0).get("text");

            System.out.println("Nội dung trả về từ Gemini:");
            System.out.println(rawText);

            logger.info("Successfully received response from Gemini. Response length: {}", rawText.length());

            return rawText;
        } catch (Exception e) {
            logger.error("Lỗi khi gọi Gemini API. Prompt: {}. Chi tiết lỗi: {}", fullPrompt, e.getMessage(), e);


            throw new AiServiceException("Lỗi khi gọi dịch vụ AI. Vui lòng kiểm tra API key hoặc dịch vụ có bị quá tải.", e);
        }
    }


    public String fallbackGenerateContent(String prompt, List<String> history, Exception e) {
        logger.error(">>> [GEMINI FALLBACK] Dịch vụ AI đang bị lỗi. Trả về phản hồi mặc định.");


        return """
        {
          "error": "Dịch vụ AI hiện không khả dụng. Vui lòng thử lại sau.",
          "questions": [
            {
              "content": "Không thể tạo câu hỏi. Dịch vụ AI bị quá tải hoặc không khả dụng.",
              "optionA": "Không thể tạo",
              "optionB": "Không thể tạo",
              "optionC": "Không thể tạo",
              "optionD": "Không thể tạo",
              "correctAnswer": "A",
              "difficulty": "EASY",
              "topic": "Lỗi AI"
            }
          ]
        }
        """;
    }


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


    public List<AiGeneratedQuestion> parseQuestionsFromJson(String json) {

        logger.info("Attempting to parse JSON response...");

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

                q.setOptions(mapper.writeValueAsString(optionsMap));
                q.setOptionsMap(optionsMap);

                q.setCorrectAnswer(node.get("correctAnswer").asText());
                q.setDifficulty(node.get("difficulty").asText());
                q.setTopic(node.get("topic").asText());
                q.setStatus("PENDING");
                q.setCreatedAt(LocalDateTime.now());

                result.add(q);
                logger.info("Successfully parsed {} questions from JSON.", result.size());
            }
        } catch (Exception e) {

            logger.error("Lỗi khi phân tích JSON từ AI. JSON nhận được: {}", json, e);


            throw new AiServiceException("AI trả về JSON không hợp lệ. Cần điều chỉnh Prompt.", e);
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
            System.out.println(" Danh sách model được phép dùng:");
            System.out.println(response.getBody());
        } catch (Exception e) {
            System.out.println(" Không thể lấy danh sách model: " + e.getMessage());
        }
    }





    public String buildExplanationPrompt(AnswerExplanationRequestDTO dto) {
        boolean isCorrect = dto.getStudentAnswer().equalsIgnoreCase(dto.getCorrectAnswer());

        return """
        Bạn là một gia sư thân thiện, chuyên giải thích bài tập cho học sinh theo cách dễ hiểu, tích cực và truyền cảm hứng.

        === THÔNG TIN CÂU HỎI ===
        Câu hỏi: %s

        Các đáp án:
        A. %s
        B. %s
        C. %s
        D. %s

        Học sinh đã chọn: %s
        Đáp án đúng: %s

        === YÊU CẦU GIẢI THÍCH ===
        %s

         Ví dụ minh họa:
        [Ví dụ thực tế hoặc liên hệ dễ hiểu]

         Mẹo ghi nhớ:
        [1 mẹo ngắn gọn]

        Bạn đã hiểu chưa? Nếu cần mình giải thích thêm thì cứ nói nhé! 

        BẮT ĐẦU GIẢI THÍCH:
        """.formatted(
                dto.getQuestionContent(),
                dto.getOptionA(),
                dto.getOptionB(),
                dto.getOptionC(),
                dto.getOptionD(),
                dto.getStudentAnswer(),
                dto.getCorrectAnswer(),
                isCorrect
                        ? " Bạn đã chọn đúng rồi đó! Giỏi lắm! Cùng xem vì sao đáp án này là chính xác nhé \n\n Tại sao \"" + dto.getCorrectAnswer() + "\" là đúng?\n→ [Giải thích chi tiết 2–3 câu]"
                        : " Tại sao \"" + dto.getStudentAnswer() + "\" không đúng?\n→ [Giải thích 1–2 câu]\n\n Tại sao \"" + dto.getCorrectAnswer() + "\" là đúng?\n→ [Giải thích chi tiết 2–3 câu]"
        );
    }

}
