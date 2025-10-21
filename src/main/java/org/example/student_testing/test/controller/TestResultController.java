package org.example.student_testing.test.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.service.TestResultService;
import org.example.student_testing.test.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/results")
@RequiredArgsConstructor
public class TestResultController {



    private final TestResultService testResultService;
    private final TestService testService;
    private final UserService userService;


    @GetMapping
    public String viewResults(@RequestParam(required = false) Integer testId,
                              @RequestParam(required = false) String studentUsername,
                              @RequestParam(required = false) Double minScore,
                              @RequestParam(required = false) Double maxScore,
                              @RequestParam(required = false) String rankCode,
                              Model model) {

        List<TestResultDTO> results = testResultService.filter(testId, studentUsername, minScore, maxScore, rankCode);

        model.addAttribute("results", results);
        model.addAttribute("tests", testService.findAll());
        model.addAttribute("students", userService.findAllStudents());
        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("testId", testId);
        model.addAttribute("minScore", minScore);
        model.addAttribute("maxScore", maxScore);
        model.addAttribute("rankCode", rankCode);
        return "test/result/list";
    }

    @GetMapping("/export")
    public void exportCSV(@RequestParam(required = false) Integer testId,
                          @RequestParam(required = false) String studentUsername,
                          @RequestParam(required = false) Double minScore,
                          @RequestParam(required = false) Double maxScore,
                          @RequestParam(required = false) String rankCode,
                          HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=test_results.csv");

        List<TestResultDTO> results = testResultService.filter(testId, studentUsername, minScore, maxScore, rankCode);
        testResultService.exportCSV(results, response.getWriter());
    }

}
