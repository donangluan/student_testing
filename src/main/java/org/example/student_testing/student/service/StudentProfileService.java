package org.example.student_testing.student.service;


import org.example.student_testing.student.dto.StudentProfileDTO;
import org.example.student_testing.student.entity.StudentProfile;
import org.example.student_testing.student.mapper.StudentProfileMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentProfileService {

    @Autowired
    private StudentProfileMapper studentProfileMapper;

    public List<StudentProfile> getAllStudentProfiles() {
        return studentProfileMapper.selectAllStudentProfile();
    }

    public StudentProfile getStudentProfileById(String studentId) {
        return studentProfileMapper.selectStudentProfileById(studentId);
    }

    public void insertStudentProfile(StudentProfileDTO dto) {
        StudentProfile studentProfile = new StudentProfile();
        BeanUtils.copyProperties(dto, studentProfile);
        studentProfile.setStudentId(dto.getStudentId());
        studentProfileMapper.insertStudentProfile(studentProfile);
    }

    public void updateStudentProfile(StudentProfileDTO dto) {
        StudentProfile studentProfile = new StudentProfile();
        BeanUtils.copyProperties(dto, studentProfile);
        studentProfile.setStudentId(dto.getStudentId());
        studentProfileMapper.updateStudentProfile(studentProfile);
    }

    public void deleteStudentProfileById(String studentId) {
        studentProfileMapper.deleteStudentProfileById(studentId);
    }
}
