package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.student.dto.UserDTO;
import org.example.student_testing.student.entity.User;


import java.util.List;

@Mapper
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);
    User findByUsername(@Param("username") String username);

    List<String> getRolesByUsername(@Param("username") String username);

    void insertUser(User user);
    void assignRoles(@Param("username") String username, @Param("roleId") int roleId);
    boolean existsByEmail(@Param("email ") String email);

    boolean hasRole(@Param("username") String username, @Param("roleId") int roleId);

    boolean existsByUsername(@Param("username") String username);


    void insertUserRole(@Param("username") String username, @Param("role") String role);

    List<UserDTO> findUsersByRole(@Param("role") String role);

    List<UserDTO> getAllTeachers();


    UserDTO getTeacherByUsername(@Param("username") String username);

    List<UserDTO> getTeachersByUsernames(@Param("list") List<String> usernames);

}
