package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.StudentAnswerDTO;
import org.example.student_testing.test.mapper.StudentAnswerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class StudentAnswerService {

    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    public List<StudentAnswerDTO> getStudentAnswers(Integer testId, String studentUsername) {
        return studentAnswerMapper.findAllAnswers(testId, studentUsername);
    }

    public void saveAnswers(Integer testId, String studentUsername, Map<Integer, String> answers) {
        for (Map.Entry<Integer, String> entry : answers.entrySet()) {
            StudentAnswerDTO dto = new StudentAnswerDTO();
            dto.setTestId(testId);
            dto.setStudentUsername(studentUsername);
            dto.setQuestionId(entry.getKey());
            dto.setSelectedOption(entry.getValue());
            dto.setAnsweredAt(LocalDateTime.now());
            studentAnswerMapper.insertAnswer(dto);
        }
    }

    public List<StudentAnswerDTO> getTestsByTeacher(String teacherUsername) {
        return studentAnswerMapper.getTestsByTeacher(teacherUsername);
    }

    public StudentAnswerDTO getAnswerByQuestionIdAndStudent(Integer testId, Integer questionId, String studentUsername) {
      return   studentAnswerMapper.findAnswerByQuestionIdAndStudent(testId, questionId, studentUsername);

    }

}
