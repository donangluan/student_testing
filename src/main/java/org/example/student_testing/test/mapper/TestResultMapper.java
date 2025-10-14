package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TestResultMapper {


    void insertResult(@Param("testId") Integer testId,
                      @Param("studentUserName") String studentUsername,
                      @Param("score") Double score,
                      @Param("percentile") Double percentile,
                      @Param("rankId") int rankId
                      );

    List<Double> findScoresByTestId(@Param("testId") Integer testId);



}
