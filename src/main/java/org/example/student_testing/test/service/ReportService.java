package org.example.student_testing.test.service;

import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.dto.TestReportDTO;
import org.example.student_testing.test.dto.TestSubmissionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private StudentService studentService;



    public TestReportDTO buildReportDTO(TestSubmissionDTO submission) {


        String studentUsername = submission.getStudentUsername();

        String studentEmail = studentService.getEmailByUsername(studentUsername);
        String studentFullName = studentService.getFullNameByUsername(studentUsername);

        System.out.println("--- DEBUG REPORT SERVICE ---");
        System.out.println("Username: " + studentUsername);
        System.out.println("Email từ CSDL: " + studentEmail);
        System.out.println("Tên đầy đủ: " + studentFullName);
        System.out.println("----------------------------");

        TestReportDTO report = new TestReportDTO();
        report.setTestName(submission.getTestName());

        report.setStudentUsername(submission.getStudentUsername());


        report.setStudentEmail(studentEmail);
        report.setScore(submission.getScore() != null ? submission.getScore() : 0.0);


        report.setTotalQuestions(submission.getTotalAnswered() != null ? submission.getTotalAnswered() : 0);

        report.setSubmissionTime(submission.getSubmittedAt());
        report.setDuration(getDurationFromSubmission(submission));

        return report;
    }


    private String getDurationFromSubmission(TestSubmissionDTO submission) {


        if (submission.getSubmittedAt() != null) {

            return "Chưa tính duration";
        }
        return "Không xác định";
    }
}
