package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.DifficultyLevelDTO;
import org.example.student_testing.test.mapper.DifficultyLevelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DifficultyService {

    @Autowired
    private DifficultyLevelMapper difficultyLevelMapper;

    public List<DifficultyLevelDTO> findAll() {
        return difficultyLevelMapper.findAll();
    }

    public DifficultyLevelDTO findById(Integer difficultyId) {
        return difficultyLevelMapper.findById(difficultyId);
    }

    public void insert(DifficultyLevelDTO dto) {
        difficultyLevelMapper.insert(dto);
    }

    public void update(DifficultyLevelDTO dto) {
        difficultyLevelMapper.update(dto);
    }

    public void delete(Integer difficultyId) {
        difficultyLevelMapper.delete(difficultyId);
    }
}
