package org.example.student_testing.chatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.student_testing.chatbot.dto.AiGeneratedQuestionDTO;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.mapper.AiGeneratedQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AiQuestionService {

    @Autowired
    private AiGeneratedQuestionMapper  aiGeneratedQuestionMapper;

    @Autowired
    private GeminiService geminiService;


    public List<AiGeneratedQuestion> generate(Integer teacherId, String topic, String difficulty, int quantity) {

        String prompt = String.format("""
    Bạn là chuyên gia tạo đề kiểm tra và thẩm định nội dung. Nhiệm vụ của bạn là tạo chính xác %d câu hỏi trắc nghiệm theo định dạng JSON sau:

    {
      "questions": [
        {
          "content": "...",
          "optionA": "...",
          "optionB": "...",
          "optionC": "...",
          "optionD": "...",
          "correctAnswer": "A|B|C|D",
          "explanation": "...",
          "difficulty": "%s",
          "topic": "%s"
        }
      ]
    }

    Yêu cầu Nghiêm ngặt về Chất lượng Nội dung:
    1. **Độ Chính xác Tuyệt đối:** Đảm bảo tất cả thông tin, số liệu, và khái niệm trong câu hỏi, đáp án đúng, và lời giải thích phải **chính xác tuyệt đối và dựa trên kiến thức đã được xác thực (factual knowledge)**, không suy diễn.
    2. **Logic Nội bộ Bắt buộc:** Đáp án chính xác (`correctAnswer`) phải được **chứng minh bằng một lý lẽ khoa học/lịch sử/kiến thức cụ thể** trong phần giải thích (`explanation`). Lời giải thích phải là bằng chứng cho đáp án.
    3. **Phân bổ Đáp án Đồng đều:** Đáp án đúng (A, B, C, D) phải được **phân bổ ngẫu nhiên và đồng đều** trong toàn bộ %d câu hỏi (Ví dụ: 5 câu A, 5 câu B, 5 câu C, 5 câu D nếu có 20 câu).
    4. **Định dạng:** Chỉ trả về duy nhất một khối JSON, không thêm bất kỳ lời chào hay mô tả nào.
    5. **Nội dung:** Chủ đề: %s, Độ khó: %s.

    """, quantity, difficulty, topic, quantity, topic, difficulty);


        String aiText = geminiService.chat(prompt, List.of());


        String json = geminiService.extractJsonFromText(aiText);


        List<AiGeneratedQuestion> questions = geminiService.parseQuestionsFromJson(json);

        questions.forEach(this::processQuestionData);


        List<AiGeneratedQuestion> validQuestions = questions.stream()

                .filter(q -> {
                    try {
                        validateQuestion(q);
                        return true;
                    } catch (IllegalArgumentException e) {
                        log.warn("Loại bỏ câu hỏi AI không hợp lệ: {}", e.getMessage());
                        return false;
                    }
                })
                .toList();


        for (AiGeneratedQuestion q : validQuestions) { 
            q.setTeacherId(teacherId);
            q.setStatus("PENDING");
            q.setCreatedAt(LocalDateTime.now());
            aiGeneratedQuestionMapper.insertQuestion(q);
        }

        log.info("Đã tạo {} câu hỏi từ AI. Đã lọc và lưu {} câu hỏi hợp lệ.", questions.size(), validQuestions.size());
        return validQuestions;
    }


    public List<AiGeneratedQuestion> getByTeacher(Integer teacherId) {
        return aiGeneratedQuestionMapper.getByTeacherId(teacherId);
    }


    public void updateStatus(Integer questionId, String status) {
        aiGeneratedQuestionMapper.updateStatus(questionId, status);
    }

    public void updateQuestion(AiGeneratedQuestion question) {
        aiGeneratedQuestionMapper.updateQuestion(question);
    }

    public AiGeneratedQuestion findById(Integer id) {
        return aiGeneratedQuestionMapper.findById(id);
    }

    public void saveSingle(AiGeneratedQuestion question) {
        processQuestionData(question);
        question.setStatus("PENDING");
        question.setCreatedAt(LocalDateTime.now());
        aiGeneratedQuestionMapper.insertQuestion(question);
    }

    private void validateQuestion(AiGeneratedQuestion q) {

        if (q.getContent() == null || q.getContent().trim().isEmpty() ||
                q.getOptionA() == null || q.getOptionA().trim().isEmpty() ||
                q.getCorrectAnswer() == null || q.getCorrectAnswer().trim().isEmpty()) {

            throw new IllegalArgumentException("Dữ liệu câu hỏi AI bị thiếu các trường cơ bản (Content, OptionA, CorrectAnswer).");
        }


        if (!List.of("A", "B", "C", "D").contains(q.getCorrectAnswer().toUpperCase())) {
            throw new IllegalArgumentException("Đáp án đúng phải là A, B, C, hoặc D. Phát hiện đáp án không hợp lệ: " + q.getCorrectAnswer());
        }

    }


    private void processQuestionData(AiGeneratedQuestion q) {


        if (q.getCorrectAnswer() != null) {
            String correctContent = q.getCorrectAnswer().trim();

            if (correctContent.equalsIgnoreCase(q.getOptionA())) {
                q.setCorrectAnswer("A");
            } else if (correctContent.equalsIgnoreCase(q.getOptionB())) {
                q.setCorrectAnswer("B");
            } else if (correctContent.equalsIgnoreCase(q.getOptionC())) {
                q.setCorrectAnswer("C");
            } else if (correctContent.equalsIgnoreCase(q.getOptionD())) {
                q.setCorrectAnswer("D");
            } else {

                log.warn("Lỗi mapping đáp án đúng: Nội dung '{}' không khớp với Option A-D. Đặt mặc định là A.", correctContent);
                q.setCorrectAnswer("A");
            }
        }


        if (q.getCorrectAnswer() != null && q.getCorrectAnswer().length() == 1) {
            q.setCorrectAnswer(q.getCorrectAnswer().trim().toUpperCase());
        }


    }




}
