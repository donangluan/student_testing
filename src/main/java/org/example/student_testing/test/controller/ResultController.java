package org.example.student_testing.test.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.ResultDTO;
import org.example.student_testing.test.service.ResultService;
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
public class ResultController {



    private final ResultService resultService;
    private final TestService testService;
    private final UserService userService;

    @GetMapping
    public String viewResults(@RequestParam(required = false) Integer testId,
                              @RequestParam(required = false) String studentUsername,
                              Model model) {
        List<ResultDTO> results = resultService.filter(testId, studentUsername);
        model.addAttribute("results", results);
        model.addAttribute("tests", testService.findAll());
        model.addAttribute("students", userService.findByUsername(studentUsername));
        model.addAttribute("studentUsername", studentUsername);
        return "test/result/list";
    }

    @GetMapping("/export")
    public void exportCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=results.csv");
        List<ResultDTO> results = resultService.getAllResults();
        resultService.exportCSV(results, response.getWriter());
    }
}
