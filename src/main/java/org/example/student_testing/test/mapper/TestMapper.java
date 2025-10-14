package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.test.dto.TestAssignmentDTO;
import org.example.student_testing.test.dto.TestDTO;
import org.example.student_testing.test.dto.TestQuestionDTO;


import java.util.List;

@Mapper
public interface TestMapper {

    void insertTest(TestDTO test);

    void insertTestAssignment(TestAssignmentDTO assignment);

    void insertTestQuestion(TestQuestionDTO testQuestion);

    List<TestDTO> findAllTests();


}
