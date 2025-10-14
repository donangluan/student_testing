package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.ResultDTO;
import org.example.student_testing.test.mapper.ResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ResultService {

    @Autowired
    private ResultMapper resultMapper;

    public void save(ResultDTO dto) {

        if (dto.getScore() != null && dto.getScore() > 10.0) {
            dto.setScore(10.0);
        }
        resultMapper.insertResult(dto);
    }

    public List<ResultDTO> getAllResults() {
        return resultMapper.findAllResults();
    }

    public List<ResultDTO> filter(Integer testId, String studentUsername) {
        return resultMapper.filterResults(testId, studentUsername);
    }

    public double calculatePercentile(int testId, double score) {
        List<Double> allScores = resultMapper.findScoresByTestId(testId);
        if (allScores == null || allScores.isEmpty()) return 0.0;

        long count = allScores.stream().filter(s -> s != null && s <= score).count();
        return (count * 100.0) / allScores.size();
    }

    public String getRank(double score) {
        if (score >= 9) return "Xuất sắc";
        if (score >= 8) return "Giỏi";
        if (score >= 7) return "Khá";
        if (score >= 6) return "Trung bình";
        return "Yếu";
    }

    public void exportCSV(List<ResultDTO> results, Writer writer) {
        try (PrintWriter pw = new PrintWriter(writer)) {
            pw.println("Học sinh,Bài test,Điểm,Percentile,Xếp loại,Ngày làm");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (ResultDTO r : results) {
                String submittedAt = r.getSubmittedAt() != null
                        ? r.getSubmittedAt().format(formatter)
                        : "";
                pw.printf("%s,%s,%.2f,%.2f,%s,%s%n",
                        r.getStudentUsername(),
                        r.getTestName(),
                        r.getScore() != null ? r.getScore() : 0.0,
                        r.getPercentile() != null ? r.getPercentile() : 0.0,
                        r.getRank() != null ? r.getRank() : "",
                        submittedAt);
            }
        }
    }

    public boolean hasSubmitted(Integer testId, String studentUsername) {
        return resultMapper.countResult(testId, studentUsername) > 0;
    }
}
