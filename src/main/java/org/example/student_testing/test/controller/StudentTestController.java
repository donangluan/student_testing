package org.example.student_testing.test.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.student_testing.test.dto.*;

import org.example.student_testing.test.service.*;
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
public class StudentTestController {

    private final StudentAnswerService answerService;
    private final QuestionService questionService;
    private final TestResultService testResultService;
    private final TestService testService;
    private final TestSubmissionService testSubmissionService;


    @GetMapping("/tests")
    public String showAvailableTests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        List<TestDTO> tests = testService.findTestsForStudent(username);
        Map<Integer, Boolean> testResultMap = new HashMap<>();
        for (TestDTO test : tests) {
            boolean submitted = testResultService.hasSubmitted(test.getTestId(), username);
            testResultMap.put(test.getTestId(), submitted);

            if (submitted) {
                Integer resultId = testResultService.getResultId(test.getTestId(), username);
                test.setResultId(resultId);
            }
        }

        model.addAttribute("tests", tests);
        model.addAttribute("studentUsername", username);
        model.addAttribute("testResultMap", testResultMap);
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
            System.err.println("❌ BỊ CHẶN (Khả dụng): Test ID " + testId + ". Lý do: " + availabilityError.get());
            redirectAttributes.addFlashAttribute("errorMessage", availabilityError.get());
            return "redirect:/student/tests";
        }


        if (testResultService.hasSubmitted(testId, username)) {
            System.err.println("❌ BỊ CHẶN (Đã nộp bài): Test ID " + testId + ". Học sinh: " + username);
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã hoàn thành bài kiểm tra này. Không thể làm lại.");
            return "redirect:/student/tests";
        }

        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);

        if (questions.isEmpty()) {
            System.err.println("❌ BỊ CHẶN (Thiếu câu hỏi): Test ID " + testId + " không có câu hỏi được gán.");
            redirectAttributes.addFlashAttribute("errorMessage", "Bài kiểm tra này chưa được cấu hình câu hỏi.");
            return "redirect:/student/tests";
        }
        TestDTO test = testService.getTestById(testId);
        session.setAttribute("startTime", LocalDateTime.now());
        session.setAttribute("duration", test.getDurationMinutes());
        model.addAttribute("duration", test.getDurationMinutes());
        model.addAttribute("questions", questions);
        model.addAttribute("testId", testId);

        return "test/student/do";
    }
    @PostMapping("/submit")
    public String submitAnswers(@RequestParam Integer testId,
                                @RequestParam Map<String, String> answers,
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
                    System.out.println("Không parse được key: " + entry.getKey());
                }
            }
        }
        LocalDateTime startTime = (LocalDateTime) session.getAttribute("startTime");
        int duration = Optional.ofNullable((Integer) session.getAttribute("duration")).orElse(0);



        LocalDateTime now = LocalDateTime.now();

        boolean isTimeout = Duration.between(startTime, now).toMinutes() > duration;

        session.removeAttribute("startTime");
        session.removeAttribute("duration");

        if (isTimeout) {
            System.out.println("Bài thi đã nộp quá giờ.");
            redirectAttributes.addFlashAttribute("errorMessage", "Bài thi của bạn đã bị nộp do hết giờ.");

            return "redirect:/student/tests";
        }

        System.out.println("Số câu đã nộp: " + parsedAnswers.size());
        answerService.saveAnswers(testId, userDetails.getUsername(), parsedAnswers);

        try {
            // 2. 🚨 BƯỚC SỬA QUAN TRỌNG: THỰC HIỆN CHẤM ĐIỂM
            int score = 0;
            int maxScore = 0;
            int totalAnswered = parsedAnswers.size();

            for (Map.Entry<Integer, String> entry : parsedAnswers.entrySet()) {
                Integer questionId = entry.getKey();
                String selectedOption = entry.getValue();

                String difficulty = questionService.getDifficulty(questionId);
                int weight = switch (difficulty) {
                    case "EASY" -> 1;
                    case "MEDIUM" -> 2;
                    case "HARD" -> 3;
                    default -> 0;
                };
                maxScore += weight;

                String correctOption = questionService.getCorrectOption(questionId);
                if (correctOption != null && correctOption.equalsIgnoreCase(selectedOption)) {
                    score += weight;
                }
            }

            double finalScore = maxScore > 0 ? ((double) score / maxScore) * 10 : 0.0;
            finalScore = Math.round(finalScore * 10.0) / 10.0;

            double percentile = testResultService.calculatePercentile(testId, finalScore);
            String rankCode = testResultService.getRankCode(finalScore);

            // 3. LƯU KẾT QUẢ VÀO DB (TestResult)
            TestResultDTO result = new TestResultDTO();
            result.setTestId(testId);
            result.setStudentUsername(studentUsername);
            result.setScore(finalScore);
            result.setPercentile(percentile);
            result.setRankCode(rankCode);
            result.setCompletedAt(LocalDateTime.now());
            testResultService.save(result);

            // 4. LƯU CHI TIẾT NỘP BÀI (TestSubmission)
            TestSubmissionDTO submission = new TestSubmissionDTO();
            submission.setTestId(testId);
            submission.setStudentUsername(studentUsername);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setTotalAnswered(totalAnswered);
            submission.setCorrectCount(score);
            submission.setScore(finalScore);
            submission.setGraded(true);
            testSubmissionService.save(submission);

            System.out.println("✅ Đã chấm điểm và lưu kết quả thành công cho Test ID " + testId + ". Score: " + finalScore);

        } catch (Exception e) {
            System.err.println("❌ Lỗi chấm điểm sau khi nộp bài: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý kết quả: " + e.getMessage());
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
            System.err.println("❌ Lỗi truy cập kết quả: Học sinh chưa nộp bài hoặc chưa được chấm điểm.");
            return "redirect:/student/tests";
        }


        Integer resultId = testResultService.getResultId(testId, studentUsername);
        TestResultDTO result = testResultService.getResultById(resultId);

        List<StudentAnswerDTO> answers = answerService.getStudentAnswers(testId, studentUsername);
        Map<Integer, String> correctMap = new HashMap<>();
        int correctCount = 0; // <--- KHAI BÁO BIẾN ĐÃ BỊ LỖI
        int total = answers.size();

        for (StudentAnswerDTO ans : answers) {
            // ... (ans.setTestId(testId) không cần thiết ở đây, đã bị xóa)
            String correctOption = questionService.getCorrectOption(ans.getQuestionId());
            correctMap.put(ans.getQuestionId(), correctOption);

            // Cập nhật giá trị cho 'correctCount'
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
