package org.example.student_testing.student.controller;


import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/list")
    public String list(Model model){
        List<CourseDTO> dto = courseService.getAllCourse();
        model.addAttribute("course", dto);
        return "student/course-list";
    }

    @GetMapping("/add")
    public String showAdd(Model model){
        model.addAttribute("course", new CourseDTO());
        return "student/course-add";
    }

    @PostMapping("/add")
    public String addCourse(@ModelAttribute CourseDTO courseDTO){
        courseService.createCourse(courseDTO);
        return "redirect:/course/list";
    }

    @GetMapping("/edit/{courseId}")
    public String showEdit(@PathVariable("courseId") Integer courseId, Model model){
        CourseDTO courseDTO = courseService.getCourseById(courseId);
        model.addAttribute("course", courseDTO);
        return "student/course-edit";
    }

    @PostMapping("/update/{courseId}")
    public String updateCourse(@PathVariable("courseId") Integer courseId, @ModelAttribute CourseDTO courseDTO){
        courseDTO.setCourseId(courseId);
        courseService.updateCourse(courseDTO);
        return "redirect:/course/list";
    }

    @GetMapping("delete/{courseId}")
    public String deleteCourse(@PathVariable("courseId") Integer courseId){
        courseService.deleteCourse(courseId);
        return "redirect:/course/list";
    }
}
