package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.chatbot.dto.AnswerExplanationRequestDTO;
import org.example.student_testing.student.mapper.StudentProfileMapper;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.QuestionViewDTO;
import org.example.student_testing.test.dto.TestResultDTO;

import org.example.student_testing.test.dto.TopicScoreDTO;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestResultMapper;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.Writer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultMapper testResultMapper;
    private final QuestionMapper questionMapper;
    private final StudentProfileMapper studentProfileMapper;


    private static final double WEAK_TOPIC_THRESHOLD = 70.0;

    public void save(TestResultDTO dto) {
        testResultMapper.insertResult(dto.getTestId(), dto.getStudentUsername(),
                dto.getScore(), dto.getPercentile(), dto.getRankCode());
    }

    public boolean hasSubmitted(Integer testId, String studentUsername) {
        return !testResultMapper.findScoresByTestIdAndStudent(testId, studentUsername).isEmpty();
    }

    public double calculatePercentile(Integer testId, double score) {
        List<Double> allScores = testResultMapper.findScoresByTestId(testId);
        if (allScores.isEmpty()) return 100.0;
        long belowOrEqual = allScores.stream().filter(s -> s <= score).count();
        return Math.round((belowOrEqual * 100.0 / allScores.size()) * 10) / 10.0;
    }

    public String getRankCode(Double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "D";
    }

    public List<TestResultDTO> filter(Integer testId, String studentUsername, Double minScore, Double maxScore, String rankCode) {
        return testResultMapper.filter(testId, studentUsername, minScore, maxScore, rankCode);
    }

    public List<TestResultDTO> findAll() {
        return testResultMapper.findAll();
    }

    public void exportCSV(List<TestResultDTO> results, Writer writer) {
        try (PrintWriter pw = new PrintWriter(writer)) {
            pw.println("Test ID,Student Username,Score,Percentile,Rank Code,Completed At");
            for (TestResultDTO r : results) {
                pw.printf("%d,%s,%.2f,%.2f,%s,%s%n",
                        r.getTestId(), r.getStudentUsername(), r.getScore(),
                        r.getPercentile(), r.getRankCode(), r.getCompletedAt());
            }
        }
    }


    public double calculateScore(Integer testId, String studentUsername) {
        List<Integer> questionIds = questionMapper.findQuestionIdsByTestId(testId);
        int totalQuestions = questionIds.size();
        int correctAnswers = 0;

        for (Integer questionId : questionIds) {
            String correctOption = questionMapper.findCorrectOptionByQuestionId(questionId);
            String selectedOption = questionMapper.findSelectedOption(testId, studentUsername, questionId);
            if (correctOption != null && correctOption.equals(selectedOption)) {
                correctAnswers++;
            }
        }

        double score = ((double) correctAnswers / totalQuestions) * 10;
        return Math.round(score * 10.0) / 10.0;
    }


    public void saveCalculatedResult(Integer testId, String studentUsername) {
        double score = calculateScore(testId, studentUsername);
        double percentile = calculatePercentile(testId, score);
        String rankCode = getRankCode(score);

        testResultMapper.insertResult(testId, studentUsername, score, percentile, rankCode);
    }




    public Integer getResultId(Integer testId, String username) {
        return testResultMapper.findResultId(testId, username);
    }

    public TestResultDTO getResultById(Integer resultId) {
        return testResultMapper.findById(resultId);
    }


    public AnswerExplanationRequestDTO buildExplanationDTO(Integer testId, Integer questionId, String username) {
        QuestionDTO question = questionMapper.findById(questionId);
        String selectedContent = questionMapper.findSelectedOption(testId, username, questionId);
        String correctContent = questionMapper.findCorrectOptionByQuestionId(questionId);

        AnswerExplanationRequestDTO dto = new AnswerExplanationRequestDTO();
        dto.setQuestionId(questionId);
        dto.setQuestionContent(question.getContent());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setStudentAnswer(findAnswerLabel(question, selectedContent));
        dto.setCorrectAnswer(findAnswerLabel(question, correctContent));
        return dto;
    }



    private String findAnswerLabel(QuestionDTO q, String selectedContent) {
        if (selectedContent == null) return "";
        if (selectedContent.equalsIgnoreCase(q.getOptionA())) return "A";
        if (selectedContent.equalsIgnoreCase(q.getOptionB())) return "B";
        if (selectedContent.equalsIgnoreCase(q.getOptionC())) return "C";
        if (selectedContent.equalsIgnoreCase(q.getOptionD())) return "D";
        return selectedContent;
    }

    public Integer countCompletedTests(String studentUsername) {
         Integer count = testResultMapper.countCompletedTestsByUsername(studentUsername);
         return count != null ? count : 0;
    }


    public double getAverageScore(String studentUsername){
        Double average = testResultMapper.calculateAverageScoreByUsername(studentUsername);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0;
    }

    public List<TopicScoreDTO> getWeakTopics(String studentUsername) {
        List<TopicScoreDTO> topicScores = testResultMapper.findAverageScoreByTopic(studentUsername);

        if (topicScores == null || topicScores.isEmpty()) {
            return Collections.emptyList();
        }


        List<TopicScoreDTO> weakTopics = topicScores.stream()
                .filter(ts -> ts.getAverageScore() < WEAK_TOPIC_THRESHOLD)
                .sorted((ts1, ts2) -> Double.compare(ts1.getAverageScore(), ts2.getAverageScore()))
                .collect(Collectors.toList());

        return weakTopics;
    }


    public List<TestResultDTO> findHistoryByUsername(String studentUsername) {

        return testResultMapper.findResultsByUsername(studentUsername);
    }



    public Map<String, Object> getDetailedResult(Integer testId, String studentUsername) {


        List<QuestionViewDTO> questionsWithAnswers = questionMapper.findQuestionsWithStudentAnswer(
                testId, studentUsername
        );


        List<QuestionViewDTO> questionsForView = questionsWithAnswers.stream()
                .filter(java.util.Objects::nonNull)
                .peek(question -> {

                    Map<String, String> optionMap = new HashMap<>();


                    if (question.getOptionA() != null) optionMap.put("A", question.getOptionA());
                    if (question.getOptionB() != null) optionMap.put("B", question.getOptionB());
                    if (question.getOptionC() != null) optionMap.put("C", question.getOptionC());
                    if (question.getOptionD() != null) optionMap.put("D", question.getOptionD());

                    question.setOptions(optionMap);
                })
                .collect(Collectors.toList());


        Map<String, Object> details = new HashMap<>();
        details.put("questions", questionsWithAnswers);

        return details;
    }

}
