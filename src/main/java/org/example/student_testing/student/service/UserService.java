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

    public UserDTO findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return userMapper.toDTO(user);
    }

    public void register(UserDTO userDTO) {
        // 1. Kiểm tra username
        if (userMapper.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // 2. Tạo tài khoản
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        userMapper.insertUser(user);

        // 3. Gán quyền vào bảng trung gian
        Integer roleId = roleMapper.findRoleIdByName(userDTO.getRoleName());
        if (roleId == null) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + userDTO.getRoleName());
        }

        // Nếu dùng bảng roleId → dùng dòng này
        userMapper.assignRoles(userDTO.getUsername(), roleId);

        // Nếu dùng bảng roleName → dùng dòng này
        userMapper.insertUserRole(userDTO.getUsername(), "ROLE_" + userDTO.getRoleName());

        // 4. Nếu là học viên → tạo hồ sơ
        if ("STUDENT".equalsIgnoreCase(userDTO.getRoleName())) {
            StudentProfile profile = new StudentProfile();
            profile.setStudentId(userDTO.getUsername()); // hoặc sinh mã riêng
            profile.setUsername(userDTO.getUsername());
            studentProfileMapper.insertStudentProfile(profile);
        }
    }


    public void registerStudent(User user, StudentProfile profile) {
        // 1. Tạo tài khoản
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insertUser(user);

        // 2. Gán quyền vào bảng trung gian
        userMapper.insertUserRole(user.getUsername(), "ROLE_STUDENT");

        // 3. Gắn username vào hồ sơ
        profile.setUsername(user.getUsername());
        studentProfileMapper.insertStudentProfile(profile);
    }




}
