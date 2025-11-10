package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.test.dto.LoginHistoryDTO;
import org.example.student_testing.test.entity.LoginHistory;

import java.util.List;

@Mapper
public interface LoginHistoryMapper {

    void insertLoginHistory(LoginHistoryDTO dto);
    List<LoginHistoryDTO> findAll();
    List<LoginHistoryDTO> findByUsername(String username);
}
