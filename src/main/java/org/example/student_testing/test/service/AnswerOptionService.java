package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.AnswerOptionDTO;
import org.example.student_testing.test.mapper.AnswerOptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerOptionService {


    @Autowired
    private AnswerOptionMapper answerOptionMapper;

    public List<AnswerOptionDTO> findAll() {
        return answerOptionMapper.findAll();
    }
}
