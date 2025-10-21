package org.example.student_testing.test.controller;

import org.example.student_testing.test.entity.ChatbotMessage;
import org.example.student_testing.test.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/admin/chatbot")
public class ChatbotMessageController {


    @Autowired
    private ChatbotService chatbotService;




    @PostMapping("/send")
    public String handleMessage(@RequestParam String message, Model model, Principal principal) {
        String username = principal.getName();
        String role = "STUDENT"; // hoặc lấy từ SecurityContext nếu cần
        String response = chatbotService.reply(message, username, role);

        model.addAttribute("response", response);
        model.addAttribute("message", message);
        model.addAttribute("history", chatbotService.getHistory(username));
        return "/admin/chatbot";
    }

    @GetMapping
    public String showChatPage(Model model, Principal principal) {
        String username = principal.getName();
        model.addAttribute("history", chatbotService.getHistory(username));
        return "/admin/chatbot";
    }
}
