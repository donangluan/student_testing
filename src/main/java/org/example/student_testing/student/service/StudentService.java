package org.example.student_testing.student.service;

import jakarta.mail.MessagingException;

import org.example.student_testing.student.dto.StudentDTO;
import org.example.student_testing.student.entity.Student;
import org.example.student_testing.student.mapper.CourseMapper;
import org.example.student_testing.student.mapper.ScoreMapper;
import org.example.student_testing.student.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EmailService emailService;


    public List<StudentDTO> getStudentDTOListPaged(int page, int size) {
        int offset = (page - 1) * size;
        return studentMapper.getStudentsPaged(offset, size);
    }

    public int countTotalStudents() {
        return studentMapper.countTotalStudents();
    }

    public StudentDTO toDTO(Student student){
        StudentDTO studentDTO=new StudentDTO();
        studentDTO.setStudentId(student.getStudentId());
        studentDTO.setFullName(student.getFullName());
        studentDTO.setDob(student.getDob());
        studentDTO.setGender(student.getGender());
        studentDTO.setEmail(student.getEmail());
        studentDTO.setCourseId(student.getCourseId());
        studentDTO.setStatus(student.getStatus());
        studentDTO.setUsername(student.getUsername());
        return studentDTO;
    }

    public Student toEntity(StudentDTO studentDTO){
        Student student=new Student();
        student.setStudentId(studentDTO.getStudentId());
        student.setFullName(studentDTO.getFullName());
        student.setDob(studentDTO.getDob());
        student.setGender(studentDTO.getGender());
        student.setEmail(studentDTO.getEmail());
        student.setCourseId(studentDTO.getCourseId());
        student.setStatus(studentDTO.getStatus());
        student.setUsername(studentDTO.getUsername());
        return student;
    }

    public void createStudent(StudentDTO studentDTO) throws MessagingException {
        if (studentDTO.getUsername() == null || studentDTO.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }

        Student student= toEntity(studentDTO);
        studentMapper.insertStudent(student);

        String courseName = courseMapper.findCourseNameById(studentDTO.getCourseId());
        studentDTO.setCourseName(courseName);

        try {
            emailService.sendWelcomeEmail(studentDTO);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public void deleteStudent(String studentId){
        scoreMapper.deleteByStudentId(studentId);
        studentMapper.deleteStudent(studentId);
    }

    public StudentDTO getStudentDTOById(String studentId){
        return studentMapper.getStudentDTOListById(studentId);
    }

    public void updateStudent(StudentDTO studentDTO){
        Student student=toEntity(studentDTO);
        studentMapper.updateStudent(student);
    }

    public List<StudentDTO> searchStudentPaged(String keyword, String status, String courseName, int page, int size) {
        int offset = (page - 1) * size;
        return studentMapper.searchStudentPaged(keyword,status, courseName,offset, size);
    }

    public int countSearchStudent(String keyword ,String status, String courseName) {
        return studentMapper.countSearchStudent(keyword, status, courseName);
    }

    public List<StudentDTO> filterStudentPaged(String keyword, String status, String courseName, int page, int size) {
        int offset = (page - 1) * size;
        return studentMapper.filterStudentPaged(keyword,status, courseName, offset, size);
    }
    //import excel
    public List<StudentDTO> getStudentDTOList() {
        return studentMapper.getStudentDTOList();
    }

    public int countFilterStudent(String keyword, String status, String courseName) {
        return studentMapper.countFilterStudent(keyword, status, courseName);
    }

    public List<StudentDTO> getStudentsForTeacher(String teacherUsername) {
        return studentMapper.getStudentsByTeacherUsername(teacherUsername);
    }

    public List<StudentDTO> getStudentsByClassId(Integer classId) {
        return studentMapper.findStudentsByClassId(classId);
    }
}
