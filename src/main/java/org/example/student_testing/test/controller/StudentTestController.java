package org.example.student_testing.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.student_testing.test.dto.QuestionDTO;

import org.example.student_testing.test.dto.StudentAnswerDTO;
import org.example.student_testing.test.dto.TestDTO;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.service.QuestionService;
import org.example.student_testing.test.service.StudentAnswerService;
import org.example.student_testing.test.service.TestResultService;
import org.example.student_testing.test.service.TestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentTestController {

    private final StudentAnswerService answerService;
    private final QuestionService questionService;
    private final TestResultService testResultService;
    private final TestService testService;

    @GetMapping("/tests")
    public String showAvailableTests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        List<TestDTO> tests = testService.findTestsForStudent(username);
        Map<Integer, Boolean> testResultMap = new HashMap<>();
        for (TestDTO test : tests) {
            boolean submitted = testResultService.hasSubmitted(test.getTestId(), username);
            testResultMap.put(test.getTestId(), submitted);
        }
        model.addAttribute("tests", tests);
        model.addAttribute("studentUsername", username);
        model.addAttribute("testResultMap", testResultMap);
        return "test/student/list";
    }

    @GetMapping("/do/{testId}")
    public String showTestToDo(@PathVariable Integer testId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        List<QuestionDTO> questions = questionService.getQuestionsByTestIdAndStudent(testId, userDetails.getUsername());
        model.addAttribute("questions", questions);
        model.addAttribute("testId", testId);
        return "test/student/do";
    }

    @PostMapping("/submit")
    public String submitAnswers(@RequestParam Integer testId,
                                @RequestParam Map<String, String> answers,
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
        int correct = 0;

        for (StudentAnswerDTO ans : answers) {
            String correctOption = questionService.getCorrectOption(ans.getQuestionId());
            correctMap.put(ans.getQuestionId(), correctOption);
            if (correctOption != null && correctOption.equalsIgnoreCase(ans.getSelectedOption())) {
                correct++;
            }
        }

        int total = answers.size();
        double score = total > 0 ? ((double) correct / total) * 10 : 0.0;
        score = Math.round(score * 10.0) / 10.0;

        double percentile = testResultService.calculatePercentile(testId, score);
        String rankCode = testResultService.getRankCode(score);

        if (!testResultService.hasSubmitted(testId, studentUsername)) {
            TestResultDTO result = new TestResultDTO();
            result.setTestId(testId);
            result.setStudentUsername(studentUsername);
            result.setScore(score);
            result.setPercentile(percentile);
            result.setRankCode(rankCode);
            result.setCompletedAt(LocalDateTime.now());
            testResultService.save(result);
        }

        TestResultDTO result = new TestResultDTO();
        result.setTestId(testId);
        result.setStudentUsername(studentUsername);
        result.setScore(score);
        result.setPercentile(percentile);
        result.setRankCode(rankCode);
        result.setCompletedAt(LocalDateTime.now());

        model.addAttribute("results", List.of(result));
        model.addAttribute("answers", answers);
        model.addAttribute("correctMap", correctMap);
        model.addAttribute("correct", correct);
        model.addAttribute("total", total);

        return "test/student/results";
    }

}
