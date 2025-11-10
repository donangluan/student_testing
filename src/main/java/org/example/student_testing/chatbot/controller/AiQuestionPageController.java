package org.example.student_testing.chatbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiQuestionPageController {

    @GetMapping("/ai-questions/list")
    public String showForm() {
        return "chatbot/ai-question";
    }
}
