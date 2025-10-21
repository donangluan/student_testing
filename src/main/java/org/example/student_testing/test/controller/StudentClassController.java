package org.example.student_testing.test.controller;

import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.dto.AssignStudentDTO;
import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.entity.StudentClass;
import org.example.student_testing.test.service.ClassService;
import org.example.student_testing.test.service.StudentClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/student-class")
public class StudentClassController {

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassService classService;

    @GetMapping("/assign")
    public String showAssignForm(Model model) {
        model.addAttribute("assignStudentDTO", new AssignStudentDTO());
        model.addAttribute("students", studentClassService.getStudentsNotINAnyClass());
        model.addAttribute("classes", studentClassService.getAllClasses());
        return "/admin/assign-student";
    }

    @PostMapping("/assign")
    public String assignStudent(@ModelAttribute AssignStudentDTO dto,
                                RedirectAttributes redirectAttributes) {
        System.out.println("DEBUG: studentUsername = " + dto.getStudentUsername());
        System.out.println("DEBUG: classId = " + dto.getClassId());
        studentClassService.assignStudentToClass(dto);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Gán học sinh vào lớp thành công!");
        return "redirect:/admin/student-class/assign";
    }

    @GetMapping("/list")
    public String showStudentsByClass(@RequestParam(required = false) Integer classId, Model model) {
        List<ClassDTO> classes = classService.getAllClasses();
        model.addAttribute("classes", classes);
        model.addAttribute("classId", classId);
        System.out.println("Lớp được chọn: " + classId);

        if (classId != null) {
            List<StudentDTO> students = studentService.getStudentsByClassId(classId);
            model.addAttribute("students", students);
        }

        return "admin/student-class-list";
    }
}
