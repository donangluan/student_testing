package org.example.student_testing.test.controller;

import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.MixedTopicTestDTO;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestSubmissionDTO;
import org.example.student_testing.test.dto.UniqueTestRequest;
import org.example.student_testing.test.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher/tests")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {



    @Autowired
    private TestService testService;
    @Autowired private QuestionService questionService;
    @Autowired private TopicService topicService;
    @Autowired private TestQuestionService testQuestionService;
    @Autowired private StudentService studentService;
    @Autowired private DifficultyService difficultyService;

    @Autowired
    private TestSubmissionService testSubmissionService;


    @GetMapping
    public String showTestList(@AuthenticationPrincipal UserDetails userDetails,Model model) {
        model.addAttribute("tests", testService.findAll());
        model.addAttribute("studentUsername", userDetails.getUsername());
        return "teacher/test/list";
    }

    @GetMapping("/detail/{testId}")
    public String showTestDetail(@PathVariable Integer testId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        model.addAttribute("questions", questionService.getQuestionsByTestId(testId));

        model.addAttribute("testId", testId);
        return "teacher/test/detail";
    }

    @GetMapping("/assign")
    public String showAssignForm(@RequestParam Integer testId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        model.addAttribute("questions", questionService.getAllQuestions());
        model.addAttribute("topics", topicService.findAllAsMap());
        model.addAttribute("testId", testId);

        // ✅ Chỉ lấy học sinh thuộc lớp của giáo viên
        model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
        return "teacher/test/assign";
    }

    @PostMapping("/assign")
    public String assignQuestions(@RequestParam Integer testId,
                                  @RequestParam List<Integer> questionIds,
                                  @RequestParam String studentUsername) {
        testQuestionService.assignQuestions(testId, questionIds, studentUsername);
        return "redirect:/teacher/tests/detail/" + testId;
    }

    @GetMapping("/create-mixed")
    public String showMixedTopicForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("mixedTestDTO", new MixedTopicTestDTO());
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
        return "teacher/test/create-mixed";
    }

    @PostMapping("/create-mixed")
    public String createMixedTopicTest(@ModelAttribute MixedTopicTestDTO mixedTestDTO,
                                       @RequestParam List<String> studentUsernames,
                                       @AuthenticationPrincipal UserDetails userDetails
                                       ) {
        mixedTestDTO.setCreatedBy(userDetails.getUsername());
        testService.createMixedTopicTest(mixedTestDTO, studentUsernames);
        return "redirect:/teacher/tests";
    }

    @PostMapping("/generate")
    public String generateTest(@ModelAttribute UniqueTestRequest request,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model
                               ) {

        request.setCreatedBy(userDetails.getUsername());
        request.setTestType("Unique");

        if (request.getStudentUsername() == null || request.getStudentUsername().isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một học sinh để gán đề.");
        }


        List<QuestionDTO> selectedQuestions = questionService.previewQuestions(
                request.getTopicId(), request.getNumberOfQuestions()
        );

        if (selectedQuestions.size() < request.getNumberOfQuestions()) {
            model.addAttribute("warning", "⚠️ Chỉ có " + selectedQuestions.size() + " câu hỏi phù hợp với yêu cầu.");
            model.addAttribute("request", request);
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("difficultyLevels", difficultyService.findAll());
            model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
            return "teacher/test/generate";
        }
        testService.generateUniqueTest(request,userDetails.getUsername());
        return "redirect:/teacher/tests";
    }

    @GetMapping("/generate")
    public String showGenerateForm(  @AuthenticationPrincipal UserDetails userDetails,Model model) {
        model.addAttribute("request", new UniqueTestRequest());
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("difficultyLevels", difficultyService.findAll());
        model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));

        return "teacher/test/generate";
    }



    @GetMapping("/submissions")
    public String showTestSubmissions(
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        String teacherUsername = userDetails.getUsername();
        List<TestSubmissionDTO> submissions = testSubmissionService.getAllSubmissionsForTeacher(teacherUsername);
        model.addAttribute("submissions", submissions);

        return "teacher/test/submissions";
    }
}
