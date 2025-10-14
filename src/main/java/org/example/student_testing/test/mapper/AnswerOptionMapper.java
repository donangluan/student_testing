package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.test.dto.AnswerOptionDTO;


import java.util.List;

@Mapper
public interface AnswerOptionMapper {

    List<AnswerOptionDTO> findAll();
    AnswerOptionDTO findByCode(String code);

}
