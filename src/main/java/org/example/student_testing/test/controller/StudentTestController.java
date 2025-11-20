package org.example.student_testing.test.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.student_testing.test.dto.*;

import org.example.student_testing.test.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
@Slf4j
public class StudentTestController {

    private final StudentAnswerService answerService;
    private final QuestionService questionService;
    private final TestResultService testResultService;
    private final TestService testService;
    private final TestSubmissionService testSubmissionService;

    private final TestSessionService testSessionService;


    @GetMapping("/tests")
    public String showAvailableTests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        List<TestDTO> tests = testService.findTestsForStudent(username);
        Map<Integer, Boolean> testResultMap = new HashMap<>();
        Map<Integer,Boolean> testExpiedMap = new HashMap<>();
        for (TestDTO test : tests) {
            boolean submitted = testResultService.hasSubmitted(test.getTestId(), username);
            testResultMap.put(test.getTestId(), submitted);

            boolean isExpired = false;
            if (test.getEndTime() != null) {
                isExpired = test.getEndTime().isBefore(LocalDateTime.now());
            }
            testExpiedMap.put(test.getTestId(), isExpired);

            if (submitted) {
                Integer resultId = testResultService.getResultId(test.getTestId(), username);
                test.setResultId(resultId);
            }
        }

        model.addAttribute("tests", tests);
        model.addAttribute("studentUsername", username);
        model.addAttribute("testResultMap", testResultMap);
        model.addAttribute("testExpiredMap", testExpiedMap);
        return "test/student/list";
    }

    @GetMapping("/do/{testId}")
    public String showTestToDo(@PathVariable Integer testId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String username = userDetails.getUsername();


        Optional<String> availabilityError = testService.isTestAvailable(testId, username);
        if (availabilityError.isPresent()) {
            log.warn("BỊ CHẶN (1: Khả dụng): Test ID {}. Lý do: {}", testId, availabilityError.get());
            redirectAttributes.addFlashAttribute("errorMessage", availabilityError.get());
            return "redirect:/student/tests";
        }
        if (testResultService.hasSubmitted(testId, username)) {
            log.warn("BỊ CHẶN (2: Đã nộp bài): Test ID {}. Học sinh: {}", testId, username);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Bạn đã hoàn thành bài kiểm tra này. Không thể làm lại.");
            return "redirect:/student/tests";
        }

        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
        if (questions.isEmpty()) {
            log.error("BỊ CHẶN (3: Thiếu câu hỏi): Test ID {} không có câu hỏi được gán.", testId);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Bài kiểm tra này chưa được cấu hình câu hỏi.");
            return "redirect:/student/tests";
        }

        TestDTO test = testService.getTestById(testId);

        LocalDateTime testEndTime = test.getEndTime();
        int maxRemainingSeconds = Integer.MAX_VALUE;

        if(testEndTime != null) {
            LocalDateTime now = LocalDateTime.now();

            if(testEndTime.isAfter(now)) {
                maxRemainingSeconds = (int) Duration.between( now, testEndTime).getSeconds();
            }else{
                log.warn("Bị chặn vì hết hạn chung: Test ID {}", testId);
                testSessionService.clearSession(testId,username);
                redirectAttributes.
                        addFlashAttribute("errorMessage",
                                "Bài kiểm tra của bạn đã hết thời gian nộp chung");
                return "redirect:/student/tests";
            }
        }


        Optional<TestSessionDTO> sessionOpt = testSessionService.getSession(testId, username);

        int initialTimeSeconds;
        Map<Integer, String> studentAnswers;

        int safeDurationMinutes = Optional.ofNullable(test.getDurationMinutes()).orElse(30);

        int testDurationSeconds = safeDurationMinutes * 60;

        if (sessionOpt.isPresent()) {

            TestSessionDTO sessions = sessionOpt.get();
            initialTimeSeconds = sessions.getTimeRemainingSeconds();
            studentAnswers = sessions.getAnswersMap();

            if (initialTimeSeconds > maxRemainingSeconds) {
                initialTimeSeconds = maxRemainingSeconds;
            }

            if (initialTimeSeconds <= 0) {
                log.error("BỊ CHẶN (4: Hết giờ phiên cũ): Test ID {} - Thời gian còn lại: {}", testId, initialTimeSeconds);
                testSessionService.clearSession(testId, username);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Bài kiểm tra đã hết thời gian làm bài. Kết quả đã được ghi nhận hoặc bài thi bị hủy.");
                return "redirect:/student/tests";
            }

            log.info("KHÔI PHỤC SESSION: Test ID {}. {} giây còn lại.", testId, initialTimeSeconds);
        } else {

            initialTimeSeconds = testDurationSeconds;

            if (initialTimeSeconds > maxRemainingSeconds) {
                initialTimeSeconds = maxRemainingSeconds;
            }

            studentAnswers = new HashMap<>();

            if (initialTimeSeconds <= 0) {

                redirectAttributes.addFlashAttribute("errorMessage",
                        "Bài kiểm tra không có thời lượng hợp lệ hoặc đã hết hạn.");
                return "redirect:/student/tests";
            }


            TestSessionDTO newSession = new TestSessionDTO();
            newSession.setTestId(testId);
            newSession.setStudentUsername(username);
            newSession.setTimeRemainingSeconds(initialTimeSeconds);
            newSession.setAnswersMap(studentAnswers);
            testSessionService.saveOrUpdateSession(newSession);

            log.info("BẮT ĐẦU PHIÊN MỚI: Test ID {}. {} giây.", testId, initialTimeSeconds);
        }


        model.addAttribute("testId", testId);
        model.addAttribute("questions", questions);
        model.addAttribute("durationMinutes", test.getDurationMinutes());
        model.addAttribute("initialTimeSeconds", initialTimeSeconds);
        model.addAttribute("studentAnswers", studentAnswers);

        model.addAttribute("testEndTime", test.getEndTime());

        return "test/student/do";
    }
    @PostMapping("/pause")
    public String pauseTestAndRedirect(

            @RequestParam Map<String, String> allParams,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String studentUsername = userDetails.getUsername();
        Map<Integer, String> parsedAnswers = new HashMap<>();




        Integer testId = null;
        if (allParams.containsKey("testId")) {
            try {
                testId = Integer.parseInt(allParams.get("testId"));
            } catch (NumberFormatException ignored) {

                log.error("Lỗi: ID bài kiểm tra không hợp lệ khi tạm dừng.");
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Lỗi: ID bài kiểm tra không hợp lệ.");
                return "redirect:/student/tests";
            }
        } else {
            log.error("Lỗi: Thiếu ID bài kiểm tra khi tạm dừng.");
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Thiếu ID bài kiểm tra.");
            return "redirect:/student/tests";
        }


        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                try {

                    Integer questionId = Integer.parseInt(entry.getKey().substring(2));
                    parsedAnswers.put(questionId, entry.getValue());
                } catch (NumberFormatException ignored) {

                }
            }
        }


        int timeRemaining = 0;
        if (allParams.containsKey("remainingTimeSeconds")) {
            try {
                timeRemaining = Integer.parseInt(allParams.get("remainingTimeSeconds"));
            } catch (NumberFormatException ignored) {

            }
        }


        TestSessionDTO sessionDTO = new TestSessionDTO();
        sessionDTO.setTestId(testId);
        sessionDTO.setStudentUsername(studentUsername);
        sessionDTO.setTimeRemainingSeconds(timeRemaining);
        sessionDTO.setAnswersMap(parsedAnswers);

        try {
            testSessionService.saveOrUpdateSession(sessionDTO);
            log.info("LƯU THÀNH CÔNG (Pause): Test ID {}. Đã lưu {} câu trả lời, còn {} giây.",
                    testId, parsedAnswers.size(), timeRemaining);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Bài làm đã được lưu lại thành công. Bạn có thể quay lại làm bài test " + testId);
            return "redirect:/student/tests";

        } catch (Exception e) {
            log.error("Lỗi khi lưu session tạm dừng cho Test ID {}: {}", testId, e.getMessage(), e);
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi lưu bài làm tạm thời: " + e.getMessage());
            return "redirect:/student/do/" + testId;
        }
    }

    @PostMapping("/submit")
    public String submitAnswers(@RequestParam Integer testId,
                                @RequestParam Map<String, String> answers,
                                @RequestParam(required = false) Integer remainingTimeSeconds,
                                HttpSession session,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes
    ) {
        String studentUsername = userDetails.getUsername();

        Map<Integer, String> parsedAnswers = new HashMap<>();
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                try {
                    Integer questionId = Integer.parseInt(entry.getKey().substring(2));
                    parsedAnswers.put(questionId, entry.getValue());
                } catch (NumberFormatException e) {
                    log.warn("Không parse được key khi nộp bài: {}", entry.getKey());
                }
            }
        }


        log.info("Nộp bài cho Test ID {}. Số câu đã nộp: {}", testId, parsedAnswers.size());
        answerService.saveAnswers(testId, userDetails.getUsername(), parsedAnswers);

        try {

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


            double finalScore = 0.0;
            if (totalQuestions > 0) {

                finalScore = Math.round(((double) correctCount / totalQuestions) * 1000.0) / 100.0;
            }



            TestResultDTO result = new TestResultDTO();
            result.setTestId(testId);
            result.setStudentUsername(studentUsername);
            result.setScore(finalScore);
            result.setCompletedAt(LocalDateTime.now());


            testResultService.save(result);



            testSessionService.clearSession(testId, studentUsername);
            log.info("Đã chấm điểm và xóa session thành công cho Test ID {}. Score: {}. Correct: {}/{}",
                    testId, finalScore, correctCount, totalQuestions);

        } catch (Exception e) {
            log.error("Lỗi chấm điểm sau khi nộp bài cho Test ID {}: {}", testId, e.getMessage(), e);
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi xử lý kết quả: " + e.getMessage() + ". Vui lòng liên hệ quản trị viên.");
            return "redirect:/student/tests";
        }

        session.removeAttribute("startTime");
        session.removeAttribute("duration");

        return "redirect:/student/result?testId=" + testId + "&studentUsername=" + userDetails.getUsername();
    }

    @GetMapping("/results")
    public String viewResults(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        model.addAttribute("results", testService.getResultsForStudent(userDetails.getUsername()));
        return "test/student/results";
    }




    @GetMapping("/result")
    public String showResult(@RequestParam Integer testId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {

        String studentUsername = userDetails.getUsername();

        if (!testResultService.hasSubmitted(testId, studentUsername)) {
            log.warn("Lỗi truy cập kết quả: Học sinh {} chưa nộp bài hoặc chưa được chấm điểm cho Test ID {}",
                    studentUsername, testId);
            return "redirect:/student/tests";
        }


        Integer resultId = testResultService.getResultId(testId, studentUsername);
        TestResultDTO result = testResultService.getResultById(resultId);

        List<StudentAnswerDTO> answers = answerService.getStudentAnswers(testId, studentUsername);
        Map<Integer, String> correctMap = new HashMap<>();
        int correctCount = 0; //
        int total = answers.size();

        for (StudentAnswerDTO ans : answers) {

            String correctOption = questionService.getCorrectOption(ans.getQuestionId());
            correctMap.put(ans.getQuestionId(), correctOption);


            if (correctOption != null && correctOption.equalsIgnoreCase(ans.getSelectedOption())) {
                correctCount++;
            }
        }








        Integer conversationId = testService.getOrCreateConversationId(testId, studentUsername);


        model.addAttribute("results", List.of(result));
        model.addAttribute("answers", answers);
        model.addAttribute("correctMap", correctMap);
        model.addAttribute("correct", correctCount);
        model.addAttribute("total", total);
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("testId", testId);


        return "test/student/results";
    }


}
