package org.example.student_testing.test.service;

import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.TeacherClassDTO;
import org.example.student_testing.test.mapper.TeacherClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherClassService {

    @Autowired
    private TeacherClassMapper teacherClassMapper;

    public void assignTeacherToClass(TeacherClassDTO teacherClassDTO) {
        teacherClassMapper.insert(teacherClassDTO);
    }

    public List<Integer> getClassIdsByTeacher( String username) {
       return teacherClassMapper.getClassIdsByTeacher(username);
    }

    public List<TeacherClassDTO> getAllTeacherClass() {
        return teacherClassMapper.getAllTeacherClass();
    }

    public List<String> getTeacherUsernamesByClass(Integer classId) {
        return teacherClassMapper.getTeacherUsernamesByClass(classId);
    }




}
