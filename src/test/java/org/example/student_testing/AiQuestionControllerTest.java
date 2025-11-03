package org.example.student_testing;

import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.service.AiQuestionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;


import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AiQuestionServiceTestConfig.class)
public class AiQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiQuestionService aiQuestionService;

    @Test
    @WithMockUser(roles = "TEACHER")
    void testGenerateQuestions() throws Exception {
        AiGeneratedQuestion q = new AiGeneratedQuestion();
        q.setQuestionContent("Câu hỏi 1");
        q.setDifficulty("Medium");
        q.setTopic("Đạo hàm");
        q.setCorrectAnswer("A");
        q.setStatus("PENDING");
        q.setCreatedAt(LocalDateTime.now());
        List<AiGeneratedQuestion> mockQuestions = List.of(q);

        when(aiQuestionService.generate(anyInt(), anyString(), anyString(), anyInt()))
                .thenReturn(mockQuestions);


        mockMvc.perform(post("/api/ai-questions/generate")
                        .param("teacherId", "1")
                        .param("topic", "Đạo hàm")
                        .param("difficulty", "Medium")
                        .param("quantity", "5")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].questionContent").value("Câu hỏi 1"))
                .andExpect(jsonPath("$[0].difficulty").value("Medium"))
                .andExpect(jsonPath("$[0].topic").value("Đạo hàm"))
        .andExpect(jsonPath("$[0].correctAnswer").value("A"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].createdAt").exists());

    }
}
