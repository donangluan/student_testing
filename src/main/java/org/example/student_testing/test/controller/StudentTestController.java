package org.example.student_testing.test.controller;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.ResultDTO;
import org.example.student_testing.test.dto.StudentAnswerDTO;
import org.example.student_testing.test.service.QuestionService;
import org.example.student_testing.test.service.ResultService;
import org.example.student_testing.test.service.StudentAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentTestController {

    @Autowired
    private StudentAnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ResultService resultService;

    @GetMapping("/do-test")
    public String showTestForm(@RequestParam Integer testId,
                               @RequestParam String studentUsername,
                               Model model) {

        if (resultService.hasSubmitted(testId, studentUsername)) {
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



        double score = correct;
        double percentile = resultService.calculatePercentile(testId, score);
        String rank = resultService.getRank(score);


        ResultDTO result = new ResultDTO();
        result.setTestId(testId);
        result.setStudentUsername(studentUsername);
        result.setScore(score);
        result.setPercentile(percentile);
        result.setRank(rank);
        result.setSubmittedAt(java.time.LocalDateTime.now());

        resultService.save(result);
        model.addAttribute("answers", answers);
        model.addAttribute("correctMap", correctMap);
        model.addAttribute("correct", correct);
        model.addAttribute("total", answers.size());
        model.addAttribute("studentUsername", studentUsername);

        model.addAttribute("score", score);
        model.addAttribute("percentile", percentile);
        model.addAttribute("rank", rank);
        return "test/student/result";
    }
}
