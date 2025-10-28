package org.example.student_testing;

import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.service.TestService;
import org.example.student_testing.test.service.TestSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private StudentService studentService;

    @Autowired
    private TestService testService;

    @Autowired
    private TestSubmissionService submissionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        int totalStudents = studentService.countAll(); // ✅ Tổng số học viên
        int totalTests = testService.getTotalTests(); // ✅ Tổng số đề
        int gradedSubmissions = submissionService.countGraded(); // ✅ Bài đã chấm

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTests", totalTests);
        model.addAttribute("gradedSubmissions", gradedSubmissions);

        return "admin/dashboard";
    }
}
