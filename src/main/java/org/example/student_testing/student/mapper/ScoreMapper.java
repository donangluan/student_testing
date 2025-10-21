package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.student.entity.Score;


import java.util.List;

@Mapper
public interface ScoreMapper {

    void deleteByStudentId(@Param("studentId") String studentId);

    List<Score> findAll();
    Score findById(Integer scoreId);
    void createScore(Score score);
    void updateScore(Score score);

    int countGradedSubmissions();





}
