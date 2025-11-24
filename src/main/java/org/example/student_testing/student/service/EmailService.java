package org.example.student_testing.student.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.test.dto.TestReportDTO;
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

    public void sendTestReportEmail(String toEmail, TestReportDTO report) throws  MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("Báo cáo kết quả bài kiểm tra: "+report.getTestName());

        String html = buildTestReportHtml(report);
        helper.setText(html, true);
        mailSender.send(message);
    }


    public String buildTestReportHtml(TestReportDTO report) {
        double percentage = (report.getTotalQuestions() > 0)
                ? (report.getScore() * 100.0 / report.getTotalQuestions())
                : 0;

        String scoreColor = report.getScore() >= (report.getTotalQuestions() * 0.8) ? "#198754" : "#ffc107";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>");
        sb.append("<div style='max-width: 600px; margin: 0 auto; background-color:" +
                " #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>");

        sb.append("<h2 style='color: #007bff; border-bottom: 2px solid #007bff;" +
                " padding-bottom: 10px;'>Báo cáo Kết quả Bài kiểm tra</h2>");

        sb.append("<p>Xin chào <strong>").append(report.getStudentUsername()).append("</strong>,</p>");
        sb.append("<p>Bài kiểm tra <strong>").append(report.getTestName()).append("</strong> " +
                "của bạn đã có kết quả. Dưới đây là tóm tắt:</p>");


        sb.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
        sb.append("<tr><td style='border: 1px solid #ddd; padding: 12px; background-color:" +
                " #e9ecef; width: 40%;'><strong>Thời gian nộp bài:</strong></td><td style='border:" +
                " 1px solid #ddd; padding: 12px;'>").append(report.getSubmissionTime()).append("</td></tr>");
        sb.append("<tr><td style='border: 1px solid #ddd; padding: 12px; background-color:" +
                " #e9ecef;'><strong>Thời gian làm bài:</strong></td><td style='border: " +
                "1px solid #ddd; padding: 12px;'>").append(report.getDuration()).append("</td></tr>");
        sb.append("<tr><td style='border: 1px solid #ddd; padding: 12px; background-color:" +
                " #e9ecef;'><strong>Điểm số:</strong></td><td style='border: " +
                "1px solid #ddd; padding: 12px; font-size: 1.1em; color:" +
                " ").append(scoreColor).append(";'><strong>").append(String.format("%.1f",
                report.getScore())).append(" / ").append(report.getTotalQuestions()).append("</strong></td></tr>");
        sb.append("<tr><td style='border: 1px solid #ddd; padding: 12px; background-color:" +
                " #e9ecef;'><strong>Tỷ lệ Đúng:</strong></td><td style='border: 1px solid #ddd; " +
                "padding: 12px;'>").append(String.format("%.2f", percentage)).append("%</td></tr>");
        sb.append("</table>");

        sb.append("<p style='margin-top: 30px;'>Để xem chi tiết kết quả, " +
                "vui lòng đăng nhập vào hệ thống học tập của chúng tôi.</p>");
        sb.append("<p style='text-align: center; margin-top: 20px;'><a href='#'" +
                " style='display: inline-block; padding: 10px 20px; background-color: " +
                "#007bff; color: white; text-decoration: none; border-radius: 5px;'>Đăng nhập Hệ thống</a></p>");

        sb.append("<p style='font-size: 0.9em; color: #6c757d; margin-top: 30px;'>" +
                "Trân trọng,<br>Đội ngũ Hỗ trợ Giáo dục</p>");
        sb.append("</div></body></html>");

        return sb.toString();

    }


}
