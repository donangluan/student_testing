package org.example.student_testing.test.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.student_testing.student.service.CourseService;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.service.AnswerOptionService;
import org.example.student_testing.test.service.DifficultyService;
import org.example.student_testing.test.service.QuestionService;
import org.example.student_testing.test.service.TopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final TopicService topicService;
    private final DifficultyService difficultyService;
    private final AnswerOptionService answerOptionService;



    @GetMapping
    public String listQuestions(Model model) {
        List<QuestionDTO> questions = questionService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "test/question/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("questionDTO", new QuestionDTO());
        model.addAttribute("topics", topicService.findAll());

        model.addAttribute("difficulties", difficultyService.findAll());
        model.addAttribute("options", answerOptionService.findAll());
        return "test/question/add";
    }

    @PostMapping("/add")
    public String addQuestion(@Valid @ModelAttribute("questionDTO") QuestionDTO questionDTO,
                              BindingResult result , Model model
    ) {

        if (result.hasErrors()) {
            System.out.println("❌ Validation lỗi:");
            result.getAllErrors().forEach(error -> System.out.println(" - " + error.getDefaultMessage()));

            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("difficulties", difficultyService.findAll());
            model.addAttribute("options", answerOptionService.findAll());
            return "test/question/add";
        }
        System.out.println("Có lỗi không? " + result.hasErrors());
        result.getAllErrors().forEach(error -> System.out.println(" - " + error.getDefaultMessage()));


        questionDTO.setCreatedBy("gv");
        questionService.createQuestion(questionDTO);
        return "redirect:/questions";
    }

    @GetMapping("/edit/{questionId}")
    public String showEditForm(@PathVariable Integer questionId, Model model) {
        System.out.println("✅ Options: " + answerOptionService.findAll());
        model.addAttribute("questionDTO", questionService.getQuestionById(questionId));
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("difficulties", difficultyService.findAll());
        model.addAttribute("options", answerOptionService.findAll());
        return "test/question/edit";
    }

    @PostMapping("/edit")
    public String updateQuestion(@Valid @ModelAttribute("questionDTO") QuestionDTO questionDTO,

                                 BindingResult result , Model model) {

        if(result.hasErrors()){
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("difficulties", difficultyService.findAll());
            model.addAttribute("options", answerOptionService.findAll());
            return "test/question/add";
        }
        questionDTO.setCreatedBy("gv");
        questionService.update(questionDTO);
        return "redirect:/questions";
    }

    @GetMapping("/delete/{questionId}")
    public String deleteQuestion(@PathVariable("questionId") Integer questionId,
                                 RedirectAttributes redirectAttributes) {
        boolean deleted = questionService.deleteQuestion(questionId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Xóa câu hỏi thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", " Câu hỏi đang được sử dụng trong đề thi, không thể xóa!");
        }
        return "redirect:/questions";
    }
}
