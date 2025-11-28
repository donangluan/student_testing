package org.example.student_testing;

import org.example.student_testing.student.service.ScoreService;
import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.service.TestService;
import org.example.student_testing.test.service.TestSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherDashboardController {



    @Autowired
    private TestService testService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TestSubmissionService testSubmissionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(value = "success", required = false) String success,
                            RedirectAttributes redirectAttributes

                            ) {
        if(success != null && success.equals("true")) {
            redirectAttributes.addFlashAttribute("loginSuccess", true);
            System.out.println("Log: login success parameter detected. Setting Toast and redirecting to clear URL");
            return "redirect:/teacher/dashboard";

        }

        model.addAttribute("totalTests", testService.getTotalTests());
        int gradedSubmissions = testSubmissionService.countGraded();
        model.addAttribute("gradedSubmissions", gradedSubmissions);
        model.addAttribute("totalStudents", studentService.countTotalStudents());
        return "teacher/dashboard";
    }
}
