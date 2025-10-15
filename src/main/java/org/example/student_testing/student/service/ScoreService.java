package org.example.student_testing.student.service;


import org.example.student_testing.student.dto.ScoreDTO;
import org.example.student_testing.student.entity.Score;
import org.example.student_testing.student.mapper.ScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    public List<ScoreDTO> getAllScores(){
        return scoreMapper.findAll().stream().
                map(this:: toDTO).collect(Collectors.toList());
    }

    public ScoreDTO toDTO(Score score){
        ScoreDTO scoreDTO = new ScoreDTO();
        scoreDTO.setScoreId(score.getScoreId());
        scoreDTO.setStudentName(score.getStudentName());
        scoreDTO.setCourseName(score.getCourseName());
        scoreDTO.setSubject(score.getSubject());
        scoreDTO.setScore(score.getScore());
        return scoreDTO;

    }

    public Score toEntity(ScoreDTO scoreDTO){
        Score score = new Score();
        score.setScoreId(scoreDTO.getScoreId());
        score.setStudentId(scoreDTO.getStudentId());
        score.setCourseId(scoreDTO.getCourseId());
        score.setSubject(scoreDTO.getSubject());
        score.setScore(scoreDTO.getScore());
        return score;

    }

    public void createScore(ScoreDTO scoreDTO){
        scoreMapper.createScore(toEntity(scoreDTO));


    }

    public int getGradedCount() {
        return scoreMapper.countGradedSubmissions();
    }


}
