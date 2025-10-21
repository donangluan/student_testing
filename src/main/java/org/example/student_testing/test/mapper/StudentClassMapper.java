package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.entity.StudentClass;

import java.util.List;

@Mapper
public interface StudentClassMapper {

    void insert(StudentClass studentClass);
    List<StudentDTO> getStudentsNotInAnyClass();

    List<ClassDTO> getAllClasses();

    List<StudentDTO> getStudentsInClass(Long classId);

}
