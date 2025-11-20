package org.example.student_testing.test.controller;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.test.dto.AnswerResultDTO;
import org.example.student_testing.test.dto.DynamicAnswerDTO;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.service.DynamicTestService;
import org.example.student_testing.test.service.QuestionService;
import org.example.student_testing.test.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@Controller
@RequestMapping("/student/dynamic")
@RequiredArgsConstructor
public class DynamicTestController {

    private final DynamicTestService service;

    private final TestResultService testResultService;

    @Autowired
    private QuestionService questionService;




    private static final int DEFAULT_DURATION_SECONDS = 30 * 60;



    @GetMapping("/do-test")
    public String start(@RequestParam Integer testId,
                        @RequestParam String studentUsername,
                        @RequestParam(required = false) Integer topicId,
                        @RequestParam String testType,
                        Model model) {

        if (testResultService.hasSubmitted(testId, studentUsername)) {
            model.addAttribute("error", "Bạn đã hoàn thành bài kiểm tra này.");
            return "test/dynamic/finish";
        }

        if (!"Dynamic".equalsIgnoreCase(testType) && topicId == null) {
            model.addAttribute("error",
                    "Lỗi tham số: Cần topicId để bắt đầu bài kiểm tra loại "
                            + testType + ". Vui lòng kiểm tra lại liên kết.");
            return "test/dynamic/finish";
        }

        int currentDifficulty = 2;
        Question question = "Dynamic".equalsIgnoreCase(testType)
                ? service.getNextQuestionMixedByDifficulty(currentDifficulty, studentUsername, testId)
                : service.getNextQuestion(currentDifficulty, studentUsername, testId, topicId);

        if (question == null) {
            model.addAttribute("error", "Không còn câu hỏi phù hợp.");
            return "test/dynamic/finish";
        }

        int answeredCount = service.getAnsweredCount(testId, studentUsername);
        int totalQuestions = service.getRequiredQuestionCount(testId, testType);

        model.addAttribute("question", question);
        model.addAttribute("testId", testId);
        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("currentDifficulty", currentDifficulty);
        model.addAttribute("topicId", topicId);
        model.addAttribute("testType", testType);
        model.addAttribute("options", List.of("A", "B", "C", "D"));
        model.addAttribute("answeredCount", answeredCount);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("timeRemainingSeconds", DEFAULT_DURATION_SECONDS);
        return "test/dynamic/do-test";
    }

    @PostMapping("/submit-test")
    public String submit(@ModelAttribute DynamicAnswerDTO dto,
                         @RequestParam Integer timeRemainingSeconds,Model model) {

        if (!"Dynamic".equalsIgnoreCase(dto.getTestType()) && dto.getTopicId() == null) {
            model.addAttribute("error",
                    "Lỗi tham số:" +
                            " Không thể tiếp tục bài kiểm tra do thiếu ID Chủ đề (topicId) trong dữ liệu submit.");
            return "test/dynamic/finish";
        }
        service.saveAnswer(dto);

        boolean correct = service.checkAnswer(dto.getQuestionId(), dto.getSelectedOption());
        int nextDifficulty = service.nextDifficulty(dto.getCurrentDifficulty(), correct);

        boolean finished = service.isFinished(dto.getTestId(), dto.getStudentUsername());

        Question next = "Dynamic".equalsIgnoreCase(dto.getTestType())
                ? service.getNextQuestionMixedByDifficulty(nextDifficulty, dto.getStudentUsername(), dto.getTestId())
                : service.getNextQuestion(nextDifficulty, dto.getStudentUsername(), dto.getTestId(), dto.getTopicId());

        if (finished || next == null) {
            model.addAttribute("testId", dto.getTestId());
            model.addAttribute("studentUsername", dto.getStudentUsername());
            model.addAttribute("testType", dto.getTestType());
            model.addAttribute("topicId", dto.getTopicId());
            return "test/dynamic/finish";
        }

        int answeredCount = service.getAnsweredCount(dto.getTestId(), dto.getStudentUsername());
        int totalQuestions = service.getRequiredQuestionCount(dto.getTestId(), dto.getTestType());

        if (timeRemainingSeconds <= 0) {
            finished = true;
        }

        model.addAttribute("question", next);
        model.addAttribute("testId", dto.getTestId());
        model.addAttribute("studentUsername", dto.getStudentUsername());
        model.addAttribute("currentDifficulty", nextDifficulty);
        model.addAttribute("topicId", dto.getTopicId());
        model.addAttribute("testType", dto.getTestType());
        model.addAttribute("options", List.of("A", "B", "C", "D"));
        model.addAttribute("answeredCount", answeredCount);
        model.addAttribute("totalQuestions", totalQuestions);

        model.addAttribute("timeRemainingSeconds", timeRemainingSeconds);
        return "test/dynamic/do-test";
    }

    @GetMapping("/result")
    public String result(@RequestParam Integer testId,
                         @RequestParam String studentUsername,
                         @RequestParam(required = false) Integer topicId,
                         @RequestParam String testType,
                         Model model) {

        List<AnswerResultDTO> answers = service.getAnswerResults(testId, studentUsername);
        long correctCount = answers.stream()
                .filter(a -> a.getSelectedOption().equalsIgnoreCase(a.getCorrectOption()))
                .count();

        double score = answers.isEmpty() ? 0 : (correctCount * 10.0) / answers.size();
        double percentile = testResultService.calculatePercentile(testId, score);
        String rank = testResultService.getRankCode(score);

        if (!testResultService.hasSubmitted(testId, studentUsername)) {
            TestResultDTO result = new TestResultDTO();
            result.setTestId(testId);
            result.setStudentUsername(studentUsername);
            result.setScore(score);
            result.setPercentile(percentile);
            result.setRankCode(rank);
            result.setCompletedAt(LocalDateTime.now());
            testResultService.save(result);
        }

        model.addAttribute("answers", answers);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("topicId", topicId);
        model.addAttribute("testType", testType);
        model.addAttribute("totalCount", answers.size());
        model.addAttribute("testId", testId);
        return "test/dynamic/result";
    }


    @GetMapping("/dynamic/do")
    public String showDynamicTest(@RequestParam Integer testId,
                                  @RequestParam String studentUsername,
                                  @RequestParam Integer questionIndex,
                                  Model model) {

        List<QuestionDTO> questions = questionService.getQuestionsByTestIdAndStudent(testId, studentUsername);

        if (questionIndex >= questions.size()) {
            return "redirect:/student/result?testId=" + testId + "&studentUsername=" + studentUsername;
        }

        QuestionDTO currentQuestion = questions.get(questionIndex);

        model.addAttribute("question", currentQuestion);
        model.addAttribute("options", List.of("A", "B", "C", "D"));
        model.addAttribute("answeredCount", questionIndex);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("testId", testId);
        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("testType", "dynamic");
        model.addAttribute("topicId", currentQuestion.getTopicId());


        model.addAttribute("timeRemainingSeconds", DEFAULT_DURATION_SECONDS);

        return "test/dynamic/do-test";
    }


}
