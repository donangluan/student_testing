package org.example.student_testing.test.service;

import jakarta.mail.MessagingException;
import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.service.EmailService;
import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestReportDTO;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.dto.TestSubmissionDTO;
import org.example.student_testing.test.mapper.StudentAnswerMapper;
import org.example.student_testing.test.mapper.TestMapper;
import org.example.student_testing.test.mapper.TestSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestSubmissionService {

    @Autowired
    private TestSubmissionMapper testSubmissionMapper;

    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private  TestMapper testMapper;

    @Autowired
    private  TestSessionService testSessionService;

    @Autowired
    private  QuestionService questionService;

    @Autowired
    private  TestResultService testResultService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmailService emailService;




    public List<TestSubmissionDTO> getAllSubmissionsForTeacher( String teacherUsername) {
        return testSubmissionMapper.getAllSubmissionsForTeacher( teacherUsername);
    }

    public int countGraded() {
        return testSubmissionMapper.countGradedSubmissions();
    }

    public void save(TestSubmissionDTO submission) {
        testSubmissionMapper.insert(submission);
    }


    public List<String> findActiveStudentsForTest(Integer testId) {

        return testMapper.findStudentsAssignedButNotSubmitted(testId);
    }


    @Transactional
    public void forceSubmit(Integer testId, String studentUsername) {

        Map<Integer, String> parsedAnswers = new HashMap<>();


        testSessionService.getSession(testId, studentUsername).ifPresent(session -> {
            parsedAnswers.putAll(session.getAnswersMap());
        });


        studentAnswerService.saveAnswers(testId, studentUsername, parsedAnswers);


        int correctCount = 0;
        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
        int totalQuestions = questions.size();

        for (QuestionDTO q : questions) {
            String studentAnswer = parsedAnswers.get(q.getQuestionId());
            String correctOption = questionService.getCorrectOption(q.getQuestionId());

            if (studentAnswer != null && correctOption != null && studentAnswer.equalsIgnoreCase(correctOption)) {
                correctCount++;
            }
        }

        double finalScore = (totalQuestions > 0) ?
                Math.round(((double) correctCount / totalQuestions) * 1000.0) / 100.0 : 0.0;


        TestResultDTO result = new TestResultDTO();
        result.setTestId(testId);
        result.setStudentUsername(studentUsername);
        result.setScore(finalScore);
        result.setCompletedAt(LocalDateTime.now());

        testResultService.save(result);


        testSessionService.clearSession(testId, studentUsername);



        try {

            TestSubmissionDTO submissionDTO = new TestSubmissionDTO();
            submissionDTO.setStudentUsername(studentUsername);
            submissionDTO.setScore(finalScore);
            submissionDTO.setTotalAnswered(totalQuestions);
            submissionDTO.setSubmittedAt(result.getCompletedAt());


            submissionDTO.setTestName(testMapper.findTestNameById(testId));


            TestReportDTO report = reportService.buildReportDTO(submissionDTO);

            System.out.println("--- DEBUG SUBMISSION SERVICE (forceSubmit) ---");
            System.out.println("Email DTO: " + report.getStudentEmail());
            System.out.println("Score DTO: " + report.getScore());
            System.out.println("----------------------------------------------");

            if (report.getStudentEmail() != null && !report.getStudentEmail().isEmpty()) {
                emailService.sendTestReportEmail(report.getStudentEmail(), report);
                System.out.println("DEBUG: Đã gửi báo cáo email sau forceSubmit cho " + report.getStudentEmail());
            }
        } catch (MessagingException e) {
            System.err.println("LỖI GỬI EMAIL BÁO CÁO: " + e.getMessage());

        }
    }


    public void handleSubmissionCompletion(Integer submissionId) {

        TestSubmissionDTO submissionDTO = testSubmissionMapper.findById(submissionId);


        TestReportDTO report = reportService.buildReportDTO(submissionDTO);

        System.out.println("--- DEBUG SUBMISSION SERVICE ---");
        System.out.println("Email DTO: " + report.getStudentEmail());
        System.out.println("Score DTO: " + report.getScore());
        System.out.println("Total Q: " + report.getTotalQuestions());
        System.out.println("--------------------------------");



        try {
            if (report.getStudentEmail() != null && report.getScore() != null) {
                emailService.sendTestReportEmail(report.getStudentEmail(), report);
                System.out.println("DEBUG: Đã gọi lệnh gửi email thành công cho " + report.getStudentEmail());
            }
        } catch (jakarta.mail.MessagingException e) {

            System.err.println("Không gửi được báo cáo cho " + report.getStudentEmail() + ": " + e.getMessage());
        }

    }



    @Transactional
    public TestResultDTO processSubmissionAndReport(Integer testId, String studentUsername, Map<Integer, String> parsedAnswers) {


        studentAnswerService.saveAnswers(testId, studentUsername, parsedAnswers);


        int correctCount = 0;
        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
        int totalQuestions = questions.size();

        for (QuestionDTO q : questions) {
            String studentAnswer = parsedAnswers.get(q.getQuestionId());
            String correctOption = questionService.getCorrectOption(q.getQuestionId());

            if (studentAnswer != null && correctOption != null && studentAnswer.equalsIgnoreCase(correctOption)) {
                correctCount++;
            }
        }

        double finalScore = (totalQuestions > 0) ?
                Math.round(((double) correctCount / totalQuestions) * 1000.0) / 100.0 : 0.0;


        TestResultDTO result = new TestResultDTO();
        result.setTestId(testId);
        result.setStudentUsername(studentUsername);
        result.setScore(finalScore);
        result.setCompletedAt(LocalDateTime.now());

        testResultService.save(result);


        testSessionService.clearSession(testId, studentUsername);


        try {
            TestSubmissionDTO submissionDTO = new TestSubmissionDTO();
            submissionDTO.setStudentUsername(studentUsername);
            submissionDTO.setScore(finalScore);
            submissionDTO.setTotalAnswered(totalQuestions);
            submissionDTO.setSubmittedAt(result.getCompletedAt());
            submissionDTO.setTestName(testMapper.findTestNameById(testId));

            TestReportDTO report = reportService.buildReportDTO(submissionDTO);


            System.out.println("--- DEBUG SUBMISSION SERVICE (processSubmissionAndReport) ---");
            System.out.println("Email DTO: " + report.getStudentEmail());
            System.out.println("Score DTO: " + report.getScore());
            System.out.println("-----------------------------------------------------------");


            if (report.getStudentEmail() != null && !report.getStudentEmail().isEmpty()) {
                emailService.sendTestReportEmail(report.getStudentEmail(), report);
                System.out.println("DEBUG: Đã gọi lệnh gửi email thành công cho " + report.getStudentEmail());
            } else {
                System.out.println("DEBUG: KHÔNG GỬI EMAIL. Lý do: Không tìm thấy Email hợp lệ cho " + studentUsername);
            }
        } catch (MessagingException e) {
            System.err.println("LỖI GỬI EMAIL BÁO CÁO: " + e.getMessage());
        }


        return result;
    }
}


