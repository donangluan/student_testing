package org.example.student_testing.test.service;


import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.mapper.ChatConversationMapper;
import org.example.student_testing.chatbot.service.AiGenerateQuestionService;
import org.example.student_testing.test.dto.*;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TestService {

    private final TestMapper testMapper;
    private final QuestionMapper questionMapper;
    @Autowired
    private  QuestionService questionService;


    @Autowired
    private  TopicService topicService;
    @Autowired
    private TestQuestionMapper testQuestionMapper;

    @Autowired
    private AiGenerateQuestionService  aiGenerateQuestionService;

    @Autowired
    private ChatConversationMapper chatConversationMapper;



    public TestService(TestMapper testMapper, QuestionMapper questionMapper) {
        this.testMapper = testMapper;
        this.questionMapper = questionMapper;
    }

    public Question toEntity(QuestionDTO dto) {
        Question q = new Question();
        q.setQuestionId(dto.getQuestionId());
        q.setContent(dto.getContent());
        q.setOptionA(dto.getOptionA());
        q.setOptionB(dto.getOptionB());
        q.setOptionC(dto.getOptionC());
        q.setOptionD(dto.getOptionD());
        q.setCorrectOption(dto.getCorrectOption());
        q.setDifficultyId(dto.getDifficultyId());
        q.setTopicId(dto.getTopicId());
        q.setCreatedBy(dto.getCreatedBy());
        q.setCreatedAt(dto.getCreatedAt());
        return q;
    }

    @Transactional
    public void generateUniqueTest(UniqueTestRequest request, String createdBy) {
        // Tạo đề kiểm tra
        TestDTO test = new TestDTO();
        test.setTestName(request.getTestName());
        test.setTestType(request.getTestType());
        test.setCreatedBy(createdBy);
        test.setCreatedAt(LocalDateTime.now());
        test.setTopicId(request.getTopicId());
        test.setDurationMinutes(request.getDurationMinutes());

        testMapper.insertTest(test);


        Integer courseId = topicService.getCourseIdByTopicId(request.getTopicId());


        List<AiGeneratedQuestion> aiQuestions = aiGenerateQuestionService.findByCourseId(courseId);


        aiGenerateQuestionService.convertAiQuestionsToOfficial(aiQuestions);


        List<QuestionDTO> selectedQuestions = questionMapper.randomQuestionsByTopic(
                request.getTopicId(), request.getNumberOfQuestions()
        );

        int requested = request.getNumberOfQuestions();
        int actual = selectedQuestions.size();

        if (actual < requested) {

        }



        //  Gán đề cho từng học sinh
        for (String studentUsername : request.getStudentUsername()) {
            if (studentUsername == null || studentUsername.isBlank()) continue;

            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(test.getTestId());
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);

            int order = 1;
            for (QuestionDTO q : selectedQuestions) {
                Integer qId = q.getQuestionId();
                if (qId == null) continue;

                String source = q.getSource();

                if (source == null || source.isBlank()) {

                    continue;
                }



                testQuestionMapper.insertTestQuestion(
                        test.getTestId(),
                        qId,
                        studentUsername,
                        q.getDifficultyId(),
                        order++,
                        source
                );
            }
        }
    }


    public List<TestDTO> findAll() {
        return testMapper.findAllTests();
    }

    public void createMixedTopicTest(MixedTopicTestDTO dto, List<String> studentUsernames) {
        if (dto.getTopicDistribution() == null || dto.getTopicDistribution().isEmpty()) {
            throw new IllegalArgumentException("Phân phối chủ đề không được để trống.");
        }

        // Tạo đề kiểm tra
        TestDTO testDTO = new TestDTO();
        testDTO.setTestName(dto.getTestName());
        testDTO.setCreatedBy(dto.getCreatedBy());
        testDTO.setTestType("Mixed");
        testDTO.setCreatedAt(LocalDateTime.now());
        testMapper.insertTest(testDTO);

        Integer testId = testDTO.getTestId();
        List<Integer> aiQuestionIds = aiGenerateQuestionService.findAllIds();

        // Gom toàn bộ câu hỏi từ các chủ đề
        List<QuestionDTO> allQuestions = new java.util.ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : dto.getTopicDistribution().entrySet()) {
            Integer topicId = entry.getKey();
            Integer count = entry.getValue();

            if (count == null || count <= 0) continue;

            List<QuestionDTO> questionDTOs = questionMapper.findRandomQuestionsByTopic(topicId, count);
            if (questionDTOs == null || questionDTOs.isEmpty()) {
                System.out.println(" Không tìm thấy câu hỏi cho chủ đề " + topicId);
                continue;
            }

            allQuestions.addAll(questionDTOs);

            // Gán câu hỏi vào đề (chung cho giáo viên)
            int order = 1;
            for (QuestionDTO q : questionDTOs) {
                String source = aiQuestionIds.contains(q.getQuestionId()) ? "ai" : "manual";
                Question qEntity = toEntity(q);
                testQuestionMapper.insertTestQuestion(
                        testId,
                        qEntity.getQuestionId(),
                        dto.getCreatedBy(),
                        qEntity.getDifficultyId(),
                        order++,
                        source
                );
            }
        }

        // Gán đề cho từng học sinh
        for (String studentUsername : studentUsernames) {
            if (studentUsername == null || studentUsername.isBlank()) continue;

            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(testId);
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);

            int order = 1;
            for (QuestionDTO q : allQuestions) {
                String source = aiQuestionIds.contains(q.getQuestionId()) ? "ai" : "manual";
                testQuestionMapper.insertTestQuestion(
                        testId,
                        q.getQuestionId(),
                        studentUsername,
                        q.getDifficultyId(),
                        order++,
                        source
                );
            }
        }

        System.out.println("Đã tạo đề kiểm tra nhiều chủ đề: " + testDTO.getTestName());
    }






    public int getTotalTests() {
        return testMapper.countAllTests();
    }


    public List<TestDTO> findTestsForStudent(String studentUsername) {

        return testMapper.findTestsAssignedToStudent(studentUsername);
    }

    public List<TestResultDTO> getResultsForStudent(String studentUsername) {
        return testMapper.findResultsByStudent(studentUsername);
    }

    public void assignTestToStudent(Integer testId, String studentUsername) {
        TestAssignmentDTO ta = new TestAssignmentDTO();
        ta.setTestId(testId);
        ta.setStudentUsername(studentUsername);
        ta.setAssignedAt(LocalDateTime.now());
        testMapper.insertTestAssignment(ta);
    }

    public TestDTO getTestById(Integer testId) {

        return testMapper.findTestById(testId);
    }



    public void createAiTest(String testName, String topic, List<Integer> questionIds,
                             List<String> studentUsernames, String teacherUsername) {

        // Tạo đề kiểm tra
        TestDTO test = new TestDTO();
        test.setTestName(testName);
        test.setTopicName(topic);
        test.setTestType("AI");
        test.setCreatedBy(teacherUsername);
        test.setCreatedAt(LocalDateTime.now());
        testMapper.insertTest(test);

        // Gán câu hỏi vào đề
        int order = 1;
        for (Integer qId : questionIds) {
            TestQuestionDTO tq = new TestQuestionDTO();
            tq.setTestId(test.getTestId());
            tq.setQuestionId(qId);
            tq.setAssignedBy(teacherUsername);
            tq.setOrderNo(order++);
            testMapper.insertTestQuestion(tq);
        }

        // Gán đề cho học sinh
        for (String studentUsername : studentUsernames) {
            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(test.getTestId());
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);
        }


    }


    public List<String> getAssignedStudents(Integer testId) {
        return testMapper.getAssignedStudents(testId);
    }


    public void assignQuestionsToTest(Integer testId, List<Integer> questionIds) {
        TestDTO test = testMapper.findTestById(testId);
        if (test == null) throw new RuntimeException("Không tìm thấy đề kiểm tra");

        String assignedBy = test.getCreatedBy();
        int order = testQuestionMapper.countQuestionsInTest(testId) + 1;

        List<Integer> aiIds = aiGenerateQuestionService.findAllIds();

        for (Integer qId : questionIds) {
            String source = aiIds.contains(qId) ? "ai" : "manual";

            testQuestionMapper.insertTestQuestionForTest(
                    testId, qId, assignedBy, null, order++, source
            );
        }


    }
    @Transactional
    public Integer getOrCreateConversationId(Integer testId, String studentUsername) {
        Integer existing = testMapper.findConversationId(testId, studentUsername);
        if (existing != null) return existing;

        Integer newId = (int) (System.currentTimeMillis() % 1000000);


        testMapper.insertConversation(testId, studentUsername, newId);


        chatConversationMapper.insertConversation(newId,studentUsername);

        return newId;
    }


}
