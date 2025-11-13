package org.example.student_testing.chatbot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.chatbot.dto.AiGeneratedQuestionDTO;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;

import java.util.List;

@Mapper
public interface AiGeneratedQuestionMapper {

  void insertQuestion(AiGeneratedQuestion question);
  List<AiGeneratedQuestion> getByTeacherId(Integer teacherId);
  void updateStatus(Integer id, String status);

    void insertAiQuestions(@Param("list") List<AiGeneratedQuestion> list);
    List<AiGeneratedQuestion> findByIds(@Param("ids") List<Integer> ids);

    void updateQuestion(AiGeneratedQuestion question);

    AiGeneratedQuestion findById(@Param("id") Integer id);

    List<AiGeneratedQuestion> findByTopicId(@Param("topicId") Integer topicId);


    List<AiGeneratedQuestion> findByCourseAndTopic(@Param("courseName") String courseName,
                                                      @Param("topicName") String topicName);

    List<AiGeneratedQuestion> findByCourse(@Param("courseName") String courseName);
    List<Integer> findAllIds();

    List<AiGeneratedQuestion> findByCourseId(@Param("courseId") Integer courseId);


    AiGeneratedQuestion findByOfficialQuestionId(@Param("questionId") Integer questionId);



    void updateOfficialQuestionId(@Param("aiQuestionId") Integer aiQuestionId,
                                  @Param("officialQuestionId") Integer officialQuestionId);


    List<Integer> findAllOfficialIds();
}
