package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.student.entity.StudentProfile;


import java.util.List;

@Mapper
public interface StudentProfileMapper {

    List<StudentProfile> selectAllStudentProfile();
    StudentProfile selectStudentProfileById(String id);
    void insertStudentProfile(StudentProfile studentProfile);
    void updateStudentProfile(StudentProfile studentProfile);
    void deleteStudentProfileById(String id);



    StudentProfile findStudentProfileByUsername(@Param("username") String username);

    void insertProfile(StudentProfile profile);
    void updateProfile(StudentProfile profile);

    void updateAvatarUrl(@Param("username")  String username, @Param("avatarUrl") String avatarUrl);

}
