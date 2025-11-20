package org.example.student_testing.chatbot.service;

import org.example.student_testing.chatbot.dto.ChatMessageDTO;
import org.example.student_testing.chatbot.entity.ChatMessage;
import org.example.student_testing.chatbot.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;


    public List<ChatMessageDTO> getMessagesByConversationId(Integer conversationId) {
        List<ChatMessage> entities = chatMessageMapper.findByConversationId(conversationId);
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }


    private ChatMessageDTO toDTO(ChatMessage entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(entity.getId());
        dto.setConversationId(entity.getConversationId());
        dto.setRole(entity.getRole());
        dto.setContent(entity.getContent());
        dto.setMetadata(entity.getMetadata());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
