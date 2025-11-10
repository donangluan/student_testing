package org.example.student_testing.student.controller;


import org.example.student_testing.student.entity.StudentProfile;
import org.example.student_testing.student.service.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/student/profile")
public class StudentProfileController {
    @Autowired
    private StudentProfileService studentProfileService;

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("/list")
    public String listStudentProfile(Model model) {
        List<StudentProfile> profileList = studentProfileService.getAllStudentProfiles();
        model.addAttribute("profileList",profileList);
        return "student/studentprofile-list";
    }
    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("")
    public String redirectToList() {
        return "redirect:/student/profile/list";
    }


    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @GetMapping("/view/{studentId}")
    public String viewStudentProfile(@PathVariable("studentId") String studentId, Model model,
                                     Authentication authentication) {
        String currentUsername = authentication.getName();
        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));


        StudentProfile profile = studentProfileService.getStudentProfileById(studentId);

        if (profile == null) {
            model.addAttribute("errorMessage", " Không tìm thấy hồ sơ học viên.");
            return "student/studentprofile-view";
        }

        if (isStudent && !profile.getUsername().equals(currentUsername)) {
            model.addAttribute("errorMessage", " Bạn không được phép xem hồ sơ của người khác.");
            return "student/studentprofile-view";
        }

        model.addAttribute("profile", profile);
        return "student/studentprofile-view";
    }
}
