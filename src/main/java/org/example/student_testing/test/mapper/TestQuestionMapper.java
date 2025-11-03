package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.QuestionDTO;

import java.util.List;

@Mapper
public interface TestQuestionMapper {

    void insertTestQuestion(@Param("testId") Integer testId,
                            @Param("questionId") Integer questionId,
                            @Param("studentUsername") String studentUsername,
                            @Param("difficultyId") Integer difficultyId,
                            @Param("orderNo") Integer orderNo,
                            @Param("source") String source);

    void deleteTestQuestion(Integer questionId);

    int countByQuestionId(@Param("questionId") Integer questionId);

    List<QuestionDTO> findQuestionsByTestId(@Param("testId") Integer testId);

    // Gán câu hỏi vào đề kiểm tra (không gán cho học sinh)
    void insertTestQuestionForTest(@Param("testId") Integer testId,
                                   @Param("questionId") Integer questionId,
                                   @Param("assignedBy") String assignedBy,
                                   @Param("difficultyId") Integer difficultyId,
                                   @Param("orderNo") Integer orderNo,
                                   @Param("source") String source);

    // Gán câu hỏi cho học sinh (nếu cần)
    void insertTestQuestionForStudent(@Param("testId") Integer testId,
                                      @Param("questionId") Integer questionId,
                                      @Param("studentUsername") String studentUsername,
                                      @Param("difficultyId") Integer difficultyId,
                                      @Param("orderNo") Integer orderNo,
                                      @Param("source") String source);

    int countQuestionsInTest(@Param("testId") Integer testId);

}
