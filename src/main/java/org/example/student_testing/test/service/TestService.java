package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.*;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TestService {

    private final TestMapper testMapper;
    private final QuestionMapper questionMapper;
    @Autowired
    private  QuestionService questionService;

    @Autowired
    private TestQuestionMapper testQuestionMapper;

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

    public void generateUniqueTest(UniqueTestRequest request, String createdBy) {

        TestDTO test = new TestDTO();
        test.setTestName(request.getTestName());
        test.setTestType(request.getTestType());
        test.setCreatedBy(createdBy);
        test.setCreatedAt(LocalDateTime.now());
        test.setTopicId(request.getTopicId());

        testMapper.insertTest(test);




        List<QuestionDTO> selectedQuestions = questionMapper.randomQuestionsByTopic(
                request.getTopicId(), request.getNumberOfQuestions()

        );


        int requested = request.getNumberOfQuestions();
        int actual = selectedQuestions.size();

        if (actual < requested) {
            System.out.println(" Chỉ tìm được " + actual + " câu hỏi phù hợp (yêu cầu: " + requested + ")");
        }
        System.out.println(">> Selected questions: " + selectedQuestions.size());

        System.out.println(">> Topic ID: " + request.getTopicId());

        System.out.println(">> Number of questions: " + request.getNumberOfQuestions());
        System.out.println(">> Selected questions: " + selectedQuestions.size());




        for (String studentUsername : request.getStudentUsername()) {
            if (studentUsername == null || studentUsername.isBlank()) continue;

            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(test.getTestId());
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);



            int order = 1;
            for (QuestionDTO q : selectedQuestions) {

                System.out.println(">> testId = " + test.getTestId());
                System.out.println(">> questionId = " + q.getQuestionId());
                System.out.println(">> studentUsername = " + studentUsername);
                System.out.println(">> difficultyId = " + q.getDifficultyId());
                System.out.println(">> orderNo = " + order);
                testQuestionMapper.insertTestQuestion(
                        test.getTestId(),
                        q.getQuestionId(),
                        studentUsername,
                        q.getDifficultyId(),
                        order++
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
                Question qEntity = toEntity(q);
                testQuestionMapper.insertTestQuestion(
                        testId,
                        qEntity.getQuestionId(),
                        dto.getCreatedBy(),
                        qEntity.getDifficultyId(),
                        order++
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
                testQuestionMapper.insertTestQuestion(
                        testId,
                        q.getQuestionId(),
                        studentUsername,
                        q.getDifficultyId(),
                        order++
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



}
