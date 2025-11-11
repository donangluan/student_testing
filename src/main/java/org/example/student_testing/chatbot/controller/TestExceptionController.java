package org.example.student_testing.chatbot.controller;

import org.example.student_testing.chatbot.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestExceptionController {


    private final GeminiService geminiService;

    @Autowired
    public TestExceptionController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }


    @GetMapping("/test-ai-error")
    public ResponseEntity<String> testAiError() {
        String testPrompt = "Tạo một câu hỏi về Java Streams";


        String result = geminiService.chat(testPrompt, List.of());


        return ResponseEntity.ok("Gọi AI thành công! Phản hồi: " + result);
    }



}
