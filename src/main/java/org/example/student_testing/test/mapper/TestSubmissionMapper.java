package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.student_testing.test.dto.TestSubmissionDTO;

import java.util.List;

@Mapper
public interface TestSubmissionMapper {

    List<TestSubmissionDTO> getAllSubmissionsForTeacher(
                                               @Param("teacherUsername") String teacherUsername);

    int countGradedSubmissions();

    void insert(TestSubmissionDTO submission);

    @Select("SELECT * FROM test_submissions WHERE submission_id = #{submissionId}")
    TestSubmissionDTO findById(Integer submissionId);
}
