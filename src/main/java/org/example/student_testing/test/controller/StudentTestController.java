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
                test.setResultId(resultId); // Gán resultId vào DTO
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
                               Model model) {
        List<QuestionDTO> questions = questionService.getQuestionsByTestIdAndStudent(testId, userDetails.getUsername());
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
                                @AuthenticationPrincipal UserDetails userDetails) {
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
        System.out.println("Số câu đã nộp: " + parsedAnswers.size());
        answerService.saveAnswers(testId, userDetails.getUsername(), parsedAnswers);
        return "redirect:/student/result?testId=" + testId + "&studentUsername=" + userDetails.getUsername();
    }

    @GetMapping("/results")
    public String viewResults(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("results", testService.getResultsForStudent(userDetails.getUsername()));
        return "test/student/results";
    }

    @GetMapping("/do-test")
    public String showTestForm(@RequestParam Integer testId,
                               @RequestParam String studentUsername,
                               Model model) {

        if (testResultService.hasSubmitted(testId, studentUsername)) {
            model.addAttribute("error", "Bạn đã hoàn thành bài kiểm tra này. Không thể làm lại.");
            return "test/student/error";
        }

        List<QuestionDTO> questions = questionService.getQuestionsForTest(testId);
        model.addAttribute("questions", questions);
        model.addAttribute("testId", testId);
        model.addAttribute("studentUsername", studentUsername);
        return "test/student/do-test";
    }

    @PostMapping("/submit-test")
    public String submitTest(@RequestParam Integer testId,
                             @RequestParam String studentUsername,
                             @RequestParam Map<String, String> answers) {

        Map<Integer, String> parsedAnswers = new HashMap<>();
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                Integer questionId = Integer.parseInt(entry.getKey().substring(2));
                parsedAnswers.put(questionId, entry.getValue());
            }
        }

        answerService.saveAnswers(testId, studentUsername, parsedAnswers);
        return "redirect:/student/result?testId=" + testId + "&studentUsername=" + studentUsername;
    }
    @GetMapping("/result")
    public String showResult(@RequestParam Integer testId,
                             @RequestParam String studentUsername,
                             Model model) {


        List<StudentAnswerDTO> answers = answerService.getStudentAnswers(testId, studentUsername);
        Map<Integer, String> correctMap = new HashMap<>();
        int score = 0;
        int maxScore = 0;
        int total = answers.size();

        for (StudentAnswerDTO ans : answers) {
            String difficulty = questionService.getDifficulty(ans.getQuestionId());
            int weight = switch (difficulty) {
                case "EASY" -> 1;
                case "MEDIUM" -> 2;
                case "HARD" -> 3;
                default -> 0;
            };
            maxScore += weight;

            String correctOption = questionService.getCorrectOption(ans.getQuestionId());
            correctMap.put(ans.getQuestionId(), correctOption);
            if (correctOption != null && correctOption.equalsIgnoreCase(ans.getSelectedOption())) {
                score += weight;
            }
        }

        double finalScore = maxScore > 0 ? ((double) score / maxScore) * 10 : 0.0;
        finalScore = Math.round(finalScore * 10.0) / 10.0;


        double percentile = testResultService.calculatePercentile(testId, finalScore);
        String rankCode = testResultService.getRankCode(finalScore);

        if (!testResultService.hasSubmitted(testId, studentUsername)) {
            TestResultDTO result = new TestResultDTO();
            result.setTestId(testId);
            result.setStudentUsername(studentUsername);
            result.setScore(finalScore);
            result.setPercentile(percentile);
            result.setRankCode(rankCode);
            result.setCompletedAt(LocalDateTime.now());
            testResultService.save(result);

            TestSubmissionDTO submission = new TestSubmissionDTO();
            submission.setTestId(testId);
            submission.setStudentUsername(studentUsername);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setTotalAnswered(answers.size());
            submission.setCorrectCount(score);
            submission.setScore(finalScore);
            submission.setGraded(true);
            testSubmissionService.save(submission);
        }

        TestResultDTO result = new TestResultDTO();
        result.setTestId(testId);
        result.setStudentUsername(studentUsername);
        result.setScore(finalScore);
        result.setPercentile(percentile);
        result.setRankCode(rankCode);
        result.setCompletedAt(LocalDateTime.now());

        model.addAttribute("results", List.of(result));
        model.addAttribute("answers", answers);
        model.addAttribute("correctMap", correctMap);
        model.addAttribute("correct", score);
        model.addAttribute("total", total);

        return "test/student/results";
    }

}
