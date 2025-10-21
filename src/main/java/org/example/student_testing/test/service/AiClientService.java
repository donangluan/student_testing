package org.example.student_testing.test.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String ask(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Khai báo URL
            String url = API_URL;

            // Khai báo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Khai báo body
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo");
            body.put("messages", List.of(Map.of("role", "user", "content", message)));
            body.put("temperature", 0.7);
            body.put("max_tokens", 500);

            // Gộp headers + body thành request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Gọi API
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            // Xử lý phản hồi
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");

            return messageObj.get("content").toString();

        } catch (HttpClientErrorException.TooManyRequests e) {
            return "Chatbot hiện đang quá tải hoặc bạn đã hết hạn mức sử dụng. Vui lòng thử lại sau.";
        } catch (Exception e) {
            return "Chatbot gặp lỗi khi xử lý. Vui lòng thử lại sau.";
        }
    }
}
