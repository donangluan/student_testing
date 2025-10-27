package org.example.student_testing.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultDetailController {

    @Autowired
    private TestResultService testResultService;

    @GetMapping("/detail/{resultId}")
    public String viewResultDetail(@PathVariable Integer resultId, Model model) {
        TestResultDTO result = testResultService.getResultById(resultId);
        model.addAttribute("result", result);
        return "test/student/detail";
    }
}
