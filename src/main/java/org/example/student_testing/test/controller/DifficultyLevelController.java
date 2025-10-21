package org.example.student_testing.test.controller;


import org.example.student_testing.test.dto.DifficultyLevelDTO;
import org.example.student_testing.test.service.DifficultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/difficulties")
public class DifficultyLevelController {
    @Autowired
    private DifficultyService difficultyService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("difficulties", difficultyService.findAll());
        return "test/difficulty/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("DifficultyLevelDTO", new DifficultyLevelDTO());
        return "test/difficulty/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute DifficultyLevelDTO dto) {
        difficultyService.insert(dto);
        return "redirect:/difficulties";
    }

    @GetMapping("/edit/{difficultyId}")
    public String showEditForm(@PathVariable Integer difficultyId, Model model) {
        model.addAttribute("DifficultyLevelDTO", difficultyService.findById(difficultyId));
        return "test/difficulty/edit";
    }

    @PostMapping("/edit")
    public String update(@ModelAttribute DifficultyLevelDTO dto) {
        difficultyService.update(dto);
        return "redirect:/difficulties";
    }

    @GetMapping("/delete/{difficultyId}")
    public String delete(@PathVariable Integer difficultyId) {
        difficultyService.delete(difficultyId);
        return "redirect:/difficulties";
    }
}
