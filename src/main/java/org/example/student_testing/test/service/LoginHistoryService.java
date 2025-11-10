package org.example.student_testing.test.service;

import org.example.student_testing.test.dto.LoginHistoryDTO;
import org.example.student_testing.test.mapper.LoginHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginHistoryService {

    @Autowired
    private LoginHistoryMapper mapper;

    public void save(LoginHistoryDTO dto) {

        mapper.insertLoginHistory(dto);
    }

    public List<LoginHistoryDTO> getAll() {
        return mapper.findAll();
    }

    public List<LoginHistoryDTO> getByUsername(String username) {
        return mapper.findByUsername(username);
    }
}
