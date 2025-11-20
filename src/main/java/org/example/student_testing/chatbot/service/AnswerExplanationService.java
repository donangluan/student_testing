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
    private GeminiService geminiService;

    @Autowired
    private ChatMessageMapper chatMessageMapper;


    public List<ChatMessageDTO> explainAnswer(Integer conversationId, AnswerExplanationRequestDTO dto) {

        String prompt = geminiService.buildExplanationPrompt(dto);


        String aiResponse = geminiService.chat(prompt, List.of());


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



        ChatMessageDTO userMsg = new ChatMessageDTO();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("USER");
        userMsg.setContent("Tại sao em sai câu này?");
        userMsg.setMetadata(metadataJson);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insertMessage(userMsg);


        ChatMessageDTO botMsg = new ChatMessageDTO();
        botMsg.setConversationId(conversationId);
        botMsg.setRole("ASSISTANT");
        botMsg.setContent(aiResponse);
        botMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insertMessage(botMsg);

        return List.of(userMsg, botMsg);
    }

    public String findAnswerLabel(QuestionDTO q, String selectedOption) {
        if ("A".equalsIgnoreCase(selectedOption)) {

            String optionA = q.getOptionA();
            return optionA != null ? optionA.trim() : "";
        }
        if ("B".equalsIgnoreCase(selectedOption)) {
            String optionB = q.getOptionB();
            return optionB != null ? optionB.trim() : "";
        }
        if ("C".equalsIgnoreCase(selectedOption)) {
            String optionC = q.getOptionC();
            return optionC != null ? optionC.trim() : "";
        }
        if ("D".equalsIgnoreCase(selectedOption)) {
            String optionD = q.getOptionD();
            return optionD != null ? optionD.trim() : "";
        }

        return "";
    }



}
