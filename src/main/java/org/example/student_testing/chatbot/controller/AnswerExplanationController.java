package org.example.student_testing.chatbot.controller;

import org.example.student_testing.chatbot.dto.AnswerExplanationRequestDTO;
import org.example.student_testing.chatbot.dto.ChatMessageDTO;
import org.example.student_testing.chatbot.service.AnswerExplanationService;
import org.example.student_testing.chatbot.service.ChatMessageService;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.StudentAnswerDTO;
import org.example.student_testing.test.service.QuestionService;
import org.example.student_testing.test.service.StudentAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/student/chatbot")
public class AnswerExplanationController {

    @Autowired
    private AnswerExplanationService explanationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private StudentAnswerService studentAnswerService;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/question")
    public String showQuestionPage(@RequestParam Integer conversationId,
                                   @RequestParam Integer testId,
                                   @RequestParam Integer questionId,
                                   @AuthenticationPrincipal UserDetails userDetails,

                                   Model model) {


        String studentUsername = userDetails.getUsername();

        List<ChatMessageDTO> messages = chatMessageService.getMessagesByConversationId(conversationId);

        QuestionDTO question = questionService.getQuestionById(questionId);


        StudentAnswerDTO answer = studentAnswerService.getAnswerByQuestionIdAndStudent(testId, questionId, studentUsername);


        AnswerExplanationRequestDTO dto = new AnswerExplanationRequestDTO();
        dto.setQuestionId(questionId);
        dto.setQuestionContent(question.getContent());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setStudentAnswer(explanationService.findAnswerLabel(question, answer.getSelectedOption()));
        dto.setCorrectAnswer(explanationService.findAnswerLabel(question, question.getCorrectOption()));





        model.addAttribute("conversationId", conversationId);
        model.addAttribute("chatMessages", messages);
        model.addAttribute("question", dto);

        System.out.println("==> Selected content: " + answer.getSelectedOption());
        System.out.println("==> Correct content: " + question.getCorrectOption());
        System.out.println("==> Student answer label: " + explanationService.findAnswerLabel(question, answer.getSelectedOption()));
        System.out.println("==> Correct answer label: " + explanationService.findAnswerLabel(question, question.getCorrectOption()));


        return "test/student/question-result";
    }



    @PostMapping("/explain")
    public String explain(@RequestParam Integer conversationId,
                          @ModelAttribute AnswerExplanationRequestDTO dto,
                          Model model) {

        List<ChatMessageDTO> messages = explanationService.explainAnswer(conversationId, dto);


        model.addAttribute("question", dto);
        model.addAttribute("chatMessages", messages);
        model.addAttribute("conversationId", conversationId);

        return "test/student/question-result";
    }
}
