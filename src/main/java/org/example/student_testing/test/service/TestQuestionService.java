package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;
import org.example.student_testing.chatbot.service.AiGenerateQuestionService;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestDTO;
import org.example.student_testing.test.entity.Test;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestMapper;
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
    private final TestMapper testMapper;





    private Set<Integer> getOfficialAiQuestionIdSet() {
        return new HashSet<>(aiGenerateQuestionService.findAllOfficialIds());
    }


    public List<QuestionDTO> loadQuestionsByTestIdAndStudent(
            Integer testId, String studentUsername) {

        // 1. Lấy thông tin bài test
        TestDTO testInfo = testMapper.findTestById(testId);

        if (testInfo == null) {
            return List.of(); // Trả về danh sách rỗng nếu không tìm thấy test
        }

        String testType = testInfo.getTestType();

        // 2. Logic phân biệt loại đề
        if ("Unique".equalsIgnoreCase(testType)) {
            // Trường hợp 1: Đề Dynamic/Unique
            // Đây là nơi Đề 93 và 111 của bạn được lưu
            System.out.println("DEBUG LOAD: Tải đề Dynamic ID " + testId + " cho học sinh: " + studentUsername);
            return testQuestionMapper.findDynamicQuestionsByTestIdAndStudent(testId, studentUsername);

        } else {
            // Trường hợp 2: Đề Fixed/Mixed (Questions Chung)
            // Đây là nơi log trước đó của bạn bị gọi nhầm khi xem Đề 111
            System.out.println("DEBUG LOAD: Tải đề Chung ID " + testId + " (Fixed/Mixed)");

            // Nếu là đề Mixed, nó có thể cần gộp cả 2 phần, nhưng ta ưu tiên tải phần chung trước
            return testQuestionMapper.findFixedQuestionsByTestId(testId);
        }
    }




    public void assignQuestions(Integer testId, List<Integer> questionIds, String studentUsername, String source ) {
        int order = 1;

        for (Integer questionId : questionIds) {
            if (questionId == null) continue;
            QuestionDTO question = questionMapper.findById(questionId);
            if (question == null) continue;
            Integer difficultyId = question.getDifficultyId();
            if (difficultyId == null) continue;
            testQuestionMapper.insertQuestionForStudent(
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
        testQuestionMapper.insertQuestionForStudent(testId, questionId, studentUsername, difficultyId, orderNo, source);
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

            testQuestionMapper.insertQuestionForFixedTest(
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


            testQuestionMapper.insertQuestionForStudent(
                    testId,
                    qId,
                    assignedBy,
                    difficultyId,
                    order++,
                    source
            );
        }
    }

    public List<QuestionDTO> loadDynamicTestQuestions(Integer testId, String studentUsername) {

        System.out.printf("DEBUG LOAD: Tải đề Dynamic ID %d cho học sinh: %s%n", testId, studentUsername);

        // 🚨 QUAN TRỌNG: Sử dụng hàm Mapper mới
        List<QuestionDTO> questions = testQuestionMapper.findDynamicQuestionsByTestIdAndStudent(testId, studentUsername);

        if (questions.isEmpty()) {
            System.err.printf("CẢNH BÁO LOAD: Không tìm thấy câu hỏi nào cho đề %d của học sinh %s. (Có thể chưa gán hoặc chưa có data).%n",
                    testId, studentUsername);
        }

        return questions;
    }


}