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




        List<QuestionDTO> selectedQuestions = questionMapper.randomQuestionsByTopic(
                request.getTopicId(), request.getNumberOfQuestions()
        );




        for (String studentUsername : request.getStudentUsername()) {

            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(test.getTestId());
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);



            int order = 1;
            for (QuestionDTO q : selectedQuestions) {
                TestQuestionDTO tq = new TestQuestionDTO();
                tq.setTestId(test.getTestId());
                tq.setQuestionId(q.getQuestionId());
                tq.setStudentUsername(studentUsername);
                tq.setOrderNo(order++);
                tq.setDifficultyId(q.getDifficultyId());
                testMapper.insertTestQuestion(tq);


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



}
