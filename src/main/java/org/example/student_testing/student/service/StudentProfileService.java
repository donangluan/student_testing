package org.example.student_testing.student.service;


import org.example.student_testing.student.dto.StudentProfileDTO;
import org.example.student_testing.student.entity.StudentProfile;
import org.example.student_testing.student.mapper.StudentProfileMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;

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


    public StudentProfile findStudentProfileByUsername(String username) {

        return studentProfileMapper.findStudentProfileByUsername(username);
    }



    @Transactional
    public void saveProfile(StudentProfile profile) {

        if (profile.getStudentId() == null || profile.getStudentId().isEmpty()) {
            String newStudentId = generateUniqueStudentId();
            profile.setStudentId(newStudentId);
            studentProfileMapper.insertProfile(profile);
        } else {

            studentProfileMapper.updateProfile(profile);
        }
    }

    private String generateUniqueStudentId() {
        String currentYear = String.valueOf(Year.now().getValue());

        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return "SV" + currentYear + "-" + uuidPart;
    }
}
