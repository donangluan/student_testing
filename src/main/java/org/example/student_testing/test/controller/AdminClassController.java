package org.example.student_testing.test.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.service.CourseService;
import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.entity.TeacherProfile;
import org.example.student_testing.test.service.ClassService;
import org.example.student_testing.test.service.TeacherProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/classes")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
@RequiredArgsConstructor
public class AdminClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherProfileService teacherProfileService;

    private void addCoursesToModel (Model model) {
        List<CourseDTO> dto = courseService.getAllCourse();
        model.addAttribute("courses", dto);
    }

    private void addTeachersToModel (Model model) {
        List<TeacherProfile> teachers = teacherProfileService.getAllTeachersForDropdown();
        model.addAttribute("teachers", teachers); // Đặt tên là "teachers"
    }


    private static final Logger log = LoggerFactory.getLogger(AdminClassController.class);


    @GetMapping
    public String listClasses(Model model) {

        List<ClassDTO> classes = classService.getAllClasses();


        log.info("📢 Đang tải {} lớp học.", classes.size());
        log.info("📢 Danh sách Lớp học được gửi đến View:");


        for (ClassDTO classDTO : classes) {
            log.info("    -> ID: {}, Tên Lớp: {}, giáo viên: {}, Khóa học: {}, Ngày KT: {}",
                    classDTO.getClassId(),
                    classDTO.getClassName(),
                    classDTO.getTeacherName(),
                    classDTO.getCourseName(),
                    classDTO.getEndDate()
            );
        }
        model.addAttribute("classes", classes);
        return "/admin/class-list";
    }


    @GetMapping("/form")
    public String showClassForm(@RequestParam(required = false) Integer id, Model model, RedirectAttributes redirectAttributes) {

        ClassDTO classDTO = (id != null)
                ? classService.getClassById(id)
                : new ClassDTO();

        model.addAttribute("classDTO", classDTO);
        addCoursesToModel(model);
        addTeachersToModel(model);
        return "admin/class-form";
    }


    @PostMapping("/save")
    public String saveClass(@Valid @ModelAttribute("classDTO") ClassDTO classDTO,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addCoursesToModel(model);
            return "admin/class-form";
        }

        String action = (classDTO.getClassId() == null) ? "Thêm mới" : "Cập nhật";
        classService.saveClass(classDTO);

        redirectAttributes.addFlashAttribute("successMessage", "✅ " + action + " Lớp học thành công!");
        return "redirect:/admin/classes";
    }


    @GetMapping("/delete/{id}")
    public String deleteClass(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            classService.deleteClass(id);
            redirectAttributes.addFlashAttribute("successMessage", "🗑 Xóa Lớp học thành công!");
        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/admin/classes";
    }
}
