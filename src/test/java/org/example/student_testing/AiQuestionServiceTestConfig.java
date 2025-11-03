package org.example.student_testing;

import org.example.student_testing.chatbot.service.AiQuestionService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AiQuestionServiceTestConfig {

    @Bean
    public AiQuestionService aiQuestionService() {
        // Trả về mock hoặc fake implementation
        return Mockito.mock(AiQuestionService.class);
    }
}
