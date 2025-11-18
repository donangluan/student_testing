package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.TestSessionDTO;

@Mapper
public interface TestSessionMapper {

    TestSessionDTO findSession(@Param("testId") Integer testId,
                               @Param("studentUsername")  String studentUsername
                               );
    void insertSession(TestSessionDTO testSessionDTO);

    void  updateSession(TestSessionDTO testSessionDTO);

    void deleteSession(@Param("testId") Integer testId,
                       @Param("studentUsername")   String studentUsername );
}
