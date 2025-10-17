package org.example.student_testing.student.controller;

import jakarta.validation.Valid;

import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.service.EmailService;
import org.example.student_testing.student.service.OtpService;
import org.example.student_testing.student.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @GetMapping
    public String showRegisterPage(Model model)
        {
            model.addAttribute("userDTO", new UserDTO());
        return "student/register";
        }

    @PostMapping("/request-otp")
    public String requestOtp (@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model){
        if (bindingResult.hasErrors()) {
            return "student/register";
        }

        String otp = otpService.generateOtp(userDTO.getEmail());
        emailService.sendOtp(userDTO.getEmail(), otp);
        redirectAttributes.addFlashAttribute("userDTO", userDTO);
        return "student/verify-otp";
    }


    @PostMapping("/verify-otp")
    public String verifyOtp (@Valid @ModelAttribute("userDTO") UserDTO userDTO
                             , @RequestParam("otp") String otp,
                             BindingResult bindingResult,

                             RedirectAttributes redirectAttributes,
                             Model model
    ){
        if (bindingResult.hasErrors()) {
            return "student/register";
        }



        if (userDTO.getRoleCode() == null || userDTO.getRoleCode().isBlank()) {
            userDTO.setRoleCode("STUDENT");
        }
        // Kiểm tra lại roleCode để tránh lỗi từ service
        if (!"STUDENT".equalsIgnoreCase(userDTO.getRoleCode())) {
            bindingResult.rejectValue("roleCode", "invalid", "Chỉ học viên được phép đăng ký.");
            model.addAttribute("userDTO", userDTO);
            return "student/register";
        }

            if(otpService.verifyOtp(userDTO.getEmail(), otp)){
                userService.register(userDTO);
                emailService.sendAccountInfo(userDTO.getEmail(), userDTO.getUsername(),userDTO.getPassword());
                otpService.clearOtp(userDTO.getEmail());
                redirectAttributes.addFlashAttribute
                        ("succesMessage", "Đăng kí thành công! Vui lòng kiểm tra email ");
                return "redirect:/login";
            }else{
                model.addAttribute("userDTO", userDTO);
                redirectAttributes.addFlashAttribute
                        ("errorMessage", "Mã otp không đúng");
                return "redirect:/register/verify-otp";
            }

    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(@ModelAttribute("userDTO") UserDTO userDTO, Model model) {
        if (!model.containsAttribute("userDTO")) {
            model.addAttribute("userDTO", new UserDTO());
        }
        return "student/verify-otp";
    }
}
