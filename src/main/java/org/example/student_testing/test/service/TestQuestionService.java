package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public void assignQuestionsToTest(Integer testId, List<Integer> questionIds) {

        // 1. Xóa các câu hỏi cũ (Tùy chọn: Nếu muốn thay thế hoàn toàn)
        // Nếu không muốn xóa, hãy bỏ qua.
        // testQuestionMapper.deleteQuestionsByTestId(testId);

        // 2. Lấy orderNo tiếp theo nếu không xóa
        Integer currentMaxOrder = testQuestionMapper.findMaxOrderNoByTestId(testId);
        int order = (currentMaxOrder != null) ? currentMaxOrder + 1 : 1;

        // 3. Thực hiện Batch Insert
        for (Integer questionId : questionIds) {
            if (questionId == null) continue;

            QuestionDTO question = questionMapper.findById(questionId);
            if (question == null || question.getDifficultyId() == null) continue;

            testQuestionMapper.insertTestQuestionForTest(
                    testId,
                    questionId,
                    question.getDifficultyId(),
                    order++,
                    question.getSource() != null ? question.getSource() : "manual"
            );
        }
    }
}
