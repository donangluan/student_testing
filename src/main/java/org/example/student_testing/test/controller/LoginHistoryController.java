package org.example.student_testing.test.controller;

import org.example.student_testing.test.service.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/login-history")
@PreAuthorize("hasRole('ADMIN')")
public class LoginHistoryController {

    @Autowired
    LoginHistoryService loginHistoryService;

    @GetMapping
    public String loginHistory(Model model) {
        model.addAttribute("logins",loginHistoryService.getAll());
        return "admin/login-history";
    }

    @GetMapping("/{username}")
    public String loginHistoryByUser(Model model, @PathVariable String username){
        model.addAttribute("logins",loginHistoryService.getByUsername(username));
        return "admin/login-history";
    }
}
