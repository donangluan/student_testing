package org.example.student_testing.chatbot.formatter;

import org.example.student_testing.chatbot.dto.QuestionAnalysisDTO;

import java.util.List;

public class TestAnalyticsFormatter {


    public static String formatQuestionStats(List<QuestionAnalysisDTO> questions) {
        StringBuilder sb = new StringBuilder();
        for (QuestionAnalysisDTO q : questions) {
            String status;
            if (q.getCorrectRate() >= 60 && q.getCorrectRate() <= 85) {
                status = "✅ Phù hợp";
            } else if (q.getCorrectRate() < 30) {
                status = "⚠️ Quá khó";
            } else if (q.getCorrectRate() > 95 && q.getDifficulty().equalsIgnoreCase("Hard")) {
                status = "⚠️ Quá dễ";
            } else {
                status = "";
            }

            sb.append(String.format("- Câu %d: Tỷ lệ đúng %.1f%% (%s) %s\n",
                    q.getOrderNo(),
                    q.getCorrectRate(),
                    q.getDifficulty(),
                    status));
        }
        return sb.toString();
    }
}
