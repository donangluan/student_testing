package org.example.student_testing.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.student_testing.test.dto.SubmissionViewDTO;
import org.example.student_testing.test.service.AdminTestViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/tests")
@RequiredArgsConstructor
public class AdminTestViewController {

    private final AdminTestViewService adminTestViewService;

    @GetMapping("/results/{testId}")
    public String viewSubmissions(@PathVariable Integer testId, Model model) {
        List<SubmissionViewDTO> submissions = adminTestViewService.getSubmissionsWithAnswers(testId);
        model.addAttribute("submissions", submissions);
        return "admin/results";
    }
}
