package org.example.student_testing;

import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.service.TestService;
import org.example.student_testing.test.service.TestSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String dashboard(Model model,
                            @RequestParam(value = "success", required = false) String success,
                            RedirectAttributes redirectAttributes
                            ) {
        if(success !=null && success.equals("true")){
            redirectAttributes.addFlashAttribute("isLoginSuccess", true);
            System.out.println("Log: login success parameter detected. Setting toast and redirect");
            return "redirect:/admin/dashboard";
        }

        int totalStudents = studentService.countAll();
        int totalTests = testService.getTotalTests();
        int gradedSubmissions = submissionService.countGraded();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTests", totalTests);
        model.addAttribute("gradedSubmissions", gradedSubmissions);

        return "admin/dashboard";
    }
}
