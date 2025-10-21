package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.AnswerResultDTO;
import org.example.student_testing.test.entity.Question;


import java.util.List;

@Mapper
public interface DynamicTestMapper {


        Question getNextQuestion(@Param("difficulty") int difficulty,
                                 @Param("studentUsername") String studentUsername,
                                 @Param("testId") int testId,
                                 @Param("topicId") int topicId);

        String getCorrectOption(@Param("questionId") int questionId);

        void saveAnswer(@Param("testId") int testId,
        @Param("studentUsername") String studentUsername,
        @Param("questionId") int questionId,
        @Param("selectedOption") String selectedOption);

        int countAnswers(@Param("testId") int testId,
        @Param("studentUsername") String studentUsername);

        List<AnswerResultDTO> getAnswerResults(@Param("testId") int testId,
                                               @Param("studentUsername") String studentUsername);

        List<Integer> getAnsweredQuestionIds(@Param("testId") int testId,
        @Param("studentUsername") String studentUsername);

        List<Question> getQuestionsByDifficulty(@Param("difficulty") int difficulty);

        List<Question> getQuestionsByDifficultyAndTopic(@Param("difficulty") int difficulty,
        @Param("topicId") int topicId);


    int getTotalQuestions(@Param("testId") int testId);


    int countAssignedQuestions(@Param("testId") int testId);

    List<Integer> getTopicIdsInTest(@Param("testId") int testId);
}
