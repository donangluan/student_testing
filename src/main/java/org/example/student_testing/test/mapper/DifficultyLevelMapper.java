package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.DifficultyLevelDTO;


import java.util.List;

@Mapper
public interface DifficultyLevelMapper {

    List<DifficultyLevelDTO> findAll();
    DifficultyLevelDTO findById(Integer difficultyId);
    void insert(DifficultyLevelDTO dto);
    void update(DifficultyLevelDTO dto);
    void delete(@Param("difficultyId") Integer difficultyId);

    Integer findIdByName(@Param("name") String name);
}
