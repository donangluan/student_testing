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

/**
 * Controller xử lý hiển thị giao diện và xử lý yêu cầu giải thích đáp án bằng chatbot.
 */
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

    /**
     * Hiển thị giao diện câu hỏi + hội thoại chatbot (GET)
     */
    @GetMapping("/question")
    public String showQuestionPage(@RequestParam Integer conversationId,
                                   @RequestParam Integer testId,
                                   @RequestParam Integer questionId,
                                   @AuthenticationPrincipal UserDetails userDetails,

                                   Model model) {

        //  Lấy username từ tài khoản đang đăng nhập
        String studentUsername = userDetails.getUsername();
        // Lấy danh sách tin nhắn theo hội thoại
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByConversationId(conversationId);

        // Lấy nội dung câu hỏi từ service
        QuestionDTO question = questionService.getQuestionById(questionId);

        // Lấy đáp án học sinh đã chọn
        StudentAnswerDTO answer = studentAnswerService.getAnswerByQuestionIdAndStudent(testId, questionId, studentUsername);

        // Gộp dữ liệu vào DTO để hiển thị + gửi form
        AnswerExplanationRequestDTO dto = new AnswerExplanationRequestDTO();
        dto.setQuestionId(questionId);
        dto.setQuestionContent(question.getContent());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setStudentAnswer(explanationService.findAnswerLabel(question, answer.getSelectedOption()));
        dto.setCorrectAnswer(explanationService.findAnswerLabel(question, question.getCorrectOption()));




        // Truyền sang view
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("chatMessages", messages);
        model.addAttribute("question", dto);

        System.out.println("==> Selected content: " + answer.getSelectedOption());
        System.out.println("==> Correct content: " + question.getCorrectOption());
        System.out.println("==> Student answer label: " + explanationService.findAnswerLabel(question, answer.getSelectedOption()));
        System.out.println("==> Correct answer label: " + explanationService.findAnswerLabel(question, question.getCorrectOption()));


        return "test/student/question-result";
    }


    /**
     * Xử lý yêu cầu giải thích đáp án từ học sinh (POST)
     */
    @PostMapping("/explain")
    public String explain(@RequestParam Integer conversationId,
                          @ModelAttribute AnswerExplanationRequestDTO dto,
                          Model model) {
        // Gọi service để sinh giải thích từ Gemini
        List<ChatMessageDTO> messages = explanationService.explainAnswer(conversationId, dto);

        // Truyền lại dữ liệu sang view để hiển thị
        model.addAttribute("question", dto);
        model.addAttribute("chatMessages", messages);
        model.addAttribute("conversationId", conversationId);

        return "test/student/question-result";  // Trả về lại cùng view
    }
}
