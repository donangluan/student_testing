package org.example.student_testing.chatbot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.chatbot.dto.QuestionAnalysisDTO;

import java.util.List;

@Mapper
public interface QuestionAnalysisMapper {

    List<QuestionAnalysisDTO> analyzeQuestion(@Param("testId") int testId);
}
