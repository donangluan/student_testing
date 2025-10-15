package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.AnswerOptionDTO;
import org.example.student_testing.test.entity.AnswerOption;
import org.example.student_testing.test.mapper.AnswerOptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerOptionService {


    @Autowired
    private AnswerOptionMapper answerOptionMapper;

    public List<AnswerOption> findAll() {
        return List.of(
                new AnswerOption("A", "Đáp án A"),
                new AnswerOption("B", "Đáp án B"),
                new AnswerOption("C", "Đáp án C"),
                new AnswerOption("D", "Đáp án D")
        );
    }
}
