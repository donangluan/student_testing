package org.example.student_testing.test.controller;

import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.dto.TeacherClassDTO;
import org.example.student_testing.test.service.ClassService;
import org.example.student_testing.test.service.TeacherClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/teacher-class")
public class TeacherClassController {

    @Autowired
    private TeacherClassService teacherClassService;
    @Autowired
    private ClassService classService;

    @Autowired
    private UserService userService;


    @GetMapping("/assign")
    public String showAssignForm(Model model) {
        model.addAttribute("classes", classService.getAllClasses());
        model.addAttribute("teachers", userService.getAllTeachers());
        model.addAttribute("assigned", teacherClassService.getAllTeacherClass());
        System.out.println("Classes: " + classService.getAllClasses());
        System.out.println("Teachers: " + userService.getAllTeachers());
        System.out.println("Assigned: " + teacherClassService.getAllTeacherClass());

        return "admin/teacher-class-assign";
    }

    @PostMapping("/assign")
    public String assignTeacher(@ModelAttribute TeacherClassDTO dto) {
        teacherClassService.assignTeacherToClass(dto);
        return "redirect:/admin/teacher-class/assign";
    }





    @GetMapping("/view")
    public String viewTeacherClasses(@RequestParam String username, Model model) {
        List<Integer> classIds = teacherClassService.getClassIdsByTeacher(username);
        List<ClassDTO> classes = classService.getClassesByIds(classIds);
        model.addAttribute("teacher", userService.getTeacherByUsername(username));
        model.addAttribute("classes", classes);
        return "admin/teacher-class-view";
    }

    @GetMapping("/class-teachers")
    public String viewClassTeachers(@RequestParam Integer classId, Model model) {
        List<String> usernames = teacherClassService.getTeacherUsernamesByClass(classId);
        List<UserDTO> teachers = userService.getTeachersByUsernames(usernames);
        model.addAttribute("class", classService.getClassById(classId));
        model.addAttribute("teachers", teachers);
        return "admin/class-teacher-view";
    }
}
