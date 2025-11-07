package org.example.student_testing.chatbot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.chatbot.dto.ChatMessageDTO;
import org.example.student_testing.chatbot.entity.ChatMessage;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insertMessage(ChatMessageDTO chatMessageDTO);

    List<ChatMessage> findByConversationId(Integer conversationId);

}
