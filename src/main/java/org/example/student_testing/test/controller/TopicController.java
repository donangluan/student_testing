package org.example.student_testing.test.controller;

import lombok.RequiredArgsConstructor;

import org.example.student_testing.test.dto.TopicDTO;
import org.example.student_testing.test.service.TopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public String listTopics(Model model) {
        model.addAttribute("topics", topicService.findAll());
        return "test/topic/topic_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("topic", new TopicDTO());
        return "test/topic/topic_add";
    }

    @PostMapping("/add")
    public String addTopic(@ModelAttribute TopicDTO topicDTO) {
        topicService.insert(topicDTO);
        return "redirect:/topics";
    }

    @GetMapping("/edit/{topicId}")
    public String showEditForm(@PathVariable Integer topicId, Model model) {
        model.addAttribute("topic", topicService.findById(topicId));
        return "test/topic/topic_edit";
    }

    @PostMapping("/edit")
    public String updateTopic(@ModelAttribute TopicDTO topicDTO) {
        topicService.update(topicDTO);
        return "redirect:/topics";
    }

    @GetMapping("/delete/{topicId}")
    public String deleteTopic(@PathVariable Integer topicId) {
        topicService.delete(topicId);
        return "redirect:/topics";
    }
}
