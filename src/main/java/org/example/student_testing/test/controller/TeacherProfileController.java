package org.example.student_testing.test.controller;

import org.example.student_testing.test.dto.TeacherProfileDTO;
import org.example.student_testing.test.service.TeacherProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/teachers")
public class TeacherProfileController {

    @Autowired
    private TeacherProfileService  teacherProfileService;

    @GetMapping()
    public String listTeacherProfiles(Model model){

        List<TeacherProfileDTO> list = teacherProfileService.getTeacherProfiles();
        model.addAttribute("teachers",list);
        System.out.println("Số lượng giáo viên: " + list.size());

        return "/admin/teacher-list";
    }

    @GetMapping("/add")
    public String showAddTeacherForm() {
        return "/admin/teacher-add";
    }


    @PostMapping("/add")
    public String addTeacher(

                            @RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String department,
                             @RequestParam String username,
                             @RequestParam String password

    ) {

        teacherProfileService.createTeacherWithAccount(fullName, email, phone, department, username, password);
        return "redirect:/admin/teachers";
    }


    @PostMapping("/update")
    public String updateTeacher(@RequestParam String teacherId,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String department) {

        teacherProfileService.updateTeacher(teacherId, fullName, email, phone, department);
        return "redirect:/admin/teachers";
    }


    @GetMapping("/delete/{teacherId}")
    public String deleteTeacher(@PathVariable String teacherId) {
        teacherProfileService.deleteTeacher(teacherId);
        return "redirect:/admin/teachers";
    }

    @GetMapping("/edit/{teacherId}")
    public String showEditForm(@PathVariable String teacherId, Model model) {
        TeacherProfileDTO teacher = teacherProfileService.getTeacherById(teacherId);
        model.addAttribute("teacher", teacher);
        return "/admin/teacher-edit";
    }




}
