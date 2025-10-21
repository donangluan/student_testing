package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.TeacherClassDTO;

import java.util.List;

@Mapper
public interface TeacherClassMapper {

    void insert(TeacherClassDTO teacherClassDTO);
    List<Integer> getClassIdsByTeacher(@Param("username") String username);
    List<TeacherClassDTO> getAllTeacherClass();
    List<String> getTeacherUsernamesByClass(@Param("classId") Integer classId);
}
