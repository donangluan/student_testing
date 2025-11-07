package org.example.student_testing.chatbot.service;

import org.example.student_testing.chatbot.dto.QuestionAnalysisDTO;
import org.example.student_testing.chatbot.formatter.TestAnalyticsFormatter;
import org.example.student_testing.chatbot.mapper.QuestionAnalysisMapper;
import org.example.student_testing.test.mapper.TestMapper;
import org.example.student_testing.test.mapper.TestResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestAnalyticsService {


    @Autowired
    private TestMapper testMapper;

    @Autowired
    private TestResultMapper testResultMapper;

    @Autowired
    private QuestionAnalysisMapper questionAnalysisMapper;

    /**
     * Phân tích bài test theo testId, trả về prompt đã gắn dữ liệu
     */
    public String analyzeTest(int testId) {
        // 1. Lấy tên bài test
        String testName = testMapper.findTestNameById(testId);

        // 2. Lấy danh sách điểm học sinh
        List<Double> scores = testResultMapper.findScoresByTestId(testId);
        int studentCount = scores.size();
        double avgScore = calculateAverage(scores);
        double maxScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double stdDev = calculateStdDev(scores, avgScore);

        // 3. Lấy phân tích câu hỏi
        List<QuestionAnalysisDTO> questions = questionAnalysisMapper.analyzeQuestion(testId);
        String questionAnalysis = TestAnalyticsFormatter.formatQuestionStats(questions);

        // 4. Gắn vào template
        String prompt = ANALYZE_TEST_PROMPT
                .replace("{testName}", testName)
                .replace("{studentCount}", String.valueOf(studentCount))
                .replace("{avgScore}", String.format("%.1f", avgScore))
                .replace("{maxScore}", String.format("%.1f", maxScore))
                .replace("{minScore}", String.format("%.1f", minScore))
                .replace("{stdDev}", String.format("%.1f", stdDev))
                .replace("{questionAnalysis}", questionAnalysis);

        return prompt;
    }

    private double calculateAverage(List<Double> scores) {
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateStdDev(List<Double> scores, double mean) {
        double variance = scores.stream()
                .mapToDouble(score -> Math.pow(score - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    public static final String ANALYZE_TEST_PROMPT = """
        Bạn là chuyên gia phân tích giáo dục, giúp giáo viên hiểu rõ kết quả học tập của học sinh.

        === DỮ LIỆU BÀI TEST ===
        Tên bài test: {testName}
        Số học sinh: {studentCount}
        Điểm trung bình: {avgScore}/100
        Điểm cao nhất: {maxScore}
        Điểm thấp nhất: {minScore}
        Độ lệch chuẩn: {stdDev}

        === PHÂN TÍCH CÂU HỎI ===
        {questionAnalysis}

        BẮT ĐẦU PHÂN TÍCH:
        """;
}
