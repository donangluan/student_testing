package org.example.student_testing.student.controller;


import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {


    @Autowired
    private EmailService emailService;

    @GetMapping("/test")
    public String testEmail() {
        StudentDTO s = new StudentDTO();
        s.setEmail("nguyenhaanhphuong7@gmail.com"); // đổi sang email thật để test
        s.setFullName("Nguyễn Hà Anh Phương ăn cứt cho ");
        s.setStudentId("ST14112009");
        s.setCourseName("Phương thối đần mần hâm rồ dại dở người ");

        try {
            emailService.sendWelcomeEmail(s);
            return "✅ Email đã gửi!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Gửi email thất bại: " + e.getMessage();
        }
    }
}
