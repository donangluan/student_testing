package org.example.student_testing.swingAPI.controllers;

import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.dto.UserLoginDTO;
import org.example.student_testing.student.service.EmailService;
import org.example.student_testing.student.service.OtpService;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.utils.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/swing/rest/api/auth")
@RequiredArgsConstructor
public class AuthRESTController {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/login")   
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO, BindingResult result) {
        if (result.hasErrors()) {
            String error = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(error);
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(), userLoginDTO.getPassword()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return ResponseEntity.ok("token: "+ jwtUtil.generateToken(userDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User or password not match");
        }
    }

    @PostMapping("request-otp")
    public ResponseEntity<?> validate(@Valid @RequestBody UserDTO userDTO, BindingResult result){
        if (result.hasErrors()) {
            String error = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(error);
        }
        if (userService.existsByUsername(userDTO.getUsername())) {
            return ResponseEntity.badRequest().body("username have already exist");
        }

        // ✅ Kiểm tra trùng email
        if (userService.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body("email have already exist");
        }
        String otp = otpService.generateOtp(userDTO.getEmail());
        emailService.sendOtp(userDTO.getEmail(), otp);
        return ResponseEntity.ok("pass validate");
    }

    @PostMapping("verify-otp")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO, @RequestParam("otp") String otp, BindingResult result){
        if (result.hasErrors()) {
            String error = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(error);
        }
        if (!otpService.verifyOtp(userDTO.getEmail(), otp)) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("otp not match");
        }
        userService.register(userDTO);
        otpService.clearOtp(userDTO.getEmail());
        return ResponseEntity.ok("register successfully");
    }
 
}
