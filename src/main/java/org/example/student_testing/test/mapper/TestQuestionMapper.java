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


    void insertTestQuestionForTest(@Param("testId") Integer testId,
                                   @Param("questionId") Integer questionId,
                                   @Param("assignedBy") String assignedBy,
                                   @Param("difficultyId") Integer difficultyId,
                                   @Param("orderNo") Integer orderNo,
                                   @Param("source") String source);


    void insertTestQuestionForStudent(@Param("testId") Integer testId,
                                      @Param("questionId") Integer questionId,
                                      @Param("studentUsername") String studentUsername,
                                      @Param("difficultyId") Integer difficultyId,
                                      @Param("orderNo") Integer orderNo,
                                      @Param("source") String source);

    int countQuestionsInTest(@Param("testId") Integer testId);

    Integer findMaxOrderNoByTestId(@Param("testId") Integer testId);

    void insertTestQuestionForTest(
            @Param("testId") Integer testId,
            @Param("questionId") Integer questionId,
            @Param("difficultyId") Integer difficultyId,
            @Param("orderNo") Integer orderNo,
            @Param("source") String source
    );

}
