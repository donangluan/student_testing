package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestQuestionService {


    private final TestQuestionMapper testQuestionMapper;
    private final QuestionMapper questionMapper;


    public void assignQuestions(Integer testId, List<Integer> questionIds, String studentUsername, String source ) {
        int order = 1;

        for (Integer questionId : questionIds) {
            if (questionId == null) {

                continue;
            }

            QuestionDTO question = questionMapper.findById(questionId);
            if (question == null) {

                continue;
            }

            Integer difficultyId = question.getDifficultyId();
            if (difficultyId == null) {

                continue;
            }


            testQuestionMapper.insertTestQuestion(
                    testId,
                    questionId,
                    studentUsername,
                    difficultyId,
                    order++,
                    source
            );
        }
    }


    public void assignSingleQuestion(Integer testId,
                                     Integer questionId,
                                     String studentUsername,
                                     Integer difficultyId,
                                     Integer orderNo,
                                     String source) {
        testQuestionMapper.insertTestQuestion(testId, questionId, studentUsername, difficultyId, orderNo, source);
    }
}
