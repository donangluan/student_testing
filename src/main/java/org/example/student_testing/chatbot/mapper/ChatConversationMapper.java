package org.example.student_testing.chatbot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatConversationMapper {

    void insertConversation(@Param("id") Integer id, @Param("username") String username);

}
