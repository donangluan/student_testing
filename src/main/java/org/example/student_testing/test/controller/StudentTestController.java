package org.example.student_testing.test.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

        // 1. Kiểm tra tính khả dụng và trạng thái nộp bài (Giữ nguyên)
        Optional<String> availabilityError = testService.isTestAvailable(testId, username);
        if (availabilityError.isPresent()) {
            System.err.println("❌ BỊ CHẶN (1: Khả dụng): Test ID " + testId + ". Lý do: " + availabilityError.get());
            redirectAttributes.addFlashAttribute("errorMessage", availabilityError.get());
            return "redirect:/student/tests";
        }
        if (testResultService.hasSubmitted(testId, username)) {
            System.err.println("❌ BỊ CHẶN (2: Đã nộp bài): Test ID " + testId + ". Học sinh: " + username);
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã hoàn thành bài kiểm tra này. Không thể làm lại.");
            return "redirect:/student/tests";
        }

        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
        if (questions.isEmpty()) {
            System.err.println("❌ BỊ CHẶN (3: Thiếu câu hỏi): Test ID " + testId + " không có câu hỏi được gán.");
            redirectAttributes.addFlashAttribute("errorMessage", "Bài kiểm tra này chưa được cấu hình câu hỏi.");
            return "redirect:/student/tests";
        }

        TestDTO test = testService.getTestById(testId);
        int durationMinutes = Optional.ofNullable(test.getDurationMinutes()).orElse(0);

        // 2. Xử lý Phiên làm bài (TestSession)
        Optional<TestSessionDTO> sessionOpt = testSessionService.getSession(testId, username);

        int initialTimeSeconds;
        Map<Integer, String> studentAnswers;

        int safeDurationMinutes = Optional.ofNullable(test.getDurationMinutes()).orElse(30);
        if (safeDurationMinutes == 0) {
            safeDurationMinutes = 30; // Buộc phải có ít nhất 30 phút nếu DB trả về 0
        }

        if (sessionOpt.isPresent()) {
            // KHÔI PHỤC PHIÊN CŨ (Pause/Resume)
            TestSessionDTO sessions = sessionOpt.get();
            initialTimeSeconds = sessions.getTimeRemainingSeconds();
            studentAnswers = sessions.getAnswersMap();

            System.out.println("✅ KHÔI PHỤC CHI TIẾT:");
            System.out.println(" - Time Remaining: " + initialTimeSeconds + " giây.");
            System.out.println(" - Answers Map Size: " + (studentAnswers != null ? studentAnswers.size() : "NULL") + ".");


            if (initialTimeSeconds <= 0) {
                System.err.println("❌ BỊ CHẶN (4: Hết giờ phiên cũ): Test ID " + testId + " - Thời gian còn lại: " + initialTimeSeconds);
                System.err.println("❌ HẾT GIỜ: Test ID " + testId + " - Thời gian còn lại: " + initialTimeSeconds);
                testSessionService.clearSession(testId, username);
                redirectAttributes.addFlashAttribute("errorMessage", "Bài kiểm tra đã hết thời gian làm bài. Kết quả đã được ghi nhận hoặc bài thi bị hủy.");
                return "redirect:/student/tests";
            }

            System.out.println("✅ KHÔI PHỤC: " + initialTimeSeconds + " giây còn lại.");

        } else {
            // BẮT ĐẦU PHIÊN MỚI
            initialTimeSeconds = safeDurationMinutes * 60; // Chuyển tổng thời gian sang giây
            studentAnswers = new HashMap<>();

            if (initialTimeSeconds <= 0) {
                System.err.println("❌ BỊ CHẶN (5: Thời lượng không hợp lệ): Test ID " + testId + ". Duration Minutes: " + durationMinutes);
                redirectAttributes.addFlashAttribute("errorMessage", "Bài kiểm tra không có thời lượng hợp lệ.");
                return "redirect:/student/tests";
            }

            // **Lưu session mới vào DB**
            TestSessionDTO newSession = new TestSessionDTO();
            newSession.setTestId(testId);
            newSession.setStudentUsername(username);
            newSession.setTimeRemainingSeconds(initialTimeSeconds);
            newSession.setAnswersMap(studentAnswers);
            testSessionService.saveOrUpdateSession(newSession);

            System.out.println("✅ BẮT ĐẦU PHIÊN MỚI: " + initialTimeSeconds + " giây.");
        }

        // 3. Truyền dữ liệu cho View
        // Lưu trữ TỔNG THỜI GIAN VÀ THỜI GIAN CÒN LẠI VÀO MODEL
        model.addAttribute("testId", testId);
        model.addAttribute("questions", questions);
        model.addAttribute("durationMinutes", test.getDurationMinutes());
        model.addAttribute("initialTimeSeconds", initialTimeSeconds); // Dùng cho bộ đếm ngược JS
        model.addAttribute("studentAnswers", studentAnswers); // Dùng để điền lại đáp án đã chọn

        return "test/student/do";
    }
    @PostMapping("/pause")
    public String pauseTestAndRedirect(

            @RequestParam Map<String, String> allParams,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String studentUsername = userDetails.getUsername();
        Map<Integer, String> parsedAnswers = new HashMap<>();



        // 1. Lấy testId (Bắt buộc)
        Integer testId = null;
        if (allParams.containsKey("testId")) {
            try {
                testId = Integer.parseInt(allParams.get("testId"));
            } catch (NumberFormatException ignored) {
                // Nếu testId không phải số, dừng lại
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: ID bài kiểm tra không hợp lệ.");
                return "redirect:/student/tests";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Thiếu ID bài kiểm tra.");
            return "redirect:/student/tests";
        }

        // 2. Phân tích câu trả lời (Chỉ lấy 'q_...')
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                try {
                    // Cắt chuỗi để lấy Question ID
                    Integer questionId = Integer.parseInt(entry.getKey().substring(2));
                    parsedAnswers.put(questionId, entry.getValue());
                } catch (NumberFormatException ignored) {
                    // Bỏ qua các tham số không hợp lệ
                }
            }
        }

        // 3. Lấy thời gian còn lại (đảm bảo không null)
        int timeRemaining = 0;
        if (allParams.containsKey("remainingTimeSeconds")) {
            try {
                timeRemaining = Integer.parseInt(allParams.get("remainingTimeSeconds"));
            } catch (NumberFormatException ignored) {
                // Nếu không phải số, mặc định là 0
            }
        }

        // 4. Cập nhật TestSession với trạng thái mới nhất
        TestSessionDTO sessionDTO = new TestSessionDTO();
        sessionDTO.setTestId(testId);
        sessionDTO.setStudentUsername(studentUsername);
        sessionDTO.setTimeRemainingSeconds(timeRemaining);
        sessionDTO.setAnswersMap(parsedAnswers);

        try {
            testSessionService.saveOrUpdateSession(sessionDTO);
            System.out.println("⏸️ LƯU THÀNH CÔNG (Redirect): Test ID " + testId + ". Đã lưu " + parsedAnswers.size() + " câu trả lời, còn " + timeRemaining + " giây.");

            redirectAttributes.addFlashAttribute("successMessage", "Bài làm đã được lưu lại thành công. Bạn có thể quay lại làm bài test " + testId);
            return "redirect:/student/tests";

        } catch (Exception e) {
            System.err.println("❌ Lỗi lưu session khi tạm dừng: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu bài làm tạm thời: " + e.getMessage());
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
                    System.out.println("Không parse được key: " + entry.getKey());
                }
            }
        }


        System.out.println("Số câu đã nộp: " + parsedAnswers.size());
        answerService.saveAnswers(testId, userDetails.getUsername(), parsedAnswers);

        try {
            // 2. Chấm điểm THỰC TẾ
            int correctCount = 0;
            List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
            int totalQuestions = questions.size();

            // Lặp qua các câu trả lời đã lưu
            for (QuestionDTO q : questions) {
                String studentAnswer = parsedAnswers.get(q.getQuestionId());
                String correctOption = questionService.getCorrectOption(q.getQuestionId()); // Lấy đáp án đúng

                if (studentAnswer != null && correctOption != null && studentAnswer.equalsIgnoreCase(correctOption)) {
                    correctCount++;
                }
            }

            // Tính điểm theo thang 10
            double finalScore = 0.0;
            if (totalQuestions > 0) {
                // Làm tròn đến 2 chữ số thập phân
                finalScore = Math.round(((double) correctCount / totalQuestions) * 1000.0) / 100.0;
            }


            // 3. Lưu TestResult
            TestResultDTO result = new TestResultDTO();
            result.setTestId(testId);
            result.setStudentUsername(studentUsername);
            result.setScore(finalScore);
            result.setCompletedAt(LocalDateTime.now());

            // 🚨 ĐẢM BẢO GỌI PHƯƠNG THỨC SAVE CÓ SẴN (Ví dụ: saveResult)
            // Thay vì testResultService.save(result);
            testResultService.save(result);


            // 4. Xóa session và kết thúc
            testSessionService.clearSession(testId, studentUsername);
            System.out.println("✅ Đã chấm điểm và xóa session thành công cho Test ID " + testId + ". Score: " + finalScore + ". Correct: " + correctCount + "/" + totalQuestions);

        } catch (Exception e) {
            System.err.println("❌ Lỗi chấm điểm sau khi nộp bài: " + e.getMessage());
            e.printStackTrace(); // In stack trace để debug chi tiết
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý kết quả: " + e.getMessage() + ". Vui lòng liên hệ quản trị viên.");
            return "redirect:/student/tests";
        }
        // Xóa các thuộc tính cũ khỏi HTTP Session (giữ nguyên)
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
