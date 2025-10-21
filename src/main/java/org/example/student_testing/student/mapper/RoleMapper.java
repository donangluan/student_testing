package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.example.student_testing.student.entity.Role;


import java.util.List;

@Mapper
public interface RoleMapper {

    Integer findRoleIdByName(@Param("roleName") String roleName );
    List<Role> findAllRoles();
    Role findRoleById(@Param("roleId") Integer roleId);
}
