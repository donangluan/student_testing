package org.example.student_testing.test.controller;


import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.MixedTopicTestDTO;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestDTO;
import org.example.student_testing.test.dto.UniqueTestRequest;
import org.example.student_testing.test.service.QuestionService;
import org.example.student_testing.test.service.TestQuestionService;
import org.example.student_testing.test.service.TestService;
import org.example.student_testing.test.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tests")
public class TestController {


    @Autowired
    private QuestionService questionService;

    private final TestService testService;
    private final TopicService topicService;
    private final UserService userService;



    @Autowired
    private TestQuestionService testQuestionService;

    public TestController(TestService testService, TopicService topicService, UserService userService) {
        this.testService = testService;
        this.topicService = topicService;
        this.userService = userService;
    }

    @GetMapping("/generate")
    public String showAssignForm(@RequestParam Integer testId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        List<QuestionDTO> questions = questionService.getAllQuestions();
        Map<Integer, String> topics = topicService.findAllAsMap();
        model.addAttribute("questions", questions);
        model.addAttribute("testId", testId);
        model.addAttribute("studentUsername", userDetails.getUsername());
        model.addAttribute("topics", topics);
        return "test/test/assign-questions";
    }

    @PostMapping("/generate")
    public String generateTest(@ModelAttribute UniqueTestRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {
        String createdBy = userDetails.getUsername();
        testService.generateUniqueTest(request, createdBy);
        return "redirect:/tests";
    }

    @GetMapping
    public String showTestList(Model model) {
        List<TestDTO> tests = testService.findAll();
        model.addAttribute("tests", tests);
        return "test/test/list";
    }

    @GetMapping("/detail/{testId}")
    public String showTestDetail(@PathVariable Integer testId, Model model) {
        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
        model.addAttribute("questions", questions);
        model.addAttribute("testId", testId);
        return "test/test/detail";
    }


    @PostMapping("/assign")
    public String assignQuestions(@RequestParam Integer testId,
                                  @RequestParam List<Integer> questionIds,
                                  @RequestParam String studentUsername) {
        testQuestionService.assignQuestions(testId, questionIds, studentUsername);
        return "redirect:/tests/detail/" + testId;
    }

    @GetMapping("/assign")
    public String showAssignForm(@RequestParam Integer testId, Model model) {
        List<QuestionDTO> questions = questionService.getAllQuestions();
        Map<Integer, String> topics = topicService.findAllAsMap();
        model.addAttribute("questions", questions);
        model.addAttribute("testId", testId);
        model.addAttribute("studentUsername", "gv001");
        model.addAttribute("topics", topics);
        return "test/test/assign-questions";
    }


    @GetMapping("/create-mixed")
    public String showMixedTopicForm(Model model) {
        model.addAttribute("mixedTestDTO", new MixedTopicTestDTO());
        model.addAttribute("topics", topicService.findAll());
        return "test/test/create-mixed";
    }

    @PostMapping("/create-mixed")
    public String createMixedTopicTest(@ModelAttribute MixedTopicTestDTO mixedTestDTO) {
        testService.createMixedTopicTest(mixedTestDTO);
        return "redirect:/tests";
    }


}
