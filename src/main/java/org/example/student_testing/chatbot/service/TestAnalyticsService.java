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


    @Autowired
    private GeminiService geminiService;


    public String analyzeTest(int testId) {

        String testName = testMapper.findTestNameById(testId);


        List<Double> scores = testResultMapper.findScoresByTestId(testId);
        int studentCount = scores.size();
        double avgScore = calculateAverage(scores);
        double maxScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double stdDev = calculateStdDev(scores, avgScore);


        List<QuestionAnalysisDTO> questions = questionAnalysisMapper.analyzeQuestion(testId);
        String questionAnalysis = TestAnalyticsFormatter.formatQuestionStats(questions);


        String prompt = ANALYZE_TEST_PROMPT
                .replace("{testName}", testName)
                .replace("{studentCount}", String.valueOf(studentCount))
                .replace("{avgScore}", String.format("%.1f", avgScore))
                .replace("{maxScore}", String.format("%.1f", maxScore))
                .replace("{minScore}", String.format("%.1f", minScore))
                .replace("{stdDev}", String.format("%.1f", stdDev))
                .replace("{questionAnalysis}", questionAnalysis);

        String finalReport = geminiService.chat(prompt, null);

        return finalReport;
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
       Bạn là chuyên gia phân tích giáo dục, giúp giáo viên hiểu rõ kết quả 
        học tập của học sinh.
        
        === DỮ LIỆU BÀI TEST ===
        Tên bài test: {testName}
        Số học sinh: {studentCount}
        Điểm trung bình: {avgScore}/100
        Điểm cao nhất: {maxScore}
        Điểm thấp nhất: {minScore}
        Độ lệch chuẩn: {stdDev}
        
        === PHÂN TÍCH CÂU HỎI ===
        {questionAnalysis}
        
        Ví dụ format:
        - Câu 1: Tỷ lệ đúng 85% (Easy)  Phù hợp
        - Câu 5: Tỷ lệ đúng 22% (Medium)  Quá khó
        - Câu 8: Tỷ lệ đúng 98% (Hard)  Quá dễ
        
        === NHIỆM VỤ ===
        Phân tích theo 4 bước:
        
        1 TỔNG QUAN:
            - Đánh giá chung về kết quả lớp
            - So sánh với tiêu chuẩn (điểm TB nên 60-75)
        
        2 ĐIỂM YẾU:
            - Xác định 3-5 câu hỏi khó nhất
            - Tìm pattern: học sinh yếu về topic nào?
        
        3 PHÂN PHỐI ĐỘ KHÓ:
            - Kiểm tra xem độ khó câu hỏi có phù hợp không
            - Gợi ý điều chỉnh (chuyển Easy→Medium, etc.)
        
       4 ĐỀ XUẤT HÀNH ĐỘNG:
            - 3 đề xuất cụ thể để cải thiện
            - Ưu tiên theo mức độ quan trọng
        
        === OUTPUT FORMAT ===
         PHÂN TÍCH BÀI TEST: {testName}
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        TỔNG QUAN:
        [Đánh giá tổng quan 2-3 câu]
        
         CÂU HỎI KHÓ NHẤT (Tỷ lệ sai >70%):
        1. Câu X: "[Nội dung]" (78% sai)
           → Topic: [topic name]
        2. ...
        
        ⚖ ĐÁNH GIÁ ĐỘ KHÓ:
        - Cần điều chỉnh: [số câu] câu
        - Cụ thể: [liệt kê]
        
         ĐỀ XUẤT CẢI THIỆN:
        1. [Đề xuất 1 - Ưu tiên cao]
        2. [Đề xuất 2]
        3. [Đề xuất 3]
        
        BẮT ĐẦU PHÂN TÍCH:
        """;
}
