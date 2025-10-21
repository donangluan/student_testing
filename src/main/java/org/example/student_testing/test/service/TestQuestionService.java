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


    public void assignQuestions(Integer testId, List<Integer> questionIds, String studentUsername) {
        int order = 1;

        for (Integer questionId : questionIds) {

            QuestionDTO question = questionMapper.findById(questionId);
            Integer difficultyId = question.getDifficultyId();

            testQuestionMapper.insertTestQuestion(
                    testId,
                    questionId,
                    studentUsername,
                    difficultyId,
                    order++
            );
        }
    }
}
