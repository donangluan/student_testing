package org.example.student_testing.student.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.example.student_testing.student.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserMapper userMapper;



    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        }

        return "student/login";
    }

}
