package org.example.student_testing;

import org.example.student_testing.student.controller.StudentController;
import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.service.CourseService;
import org.example.student_testing.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;



@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;


    @Autowired
    private CourseService courseService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public StudentService studentService() {
            return Mockito.mock(StudentService.class);
        }
        @Bean
        public CourseService courseService() {
            return Mockito.mock(CourseService.class);
        }
    }




    @Test
    public void testAddStudentSuccess() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.setStudentId("SV001");
        dto.setFullName("Nguyễn Văn A");
        dto.setDob(new SimpleDateFormat("yyyy-MM-dd").parse("2007-05-10"));
        dto.setGender(true);
        dto.setEmail("a@student.edu.vn");
        dto.setCourseId(1);
        dto.setStatus("Đang học");
        dto.setUsername("nguyenvana");

        // Giả lập service không throw exception
        Mockito.doNothing().when(studentService).createStudent(any());

        mockMvc.perform(post("/student/add")
                        .with(user("admin").roles("ADMIN")) // giả lập người dùng có quyền
                        .with(csrf()) // thêm token CSRF
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("studentId", dto.getStudentId())
                        .param("fullName", dto.getFullName())
                        .param("dob", "2007-05-10")
                        .param("gender", "true")
                        .param("email", dto.getEmail())
                        .param("courseId", String.valueOf(dto.getCourseId()))
                        .param("status", dto.getStatus())
                        .param("username", dto.getUsername()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/list"));
    }


    @Test
    public void testGetStudentListPage() throws Exception {
        // Giả lập dữ liệu trả về
        StudentDTO student = new StudentDTO();
        student.setGender(true); // hoặc false
        List<StudentDTO> students = List.of(student);
        List<CourseDTO> courses = List.of(new CourseDTO());

        Mockito.when(studentService.getStudentDTOListPaged(anyInt(), anyInt())).thenReturn(students);
        Mockito.when(studentService.countTotalStudents()).thenReturn(1);
        Mockito.when(courseService.getAllCourse()).thenReturn(courses);

        Mockito.when(courseService.getAllCourse()).thenReturn(courses);

        mockMvc.perform(get("/student/list")
                        .with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(view().name("student/student-list"))
                .andExpect(model().attributeExists("dto"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attribute("currentMode", "list"));
    }


    @Test
    public void testDeleteStudentSuccess() throws Exception {
        // Giả lập service không throw lỗi
        Mockito.doNothing().when(studentService).deleteStudent("SV001");

        // Gửi request giả lập
        mockMvc.perform(get("/student/delete/SV001")
                        .with(user("admin").roles("ADMIN")) // giả lập người dùng có quyền
                        .with(csrf())) // thêm token CSRF nếu cần
                .andExpect(status().is3xxRedirection()) // kiểm tra redirect
                .andExpect(redirectedUrl("/student/list")); // kiểm tra đường dẫn chuyển hướng
    }

}


