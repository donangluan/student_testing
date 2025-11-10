package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.test.dto.AnswerResultDTO;
import org.example.student_testing.test.dto.DynamicAnswerDTO;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.mapper.DynamicTestMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicTestService {

    private final DynamicTestMapper mapper;


    public Question getNextQuestion(int difficulty, String studentUsername, int testId, int topicId) {
        List<Integer> answeredIds = mapper.getAnsweredQuestionIds(testId, studentUsername);


        List<Question> current = mapper.getQuestionsByDifficultyAndTopic(difficulty, topicId);
        for (Question q : current) {
            if (!answeredIds.contains(q.getQuestionId())) return q;
        }


        for (int d = difficulty - 1; d >= 1; d--) {
            List<Question> fallback = mapper.getQuestionsByDifficultyAndTopic(d, topicId);
            for (Question q : fallback) {
                if (!answeredIds.contains(q.getQuestionId())) return q;
            }
        }


        for (int d = difficulty + 1; d <= 5; d++) {
            List<Question> fallback = mapper.getQuestionsByDifficultyAndTopic(d, topicId);
            for (Question q : fallback) {
                if (!answeredIds.contains(q.getQuestionId())) return q;
            }
        }

        return null;
    }

    public Question getNextQuestionMixed(int difficulty, String studentUsername, int testId) {
        List<Question> allQuestions = mapper.getQuestionsByDifficulty(difficulty);
        List<Integer> answeredIds = mapper.getAnsweredQuestionIds(testId, studentUsername);
        return allQuestions.stream()
                .filter(q -> !answeredIds.contains(q.getQuestionId()))
                .findFirst()
                .orElse(null);
    }

    public boolean checkAnswer(int questionId, String selectedOption) {
        String correct = mapper.getCorrectOption(questionId);
        return correct != null && correct.equalsIgnoreCase(selectedOption);
    }

    public void saveAnswer(DynamicAnswerDTO dto) {
        mapper.saveAnswer(dto.getTestId(), dto.getStudentUsername(), dto.getQuestionId(), dto.getSelectedOption());
    }

    public int nextDifficulty(int current, boolean correct) {
        return correct ? Math.min(current + 1, 5) : Math.max(current - 1, 1);
    }

    public boolean isFinished(int testId, String studentUsername) {
        int totalRequired = mapper.countAssignedQuestions(testId);
        int answered = mapper.countAnswers(testId, studentUsername);
        System.out.println("Đã làm: " + answered + " / Tổng cần làm: " + totalRequired);
        return answered >= totalRequired;
    }

    public List<AnswerResultDTO> getAnswerResults(int testId, String studentUsername) {
        return mapper.getAnswerResults(testId, studentUsername);
    }



    public int getTotalQuestions(int testId) {
        return mapper.getTotalQuestions(testId);


    }

    public int getAnsweredCount(int testId, String studentUsername) {
        return mapper.countAnswers(testId, studentUsername);
    }

    public int getRequiredQuestionCount(int testId, String testType) {
        if ("Unique".equalsIgnoreCase(testType)) {
            return mapper.countAssignedQuestions(testId);
        } else {
            return mapper.getTotalQuestions(testId);
        }
    }


    public Question getNextQuestionMixedByDifficulty(int difficulty, String studentUsername, int testId) {
        List<Integer> answeredIds = mapper.getAnsweredQuestionIds(testId, studentUsername);
        List<Integer> topicIds = mapper.getTopicIdsInTest(testId);


        for (Integer topicId : topicIds) {
            List<Question> pool = mapper.getQuestionsByDifficultyAndTopic(difficulty, topicId);
            for (Question q : pool) {
                if (!answeredIds.contains(q.getQuestionId())) return q;
            }
        }


        for (int d = difficulty - 1; d >= 1; d--) {
            for (Integer topicId : topicIds) {
                List<Question> pool = mapper.getQuestionsByDifficultyAndTopic(d, topicId);
                for (Question q : pool) {
                    if (!answeredIds.contains(q.getQuestionId())) return q;
                }
            }
        }


        for (int d = difficulty + 1; d <= 5; d++) {
            for (Integer topicId : topicIds) {
                List<Question> pool = mapper.getQuestionsByDifficultyAndTopic(d, topicId);
                for (Question q : pool) {
                    if (!answeredIds.contains(q.getQuestionId())) return q;
                }
            }
        }

        return null;
    }

}
