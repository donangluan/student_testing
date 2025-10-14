package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.student.entity.StudentProfile;


import java.util.List;

@Mapper
public interface StudentProfileMapper {

    List<StudentProfile> selectAllStudentProfile();
    StudentProfile selectStudentProfileById(String id);
    void insertStudentProfile(StudentProfile studentProfile);
    void updateStudentProfile(StudentProfile studentProfile);
    void deleteStudentProfileById(String id);



}
