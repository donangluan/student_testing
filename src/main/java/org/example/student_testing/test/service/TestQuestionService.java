package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;
import org.example.student_testing.chatbot.service.AiGenerateQuestionService;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map; // Cần thiết cho assignQuestionsInBatch
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TestQuestionService {

    private final TestQuestionMapper testQuestionMapper;
    private final QuestionMapper questionMapper;
    private final AiGenerateQuestionService aiGenerateQuestionService;


    private Set<Integer> getOfficialAiQuestionIdSet() {
        return new HashSet<>(aiGenerateQuestionService.findAllOfficialIds());
    }



    public void assignQuestions(Integer testId, List<Integer> questionIds, String studentUsername, String source ) {
        int order = 1;

        for (Integer questionId : questionIds) {
            if (questionId == null) continue;
            QuestionDTO question = questionMapper.findById(questionId);
            if (question == null) continue;
            Integer difficultyId = question.getDifficultyId();
            if (difficultyId == null) continue;
            testQuestionMapper.insertTestQuestion(
                    testId, questionId, studentUsername, difficultyId, order++, source
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

        List<QuestionDTO> questions = questionMapper.findAllByIds(questionIds);


        Set<Integer> officialAiQuestionIds = getOfficialAiQuestionIdSet();

        Integer currentMaxOrder = testQuestionMapper.findMaxOrderNoByTestId(testId);
        int order = (currentMaxOrder != null) ? currentMaxOrder + 1 : 1;

        for (QuestionDTO question : questions) {
            if (question == null) continue;

            String source = officialAiQuestionIds.contains(question.getQuestionId()) ? "ai" : "manual";

            if (question.getDifficultyId() == null) {
                System.err.println("CẢNH BÁO: Question ID " + question.getQuestionId() + " thiếu Difficulty ID. Bỏ qua.");
                continue;
            }

            testQuestionMapper.insertTestQuestionForTest(
                    testId,
                    question.getQuestionId(),
                    question.getDifficultyId(),
                    order++,
                    source
            );
        }
    }


    @Transactional
    public void assignQuestionsInBatch(Integer testId,
                                       List<Integer> questionIds,
                                       String assignedBy,
                                       Map<String, String> questionSources) {


        List<QuestionDTO> questions = questionMapper.findAllByIds(questionIds);


        Set<Integer> officialAiQuestionIds = getOfficialAiQuestionIdSet();

        int order = 1;

        for (QuestionDTO question : questions) {
            if (question == null) continue;

            Integer qId = question.getQuestionId();


            String source = questionSources.get(String.valueOf(qId));


            if (source == null || source.isBlank()) {
                source = officialAiQuestionIds.contains(qId) ? "ai" : "manual";
            }


            Integer difficultyId = question.getDifficultyId();
            if (difficultyId == null) {
                System.err.println("CẢNH BÁO: Question ID " + qId + " thiếu Difficulty ID. Bỏ qua.");
                continue;
            }


            testQuestionMapper.insertTestQuestion(
                    testId,
                    qId,
                    assignedBy,
                    difficultyId,
                    order++,
                    source
            );
        }
    }
}