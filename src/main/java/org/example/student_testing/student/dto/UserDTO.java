package org.example.student_testing.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, max = 20, message = "Username từ 4–20 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;


    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private boolean isLocked;

    private String roleCode;

    private LocalDateTime createdAt;
    private LocalDateTime  updatedAt;

}
