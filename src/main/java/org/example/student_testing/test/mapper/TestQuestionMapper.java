package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestQuestionMapper {

    void insertTestQuestion(@Param("testId") Integer testId,
                            @Param("questionId") Integer questionId,
                            @Param("studentUsername") String studentUsername,
                            @Param("difficultyId") Integer difficultyId,
                            @Param("orderNo") Integer orderNo);

    void deleteTestQuestion(Integer questionId);

    int countByQuestionId(@Param("questionId") Integer questionId);


}
