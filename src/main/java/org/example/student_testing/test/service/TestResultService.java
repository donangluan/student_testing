package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestSubmissionDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultService {

    @Autowired
    private TestResultMapper testResultMapper;

    @Autowired
    private QuestionMapper questionMapper;


    public void submitTest(TestSubmissionDTO dto) {

        List<QuestionDTO> questions = questionMapper.findQuestionsByTestId(dto.getTestId());

        int totalScore = 0;

        for (QuestionDTO q : questions) {
            String selected = dto.getAnswers().get(q.getQuestionId());
            if (selected != null && selected.equalsIgnoreCase(q.getCorrectOption())) {

                totalScore += getScoreByDifficulty(q.getDifficultyId());
            }
        }


        double normalizedScore = normalizeScore(totalScore, questions.size());


        double percentile = calculatePercentile(dto.getTestId(), normalizedScore);


        int rankId = getRankId(normalizedScore);


        testResultMapper.insertResult(dto.getTestId(), dto.getStudentUsername(), normalizedScore, percentile, rankId);
    }


    private int getScoreByDifficulty(Integer difficultyId) {
        return switch (difficultyId) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            default -> 0;
        };
    }


    private double normalizeScore(int rawScore, int totalQuestions) {
        int maxScore = totalQuestions * 3;
        return Math.round((rawScore * 100.0 / maxScore) * 10) / 10.0;
    }


    private double calculatePercentile(Integer testId, double score) {
        List<Double> allScores = testResultMapper.findScoresByTestId(testId);
        long belowOrEqual = allScores.stream().filter(s -> s <= score).count();
        return Math.round((belowOrEqual * 100.0 / allScores.size()) * 10) / 10.0;
    }


    private int getRankId(double score) {
        if (score >= 90) return 4;
        if (score >= 80) return 3;
        if (score >= 70) return 2;
        return 1;
    }
}
