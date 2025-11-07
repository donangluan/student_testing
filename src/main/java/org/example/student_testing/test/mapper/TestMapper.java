package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.*;


import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TestMapper {

    void insertTest(TestDTO test);

    void insertTestAssignment(TestAssignmentDTO assignment);

    void insertTestQuestion(TestQuestionDTO testQuestion);

    List<TestDTO> findAllTests();


    int countAllTests();

    List<TestDTO> findTestsAssignedToStudent(String studentUsername);

    List<TestResultDTO> findResultsByStudent(String studentUsername);


    TestDTO findTestById(Integer testId);



    void insertHistory(@Param("testId") Integer testId,
                       @Param("teacherId") Integer teacherId,
                       @Param("createdAt") LocalDateTime createdAt);


    List<String> getAssignedStudents(@Param("testId") Integer testId);

    List<QuestionDTO> findQuestionsByTestId(@Param("testId") Integer testId);

    String findTestNameById(@Param("testId") int testId);

    Integer findConversationId(@Param("testId") Integer testId, @Param("studentUsername") String studentUsername);
    void insertConversation(@Param("testId") Integer testId,
                            @Param("studentUsername") String studentUsername,
                            @Param("conversationId") Integer conversationId);

}
