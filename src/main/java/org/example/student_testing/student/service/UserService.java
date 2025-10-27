package org.example.student_testing.student.service;


import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.entity.StudentProfile;
import org.example.student_testing.student.entity.User;
import org.example.student_testing.student.mapper.RoleMapper;
import org.example.student_testing.student.mapper.StudentProfileMapper;
import org.example.student_testing.student.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private StudentProfileMapper studentProfileMapper;



    private UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRoleCode(user.getRoleCode());
        return dto;
    }

    public UserDTO findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return toDTO(user);
    }

    public void register(UserDTO userDTO) {




        //  Nếu roleCode bị null hoặc sai → gán lại cho chắc chắn
        if (userDTO.getRoleCode() == null || !"STUDENT".equalsIgnoreCase(userDTO.getRoleCode())) {
            System.out.println("Cảnh báo: Vai trò không hợp lệ hoặc bị mất khi đăng ký. Đang gán lại là STUDENT.");
            userDTO.setRoleCode("STUDENT");
        }

        //  Tạo tài khoản người dùng
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setRoleCode("STUDENT"); // Gán cứng vai trò
        userMapper.insertUser(user);

        //  Gán quyền ROLE_STUDENT
        Integer roleId = roleMapper.findRoleIdByName("STUDENT");
        if (roleId == null) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: STUDENT");
        }
        userMapper.assignRoles(userDTO.getUsername(), roleId);
        userMapper.insertUserRole(userDTO.getUsername(), "ROLE_STUDENT");

        //  Tạo hồ sơ học viên
        StudentProfile profile = new StudentProfile();
        profile.setStudentId(userDTO.getUsername());
        profile.setUsername(userDTO.getUsername());
        studentProfileMapper.insertStudentProfile(profile);
    }





    public List<UserDTO> findAllStudents() {
        return userMapper.findUsersByRole("STUDENT");
    }

    public List<UserDTO> getAllTeachers() {
        return userMapper.getAllTeachers();
    }

    public UserDTO getTeacherByUsername(String username) {
        return userMapper.getTeacherByUsername(username);
    }

    public List<UserDTO> getTeachersByUsernames(List<String> usernames) {
        return userMapper.getTeachersByUsernames(usernames);
    }





    public List<UserDTO> getAllUsers() {
        return userMapper.findAllUsers(); // hoặc lấy tất cả nếu có thêm hàm
    }


    public void createUser(User user) {
        if (!userMapper.existsByUsername(user.getUsername())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userMapper.insertUser(user);

            String roleName = "ROLE_" + user.getRoleCode().toUpperCase(); // ví dụ: ROLE_ADMIN
            userMapper.insertUserRole(user.getUsername(), roleName);
        }
    }

    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    public void deleteUser(String username) {
        userMapper.deleteRolesByUsername(username);
        userMapper.deleteByUsername(username);
    }

    public List<UserDTO> findUsersByRole(String roleCode) {
        return userMapper.findUsersByRole(roleCode);
    }

    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

}
