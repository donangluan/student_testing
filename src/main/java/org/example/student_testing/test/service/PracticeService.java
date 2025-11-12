package org.example.student_testing.test.service;

import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TopicScoreDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PracticeService {


    private final QuestionMapper questionMapper;
    private final TestResultService testResultService;


    private final String KEY_TOTAL = "totalQuestions";
    private final String KEY_CORRECT = "correctCount";
    private final String KEY_SCORE = "score";
    private final String KEY_DETAILS = "detailedResults";



    public PracticeService(TestResultService testResultService, QuestionMapper questionMapper) {
        this.testResultService = testResultService;
        this.questionMapper = questionMapper;
    }


    public List<QuestionDTO> generatePersonalizedPractice(String username) {


        List<TopicScoreDTO> weakTopics = testResultService.getWeakTopics(username);

        if (weakTopics.isEmpty()) {
            return List.of();
        }


        List<Integer> weakTopicIds = weakTopics.stream()
                .limit(3)
                .map(TopicScoreDTO::getTopicId)
                .map(Integer::intValue)
                .collect(Collectors.toList());


        final int TOTAL_QUESTIONS = 20;

        return questionMapper.findRandomQuestionsByTopicIds(weakTopicIds, TOTAL_QUESTIONS);
    }


    public Map<String, Object> gradePracticeTest(Map<Integer, String> studentAnswers) {
        int totalQuestions = studentAnswers.size();
        int correctCount = 0;

        List<Map<String, Object>> detailedResults = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : studentAnswers.entrySet()) {
            Integer questionId = entry.getKey();
            String selectedOption = entry.getValue();


            QuestionDTO question = questionMapper.findById(questionId);


            if (question == null) continue;

            String correctOption = question.getCorrectOption();

            boolean isCorrect = correctOption != null && correctOption.equalsIgnoreCase(selectedOption);
            if (isCorrect) {
                correctCount++;
            }


            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", questionId);
            detail.put("content", question.getContent());
            detail.put("optionA", question.getOptionA());
            detail.put("optionB", question.getOptionB());
            detail.put("optionC", question.getOptionC());
            detail.put("optionD", question.getOptionD());

            detail.put("selectedOption", selectedOption);
            detail.put("correctOption", correctOption);
            detail.put("isCorrect", isCorrect);
            detailedResults.add(detail);
        }


        double score = totalQuestions > 0 ? ((double) correctCount / totalQuestions) * 10 : 0;

        Map<String, Object> results = new HashMap<>();
        results.put(KEY_TOTAL, totalQuestions);
        results.put(KEY_CORRECT, correctCount);
        results.put(KEY_SCORE, Math.round(score * 10.0) / 10.0);
        results.put(KEY_DETAILS, detailedResults);

        return results;
    }


}
