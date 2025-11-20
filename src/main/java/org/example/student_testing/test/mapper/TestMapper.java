package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.*;
import org.example.student_testing.test.entity.Question;


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



    void insertCriteria(TestCriteriaDTO dto);
    List<TestCriteriaDTO> getCriteriaByTestId(Integer testId);


    List<Question> findRandomQuestionsByCriteria(@Param("topicId") Integer topicId,
                                                 @Param("difficultyId") Integer difficultyId,
                                                 @Param("limit") Integer limit);


    void insertTestQuestion_Dynamic(@Param("testId") Integer testId,
                                    @Param("questionId") Integer questionId,
                                    @Param("createdBy") String createdBy,
                                    @Param("studentUsername") String studentUsername,
                                    @Param("difficultyId") Integer difficultyId,
                                    @Param("orderNo") Integer orderNo,
                                    @Param("source") String source);


    int countAssignedQuestionsForStudent(@Param("testId") Integer testId,
                                         @Param("studentUsername") String studentUsername);


    List<QuestionDTO> findQuestionsByTestId(Integer testId, String studentUsernameToView);

    List<Integer> findExpiredTestIds(LocalDateTime currentTime);


    List<String> findStudentsAssignedButNotSubmitted(@Param("testId") Integer testId);

}
