package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.test.dto.ChatbotMessageDTO;

import java.util.List;

@Mapper
public interface ChatbotMapper {

    void insertMessage(ChatbotMessageDTO  ChatbotMessageDTO );

    List<ChatbotMessageDTO> getMessagesByUser(String username);

}
