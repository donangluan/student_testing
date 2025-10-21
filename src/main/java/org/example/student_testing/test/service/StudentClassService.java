package org.example.student_testing.test.service;

import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.test.dto.AssignStudentDTO;
import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.dto.StudentClassDTO;
import org.example.student_testing.test.entity.StudentClass;
import org.example.student_testing.test.mapper.StudentClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentClassService {

    @Autowired
    private StudentClassMapper studentClassMapper;

    public void assignStudentToClass(AssignStudentDTO dto) {
       StudentClass studentClass = new StudentClass();
       studentClass.setClassId(dto.getClassId());
       studentClass.setStudentUsername(dto.getStudentUsername());
       studentClassMapper.insert(studentClass);
    }


    public List<StudentDTO> getStudentsNotINAnyClass() {
       return studentClassMapper.getStudentsNotInAnyClass();
    }
    public List<ClassDTO> getAllClasses() {

        return studentClassMapper.getAllClasses();
    }

    public List<StudentDTO> getStudentsInClass(Long classId) {
        return studentClassMapper.getStudentsInClass(classId);
    }
}
