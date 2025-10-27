package org.example.student_testing.test.service;

import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.service.CourseService;
import org.example.student_testing.student.service.StudentService;
import org.example.student_testing.test.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private TestService testService;
    @Autowired private QuestionService questionService;
    @Autowired private TopicService topicService;
    @Autowired private TestQuestionService testQuestionService;
    @Autowired private StudentService studentService;
    @Autowired private DifficultyService difficultyService;
    @Autowired private CourseService courseService;
    @Autowired private TestSubmissionService testSubmissionService;

    public List<CourseDTO> getAllCourses() {
        return courseService.getAllCourse();
    }

    public Map<CourseDTO, List<TopicDTO>> getGroupedTopics(List<Integer> selectedCourseIds) {
        List<CourseDTO> allCourses = courseService.getAllCourse();
        List<TopicDTO> selectedTopics = topicService.findTopicsByCourseIds(selectedCourseIds);
        Map<Integer, List<TopicDTO>> topicsByCourse = selectedTopics.stream()
                .collect(Collectors.groupingBy(TopicDTO::getCourseId));

        Map<CourseDTO, List<TopicDTO>> groupedTopics = new LinkedHashMap<>();
        for (CourseDTO course : allCourses) {
            if (selectedCourseIds.contains(course.getCourseId())) {
                List<TopicDTO> topicList = topicsByCourse.get(course.getCourseId());
                if (topicList != null && !topicList.isEmpty()) {
                    groupedTopics.put(course, topicList);
                }
            }
        }
        return groupedTopics;
    }

    public List<QuestionDTO> previewQuestions(Integer topicId, int numberOfQuestions) {
        return questionService.previewQuestions(topicId, numberOfQuestions);
    }

    public void assignTest(Integer testId, String studentUsername, List<Integer> questionIds) {
        testService.assignTestToStudent(testId, studentUsername);
        testQuestionService.assignQuestions(testId, questionIds, studentUsername);
    }

    public void createMixedTest(MixedTopicTestDTO dto, List<String> studentUsernames, String teacherUsername) {
        dto.setCreatedBy(teacherUsername);
        testService.createMixedTopicTest(dto, studentUsernames);
    }

    public void generateUniqueTest(UniqueTestRequest request, String teacherUsername) {
        request.setCreatedBy(teacherUsername);
        request.setTestType("Unique");
        testService.generateUniqueTest(request, teacherUsername);
    }

    public List<TestSubmissionDTO> getSubmissions(String teacherUsername) {
        return testSubmissionService.getAllSubmissionsForTeacher(teacherUsername);
    }

    public List<StudentDTO> getStudents(String teacherUsername) {
        return studentService.getStudentsForTeacher(teacherUsername);
    }

    public Map<Integer, String> getTopicMap() {
        return topicService.findAllAsMap();
    }

    public List<TopicDTO> getTopicsByCourse(Integer courseId) {
        return topicService.findByCourseId(courseId);
    }

    public List<DifficultyLevelDTO> getDifficultyLevels() {
        return difficultyService.findAll();
    }

    public List<TopicDTO> getTopicsByCourses(List<Integer> courseIds) {
        return topicService.findTopicsByCourseIds(courseIds);
    }
}
