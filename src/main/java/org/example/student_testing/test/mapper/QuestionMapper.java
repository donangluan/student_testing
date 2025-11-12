package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.QuestionViewDTO;
import org.example.student_testing.test.entity.Question;


import java.util.List;

@Mapper
public interface QuestionMapper {

    List<QuestionDTO> findAll();
    QuestionDTO findById(@Param("questionId") Integer questionId);
    void insert(QuestionDTO QuestionDTO);
    void update(Question Question);
    void delete(@Param("questionId") Integer questionId);






    List<QuestionDTO> findQuestionsByTestId(@Param("testId")Integer testId);
    String getCorrectOption(Integer questionId);

    List<QuestionDTO> findRandomQuestionsByTopic(@Param("topicId") Integer topicId, @Param("count") Integer count);



    Question findNextQuestion(@Param("difficultyId") Integer difficultyId,
                              @Param("studentUsername") String studentUsername,
                              @Param("testId") Integer testId);

    List<QuestionDTO> randomQuestionsByTopic(
            @Param("topicId") Integer topicId,

            @Param("limit") Integer limit
    );

    List<QuestionDTO> getQuestionsByTestIdAndStudent(
            @Param("testId") Integer testId,
            @Param("studentUsername") String studentUsername
    );



    List<Integer> findQuestionIdsByTestId(Integer testId);

    String findCorrectOptionByQuestionId(Integer questionId);

    String findSelectedOption(Integer testId, String studentUsername, Integer questionId);



    List<QuestionViewDTO> findQuestionsWithAnswer(@Param("testId") Integer testId,
                                                  @Param("studentUsername") String studentUsername);


    Integer getDifficultyByQuestionId(Integer questionId);

    List<QuestionDTO> findByCourseAndTopic(@Param("courseName") String courseName,
                                           @Param("topicName") String topicName);

    Integer findIdByContent(@Param("content") String content);


    List<QuestionDTO> findRandomQuestionsByTopicIds(
            @Param("topicIds") List<Integer> topicIds,
            @Param("limit") Integer limit
    );

    List<QuestionViewDTO> findQuestionsWithStudentAnswer(
            @Param("testId") Integer testId,
            @Param("studentUsername") String studentUsername
    );


}



