package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.entity.Student;


import java.util.List;

@Mapper
public interface StudentMapper {

    List<StudentDTO> getStudentDTOList();

    List<Student> getStudentList();

    List<StudentDTO> getStudentsPaged(@Param("offset") int offset, @Param("limit") int limit);
    int countTotalStudents();

    void insertStudent(Student Student);

    void deleteStudent(@Param("studentId") String  studentId);

    void updateStudent(Student Student);

    StudentDTO getStudentDTOListById(@Param("studentId") String studentId);


    List<StudentDTO> searchStudentPaged(@Param("keyword") String keyword,
                                        @Param("status") String status,
                                        @Param("courseName") String courseName,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    int countSearchStudent(@Param("keyword") String keyword ,@Param("status") String status,
                           @Param("courseName") String courseName);


    List<StudentDTO> filterStudentPaged(@Param("keyword") String keyword,
                                        @Param("status") String status,

                                        @Param("courseName") String courseName,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    int countFilterStudent(@Param("keyword") String keyword,
            @Param("status") String status,
                           @Param("courseName") String courseName);


    List<StudentDTO> getStudentsByTeacherUsername(@Param("teacherUsername") String teacherUsername);


    List<StudentDTO> findStudentsByClassId(@Param("classId") Integer classId);

    int countAllStudents();

}
