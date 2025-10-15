package org.example.student_testing;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }

        String role = auth.getAuthorities().iterator().next().getAuthority();
        switch (role) {
            case "ADMIN":
                return "redirect:/admin/dashboard";
            case "TEACHER":
                return "redirect:/tests";
            case "STUDENT":
                return "redirect:/my-tests";
            default:
                return "redirect:/login";
        }
    }

    @GetMapping("/test")
    public String testPage() {
        return "test";
    }
}
