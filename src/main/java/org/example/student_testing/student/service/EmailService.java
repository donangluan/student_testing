package org.example.student_testing.student.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.example.student_testing.student.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(StudentDTO student) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(student.getEmail());
        helper.setSubject("🎓 Chào mừng đến với hệ thống sinh viên");

        String html = "<h2>Chào " + student.getFullName() + "</h2>"
                + "<p>Mã học viên: <strong>" + student.getStudentId() + "</strong></p>"
                + "<p>Khóa học: <strong>" + student.getCourseName() + "</strong></p>"
                + "<p>Chúc bạn học tập thật tốt! 🎓</p>";

        helper.setText(html, true);
        mailSender.send(message);
    }


    public void sendOtp(String to, String otp)  {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Mã otp đăng kí");
            message.setText("Mã otp của bạn là: "+otp);
            mailSender.send(message);
    }

    public void sendAccountInfo(String to, String username,  String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Tài khoản đã được tạo");
        message.setText("username: "+username +"\nrawPassword: "+rawPassword);
        mailSender.send(message);
    }


}
