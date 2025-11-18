package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.QuestionDTO;

import java.util.List;

@Mapper
public interface TestQuestionMapper {

    void insertQuestionForFixedTest(
            @Param("testId") Integer testId,
            @Param("questionId") Integer questionId,
            @Param("difficultyId") Integer difficultyId,
            @Param("topicId") Integer topicId,
            @Param("orderNo") Integer orderNo,
            @Param("source") String source
    );


    void insertQuestionForStudent(
            @Param("testId") Integer testId,
            @Param("questionId") Integer questionId,
            @Param("studentUsername") String studentUsername,
            @Param("difficultyId") Integer difficultyId,
            @Param("topicId") Integer topicId,
            @Param("orderNo") Integer orderNo,
            @Param("source") String source
    );


    List<QuestionDTO> findFixedQuestionsByTestId(
            @Param("testId") Integer testId
    );


    List<QuestionDTO> findDynamicQuestionsByTestIdAndStudent(
            @Param("testId") Integer testId,
            @Param("studentUsername") String studentUsername
    );


    void deleteTestQuestion(Integer questionId);

    int countByQuestionId(@Param("questionId") Integer questionId);


    List<QuestionDTO> findQuestionsByTestId(@Param("testId") Integer testId);

    int countQuestionsInTest(@Param("testId") Integer testId);

    Integer findMaxOrderNoByTestId(@Param("testId") Integer testId);

    int countAssignedQuestionsForStudent(@Param("testId") Integer testId, @Param("studentUsername") String studentUsername);

}
