package org.example.student_testing.student.controller;


import org.example.student_testing.student.entity.StudentProfile;
import org.example.student_testing.student.service.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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


    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/")
    public String viewOwnStudentProfile(Model model, Authentication authentication) {

        String currentUsername = authentication.getName();


        StudentProfile profile = studentProfileService.findStudentProfileByUsername(currentUsername);

        if (profile == null) {
            model.addAttribute("errorMessage", "Không tìm thấy hồ sơ cá nhân cho người dùng: " + currentUsername);
            return "student/studentprofile-view";
        }

        model.addAttribute("profile", profile);
        return "student/studentprofile-view";
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



    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/create")
    public String showCreateProfileForm(Model model, Authentication authentication) {

        StudentProfile newProfile = new StudentProfile();


        newProfile.setUsername(authentication.getName());


        model.addAttribute("profile", newProfile);


        model.addAttribute("formTitle", "Tạo Hồ sơ cá nhân");


        return "student/studentprofile-form";
    }



    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/save")
    public String saveProfile(@ModelAttribute("profile") StudentProfile profile,
                              Authentication authentication,
                              Model model) {


        if (!profile.getUsername().equals(authentication.getName())) {
            model.addAttribute("errorMessage", "Lỗi bảo mật: Không thể lưu hồ sơ cho người dùng khác.");
            return "student/studentprofile-form";
        }

        try {

            studentProfileService.saveProfile(profile);


            return "redirect:/student/profile/";

        } catch (Exception e) {

            model.addAttribute("errorMessage", "Lỗi khi lưu hồ sơ: " + e.getMessage());
            model.addAttribute("formTitle", "Tạo Hồ sơ cá nhân");
            return "student/studentprofile-form";
        }
    }
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        String username = authentication.getName();

        if(file.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một tập tin ảnh để tải lên");
            return "redirect:/student/profile/";
        }


        try {
            if(file.getContentType() == null || !file.getContentType().startsWith("image/")){
            redirectAttributes.addFlashAttribute("errorMessage", "Định dạng file không hợp lệ. Vui lòng chọn ảnh ");
                return "redirect:/student/profile/";
            }

            studentProfileService.uploadAvatar(username,file);
            redirectAttributes.addFlashAttribute("successMessage", "Ảnh đại diện đã được cập nhật thành công!");
        }catch (IOException e){
            redirectAttributes.addFlashAttribute("errorMessage","Lỗi I/O khi lưu ảnh: " + e.getMessage());
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi cập nhật ảnh: " + e.getMessage());
        }
        return "redirect:/student/profile/";
        }
    }


