package org.example.student_testing.chatbot.controller;

import org.example.student_testing.chatbot.service.TestAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/teacher/analytics")
public class TestAnalyticsController {

    @Autowired
    private TestAnalyticsService testAnalyticsService;



    @GetMapping
    public String showAnalyticsPage() {
        return "teacher/test/select_test";
    }


    @GetMapping("/{testId}")
    public String analyzeTest(@PathVariable("testId") int testId, Model model) {
        String report = testAnalyticsService.analyzeTest(testId);
        model.addAttribute("report", report);
        return "teacher/test/report";
    }
}
