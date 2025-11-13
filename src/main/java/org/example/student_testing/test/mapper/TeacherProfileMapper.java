package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.TeacherProfileDTO;
import org.example.student_testing.test.entity.TeacherProfile;

import java.util.List;

@Mapper
public interface TeacherProfileMapper {

    List<TeacherProfileDTO> getAllTeachers();

    void insertTeacher(TeacherProfile teacher);

    void updateTeacher(TeacherProfile teacher);

    void deleteById(@Param("teacherId") String teacherId);

    TeacherProfileDTO findById(@Param("teacherId") String teacherId);
    String getLastTeacherId();


    List<TeacherProfile> findAllTeachersForDropdown();

}
