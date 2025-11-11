package org.example.student_testing.student.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.apache.poi.ss.usermodel.Workbook;
import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;


    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/list")
    public String listStudent(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              Model model) {
        List<StudentDTO> dto = studentService.getStudentDTOListPaged(page, size);
        int total = studentService.countTotalStudents();
        int totalPages = (int) Math.ceil((double) total / size);
        System.out.println("DTO size: " + dto.size());
        model.addAttribute("dto", dto);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("currentMode", "list");

        model.addAttribute("courses", courseService.getAllCourse());
        return "student/student-list";
    }

    @GetMapping("/add")
    public String addStudent(Model model) {
        model.addAttribute("student", new StudentDTO());

        List<CourseDTO> c = courseService.getAllCourse();
        model.addAttribute("courses", c);
        return "student/student-add";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addStudent(@Valid @ModelAttribute StudentDTO studentDTO,
                             BindingResult bindingResult, Model model,
                             RedirectAttributes redirectAttributes
    ) {



        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentDTO);
            model.addAttribute("org.springframework.validation.BindingResult.student", bindingResult);
            model.addAttribute("courses", courseService.getAllCourse());
            return "student/student-add";
        }


        try {

            studentService.createStudent(studentDTO);

            redirectAttributes.addFlashAttribute("successMessage", " Thêm học viên thành công và đã gửi email!");
            return "redirect:/student/list";

        } catch (MessagingException e) {

            redirectAttributes.addFlashAttribute("successMessage", " Thêm học viên thành công nhưng gửi email thất bại: " + e.getMessage());
            return "redirect:/student/list";

        } catch (Exception e) {

            model.addAttribute("student", studentDTO);
            model.addAttribute("dbError", "Lỗi khi lưu dữ liệu: " + e.getMessage());
            model.addAttribute("courses", courseService.getAllCourse());
            return "student/student-add";
        }


    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{studentId}")
    public String deleteStudent(@PathVariable("studentId") String studentId,
                                RedirectAttributes redirectAttributes){
        studentService.deleteStudent(studentId);
        redirectAttributes.addFlashAttribute("successMessage", "🗑 Xóa học viên thành công!");
        return "redirect:/student/list";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{studentId}")
    public String editStudent(@PathVariable("studentId") String studentId, Model model){
        StudentDTO dto = studentService.getStudentDTOById(studentId);
        System.out.println("CourseId: " + dto.getCourseId());
        List<CourseDTO> c = courseService.getAllCourse();
        model.addAttribute("courses", c);
        model.addAttribute("student",dto);


        return "student/student-edit";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{studentId}")
    public String updateStudent(@PathVariable("studentId") String studentId,
                                @Valid @ModelAttribute StudentDTO studentDTO,
                                BindingResult bindingResult, Model model
    , RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentDTO);
            model.addAttribute("org.springframework.validation.BindingResult.student", bindingResult);
            model.addAttribute("courses", courseService.getAllCourse());
            return "student/student-add";
        }

        System.out.println("Validation errors: " + bindingResult.getAllErrors());

        studentDTO.setStudentId(studentId);

        studentService.updateStudent(studentDTO);
        redirectAttributes.addFlashAttribute("successMessage", " Sửa học viên thành công!");
        model.addAttribute("courses", courseService.getAllCourse());
        return "redirect:/student/list";

    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/search")
    public String searchStudent(@RequestParam("keyword") String keyword,
                                @RequestParam("status") String status,
                                @RequestParam("courseName") String courseName,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                Model model){
        List<StudentDTO> dto = studentService.searchStudentPaged(keyword, status, courseName, page, size);
        int total = studentService.countSearchStudent(keyword , status, courseName);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("dto", dto);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("status", status);
        model.addAttribute("courseName", courseName);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

        model.addAttribute("currentMode", "search");
        model.addAttribute("courses", courseService.getAllCourse());
    return "student/student-list";
    }
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/filter")
    public String filterStudent(@RequestParam("keyword") String keyword,
                                @RequestParam("status") String status,
                                @RequestParam("courseName") String courseName,

                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                Model model){
        List<StudentDTO> dto = studentService.filterStudentPaged( keyword, status, courseName, page, size);
        int total = studentService.countFilterStudent(keyword, status, courseName);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("dto", dto);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseName", courseName);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

        model.addAttribute("currentMode", "filter");
        model.addAttribute("courses", courseService.getAllCourse());
        return "student/student-list";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            List<StudentDTO> students = ExcelHelper.readStudentsFromExcel(file);
            for (StudentDTO s : students) {
                studentService.createStudent(s);
            }
            model.addAttribute("message", " Import thành công " + students.size() + " sinh viên.");
        } catch (Exception e) {
            model.addAttribute("message", " Lỗi import: " + e.getMessage());
        }
        return "student/student-import";
    }
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");

        List<StudentDTO> students = studentService.getStudentDTOList();
        Workbook workbook = ExcelHelper.generateStudentExcel(students);
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    @GetMapping("/change-password")
    public String showChangePasswordForm() {

        return "student/change_password";
    }


    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 Principal principal,
                                 Model model
                                 ) {

        String username = principal.getName();

        if(!newPassword.equals(confirmNewPassword)){
            model.addAttribute("error", "Mật khẩu mới và nhập mật khẩu xác nhận không khớp ");
            return "student/change_password";
        }

        if(newPassword.length() < 6){
            model.addAttribute("error","Độ dài của mật khẩu phải lớn hơn 6 kí tự");
            return "student/change_password";
        }

        if(currentPassword.equals(confirmNewPassword)){
        model.addAttribute("error", "Mật khẩu mới không được trùng với mật khẩu cũ ");
            return "student/change_password";

        }

        boolean succes = userService.changePassword(username, currentPassword, newPassword);

        if(succes){
            model.addAttribute("success", "Đổi mật khẩu thành công");
        }else{
            model.addAttribute("error","Đổi mật khẩu không thành công");
        }

        return "student/change_password";
    }
}
