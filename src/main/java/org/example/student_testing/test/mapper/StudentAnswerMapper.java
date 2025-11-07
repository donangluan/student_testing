package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.StudentAnswerDTO;


import java.util.List;

@Mapper
public interface StudentAnswerMapper {

    void insertAnswer(StudentAnswerDTO studentAnswerDTO);
    List<StudentAnswerDTO> findAllAnswers(@Param("testId")  Integer testId,

                                          @Param("studentUsername") String studentUsername );
    StudentAnswerDTO findAnswerById(Integer id);

    List<StudentAnswerDTO> getTestsByTeacher(@Param("teacherUsername") String teacherUsername);

    StudentAnswerDTO findAnswerByQuestionIdAndStudent(@Param("testId") Integer testId,
                                                      @Param("questionId") Integer questionId,
                                                      @Param("studentUsername") String studentUsername);



}
