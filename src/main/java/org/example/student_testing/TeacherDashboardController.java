package org.example.student_testing;

import org.example.student_testing.student.service.ScoreService;
import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherDashboardController {



    @Autowired
    private TestService testService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalTests", testService.getTotalTests());
        model.addAttribute("gradedSubmissions", scoreService.getGradedCount());
        model.addAttribute("totalStudents", studentService.countTotalStudents());
        return "teacher/dashboard";
    }
}
