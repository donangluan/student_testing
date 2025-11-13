package org.example.student_testing.test.service;

import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.entity.User;
import org.example.student_testing.student.mapper.RoleMapper;
import org.example.student_testing.student.mapper.UserMapper;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.TeacherProfileDTO;
import org.example.student_testing.test.entity.TeacherProfile;
import org.example.student_testing.test.mapper.TeacherProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherProfileService {

    @Autowired
    private TeacherProfileMapper teacherProfileMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<TeacherProfileDTO> getTeacherProfiles(){
        return teacherProfileMapper.getAllTeachers();
    }

    public void createTeacherWithAccount( String fullName, String email, String phone,
                                         String department, String username, String password) {

        String teacherId = generateTeacherId();

        User dto = new User();
        dto.setUsername(username);
        dto.setPassword(passwordEncoder.encode(password));
        dto.setFullName(fullName);
        dto.setEmail(email);
        dto.setRoleCode("teacher");
        userMapper.insertUser(dto);


        Integer roleId = roleMapper.findRoleIdByName("TEACHER");
        if (roleId == null) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: TEACHER");
        }
        userMapper.assignRoles(username, roleId);


        TeacherProfile teacher = new TeacherProfile();
        teacher.setTeacherId(teacherId);
        teacher.setFullName(fullName);
        teacher.setPhone(phone);
        teacher.setDepartment(department);
        teacher.setUsername(username);
        teacher.setEmail(email);
        teacher.setCreatedAt(LocalDateTime.now());
        teacherProfileMapper.insertTeacher(teacher);
    }

    public void updateTeacher(String teacherId, String fullName, String email, String phone, String department) {
        TeacherProfile teacher = new TeacherProfile();
        teacher.setTeacherId(teacherId);
        teacher.setFullName(fullName);
        teacher.setEmail(email);
        teacher.setPhone(phone);
        teacher.setDepartment(department);
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherProfileMapper.updateTeacher(teacher);
    }
    public void deleteTeacher(String teacherId) {
        TeacherProfileDTO teacher = teacherProfileMapper.findById(teacherId);
        if (teacher != null) {
            teacherProfileMapper.deleteById(teacherId);
            userMapper.deleteByUsername(teacher.getUsername());
            userMapper.deleteRolesByUsername(teacher.getUsername());
        }
    }

    public TeacherProfileDTO getTeacherById(String teacherId) {
        return teacherProfileMapper.findById(teacherId);
    }


    public String generateTeacherId() {
        String lastId = teacherProfileMapper.getLastTeacherId();
        int nextNumber = 1;

        if (lastId != null && lastId.startsWith("GV")) {
            try {
                nextNumber = Integer.parseInt(lastId.substring(2)) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return String.format("GV%03d", nextNumber);
    }



    public List<TeacherProfile> getAllTeachersForDropdown() {
        return teacherProfileMapper.findAllTeachersForDropdown();
    }



}
