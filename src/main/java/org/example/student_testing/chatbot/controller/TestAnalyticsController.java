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



    /**
     * Hiển thị form chọn bài test để phân tích
     */
    @GetMapping
    public String showAnalyticsPage() {
        return "teacher/test/select_test"; // View chọn bài test
    }

    /**
     * Phân tích bài test theo testId và hiển thị báo cáo
     */
    @GetMapping("/analyze")
    public String analyzeTest(@RequestParam("testId") int testId, Model model) {
        String report = testAnalyticsService.analyzeTest(testId);
        model.addAttribute("report", report);
        return "teacher/test/report";
    }
}
