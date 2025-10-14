package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.student_testing.test.dto.ResultDTO;


import java.util.List;

@Mapper
public interface ResultMapper {

    List<ResultDTO> findAllResults();
    List<ResultDTO> filterResults(@Param("testId") Integer testId, @Param("studentUsername") String studentUsername);

    void insertResult(ResultDTO dto);

    @Select("SELECT score FROM results WHERE test_id = #{testId}")
    List<Double> findScoresByTestId(@Param("testId") int testId);

    int countResult(@Param("testId") Integer testId,
                    @Param("studentUsername") String studentUsername);

}
