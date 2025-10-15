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
    private TestQuestionMapper testQuestionMapper;

    public TestService(TestMapper testMapper, QuestionMapper questionMapper) {
        this.testMapper = testMapper;
        this.questionMapper = questionMapper;
    }

    public void generateUniqueTest(UniqueTestRequest request, String createdBy) {

        TestDTO test = new TestDTO();
        test.setTestName(request.getTestName());
        test.setTestType(request.getTestType());
        test.setCreatedBy(createdBy);
        test.setCreatedAt(LocalDateTime.now());
        test.setTopicId(request.getTopicId());

        testMapper.insertTest(test);




        List<QuestionDTO> selectedQuestions = questionMapper.randomQuestionsByTopicAndDifficulty(
                request.getTopicId(), request.getDifficultyId(), request.getNumberOfQuestions()

        );


        int requested = request.getNumberOfQuestions();
        int actual = selectedQuestions.size();

        if (actual < requested) {
            System.out.println(" Chỉ tìm được " + actual + " câu hỏi phù hợp (yêu cầu: " + requested + ")");
        }
        System.out.println(">> Selected questions: " + selectedQuestions.size());

        System.out.println(">> Topic ID: " + request.getTopicId());
        System.out.println(">> Difficulty ID: " + request.getDifficultyId());
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


    public void createMixedTopicTest(MixedTopicTestDTO dto) {
        TestDTO testDTO = new TestDTO();
        testDTO.setTestName(dto.getTestName());
        testDTO.setCreatedBy(dto.getCreatedBy());
        testDTO.setTestType("Mixed");
        testMapper.insertTest(testDTO);
        Integer testId = testDTO.getTestId();
        int order = 1;


        for (Map.Entry<Integer, Integer> entry : dto.getTopicDistribution().entrySet()) {
            Integer topicId = entry.getKey();
            Integer count = entry.getValue();

            if (count != null && count > 0) {
                List<Question> questions = questionMapper.findRandomQuestionsByTopic(topicId, count);
                for (Question q : questions) {
                    System.out.println(">> Question ID: " + q.getQuestionId());
                    System.out.println(">> Difficulty: " + q.getDifficultyId());

                    testQuestionMapper.insertTestQuestion(
                            testId,
                            q.getQuestionId(),
                            dto.getCreatedBy(),
                            q.getDifficultyId(),
                            order++
                    );
                }
            }
        }
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
