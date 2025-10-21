package org.example.student_testing.test.controller;

import org.example.student_testing.test.dto.StudentAnswerDTO;
import org.example.student_testing.test.service.StudentAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;

@Controller
@RequestMapping("/teacher")
public class StudentAnswerController {

    @Autowired
    private StudentAnswerService service;

    @GetMapping("/answers")
    public String showAnswers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        List<StudentAnswerDTO> answers = service.getTestsByTeacher(username);
        model.addAttribute("answers", answers);
        System.out.println("Số lượng bài làm: " + answers.size());

        return "teacher/test/answers";
    }
}
