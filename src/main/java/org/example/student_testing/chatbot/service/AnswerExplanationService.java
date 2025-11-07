package org.example.student_testing.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.student_testing.chatbot.dto.AnswerExplanationRequestDTO;
import org.example.student_testing.chatbot.dto.ChatMessageDTO;
import org.example.student_testing.chatbot.entity.ChatMessage;
import org.example.student_testing.chatbot.mapper.ChatMessageMapper;
import org.example.student_testing.test.dto.QuestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AnswerExplanationService {

    @Autowired
    private GeminiService geminiService; // Gọi AI sinh giải thích

    @Autowired
    private ChatMessageMapper chatMessageMapper;


    public List<ChatMessageDTO> explainAnswer(Integer conversationId, AnswerExplanationRequestDTO dto) {
        // Tạo prompt từ dữ liệu
        String prompt = geminiService.buildExplanationPrompt(dto);

        // Gọi Gemini để sinh giải thích
        String aiResponse = geminiService.chat(prompt, List.of());

        // Tạo metadata JSON
        Map<String, Object> metadataMap = Map.of(
                "questionId", dto.getQuestionId(),
                "studentAnswer", dto.getStudentAnswer(),
                "correctAnswer", dto.getCorrectAnswer()
        );
        String metadataJson;
        try {
            metadataJson = new ObjectMapper().writeValueAsString(metadataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi chuyển metadata sang JSON", e);
        }


        // Lưu tin nhắn học sinh
        ChatMessageDTO userMsg = new ChatMessageDTO();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("USER");
        userMsg.setContent("Tại sao em sai câu này?");
        userMsg.setMetadata(metadataJson);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insertMessage(userMsg);

        // Lưu phản hồi của AI
        ChatMessageDTO botMsg = new ChatMessageDTO();
        botMsg.setConversationId(conversationId);
        botMsg.setRole("ASSISTANT");
        botMsg.setContent(aiResponse);
        botMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insertMessage(botMsg);

        return List.of(userMsg, botMsg);
    }

    public String findAnswerLabel(QuestionDTO q, String selectedContent) {
        if (selectedContent == null) return "";
        selectedContent = selectedContent.trim();
        if (selectedContent.equalsIgnoreCase(q.getOptionA().trim())) return "A";
        if (selectedContent.equalsIgnoreCase(q.getOptionB().trim())) return "B";
        if (selectedContent.equalsIgnoreCase(q.getOptionC().trim())) return "C";
        if (selectedContent.equalsIgnoreCase(q.getOptionD().trim())) return "D";
        return selectedContent;
    }



}
