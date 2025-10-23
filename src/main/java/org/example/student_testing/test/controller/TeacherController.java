package org.example.student_testing.test.controller;

import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.service.CourseService;
import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.*;
import org.example.student_testing.test.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private  CourseService courseService;

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
    public String showMixedTopicForm(@RequestParam(required = false) List<Integer> selectedCourseIds,
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (selectedCourseIds == null || selectedCourseIds.isEmpty()) {
            model.addAttribute("groupedTopics", null); // để kích hoạt form chọn môn
        }


        List<CourseDTO> allCourses = courseService.getAllCourse();
        Map<CourseDTO, List<TopicDTO>> groupedTopics = new LinkedHashMap<>();

        if (selectedCourseIds != null && !selectedCourseIds.isEmpty()) {
            List<TopicDTO> selectedTopics = topicService.findTopicsByCourseIds(selectedCourseIds);
            Map<Integer, List<TopicDTO>> topicsByCourse = selectedTopics.stream()
                    .collect(Collectors.groupingBy(TopicDTO::getCourseId));

            for (CourseDTO course : allCourses) {
                if (selectedCourseIds.contains(course.getCourseId())) {
                    List<TopicDTO> topicList = topicsByCourse.get(course.getCourseId());
                    if (topicList != null && !topicList.isEmpty()) {
                        groupedTopics.put(course, topicList);
                    }
                }
            }
        }


        model.addAttribute("courses", allCourses);
        model.addAttribute("selectedCourseIds", selectedCourseIds);
        model.addAttribute("groupedTopics", groupedTopics);
        model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
        model.addAttribute("mixedTestDTO", new MixedTopicTestDTO());
        return "teacher/test/create-mixed";
    }

    @PostMapping("/select-courses")
    public String handleCourseSelection(@RequestParam(required = false) List<Integer> selectedCourseIds,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        return showMixedTopicForm(selectedCourseIds, userDetails, model);
    }


    @PostMapping("/create-mixed")
    public String createMixedTopicTest(@ModelAttribute MixedTopicTestDTO mixedTestDTO,
                                       @RequestParam List<String> studentUsernames,
                                       @AuthenticationPrincipal UserDetails userDetails
                                       ) {
        System.out.println("📦 testName = " + mixedTestDTO.getTestName());
        System.out.println("📦 topicDistribution = " + mixedTestDTO.getTopicDistribution());
        System.out.println("📥 selectedCourseIds = " + mixedTestDTO.getSelectedCourseIds());

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
    public String showGenerateForm( @RequestParam(required = false) Integer courseId,
                                    @AuthenticationPrincipal UserDetails userDetails,Model model) {

        List<CourseDTO> courses = courseService.getAllCourse();
        List<TopicDTO> topics = (courseId != null)
                ? topicService.findByCourseId(courseId)
                : new ArrayList<>();
        model.addAttribute("courses", courses);
        model.addAttribute("topics", topics);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
        model.addAttribute("request", new UniqueTestRequest());

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
