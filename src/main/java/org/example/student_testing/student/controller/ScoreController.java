package org.example.student_testing.student.controller;


import org.example.student_testing.student.dto.ScoreDTO;
import org.example.student_testing.student.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/***
 * Scorce
 *
 */
@Controller
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;


    @GetMapping("/list")
    public String scoreList(Model model){

        List<ScoreDTO> dto = scoreService.getAllScores();
        model.addAttribute("score",dto);
        return "student/score-list";
    }


    @GetMapping("/add")
    public String scoreShowAdd(Model model){
        model.addAttribute("score",new ScoreDTO());
        return "student/score-add";
    }


    @PostMapping("/add")
    public String scoreAdd(@ModelAttribute ScoreDTO scoreDTO){
        scoreService.createScore(scoreDTO);
        return "redirect:/score/list";
    }
}
