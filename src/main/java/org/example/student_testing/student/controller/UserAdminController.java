package org.example.student_testing.student.controller;

import org.example.student_testing.student.entity.User;
import org.example.student_testing.student.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserService userService;


    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }


    @GetMapping("/create")
    public String createForm(Model model) {
        User user = new User();
        user.setRoleCode("admin");
        model.addAttribute("user", user);
        return "admin/user-add";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute User user) {
        userService.createUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{username}")
    public String editForm(@PathVariable String username, Model model) {
        model.addAttribute("user", userService.findByUsername(username));
        return "admin/user-update";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute User user) {
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{username}")
    public String delete(@PathVariable String username) {
        userService.deleteUser(username);
        return "redirect:/admin/users";


    }

}
