package org.example.student_testing.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class StudentDTO {

    @NotBlank(message = "Mã học viên không được để trống")
    private String studentId;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày sinh không được để trống")
    private Date dob;

    @NotNull(message = "Giới tính không được để trống")
    private Boolean gender;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được  để trống")
    private String email;

    @NotNull(message = "Khóa học không được để trống")
    private Integer courseId;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String courseName;
    @NotBlank(message = "Username không được để trống")
    private String username;



}
