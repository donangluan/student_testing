package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.SubmissionViewDTO;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.dto.TopicScoreDTO;

import java.util.List;

@Mapper
public interface TestResultMapper {
    void insertResult(Integer testId, String studentUsername, Double score, Double percentile, String rankCode);
    List<Double> findScoresByTestId(Integer testId);
    List<Double> findScoresByTestIdAndStudent(Integer testId, String studentUsername);
    List<TestResultDTO> findAll();
    List<TestResultDTO> filter(Integer testId, String studentUsername, Double minScore, Double maxScore, String rankCode);

    boolean hasSubmitted(@Param("testId") Integer testId, @Param("username") String username);
    Integer findResultId(@Param("testId") Integer testId, @Param("username") String username);

    TestResultDTO findById(@Param("resultId") Integer resultId);

    List<SubmissionViewDTO> findSubmissionsByTestId(@Param("testId") Integer testId);

    String findStudentAnswer(@Param("questionId") Integer questionId, @Param("username") String username);
    String findCorrectAnswer(@Param("questionId") Integer questionId);

    int countCompletedTestsByUsername(@Param("username") String username);
    Double calculateAverageScoreByUsername( @Param("username") String username);

    List<TopicScoreDTO> findAverageScoreByTopic(@Param("username") String username);


    List<TestResultDTO> findResultsByUsername(@Param("studentUsername") String studentUsername);

    Integer countCompletedAttempts(@Param("testId")  Integer testId, @Param("username") String username);

}
