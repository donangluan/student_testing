package org.example.student_testing.chatbot.service;

import org.example.student_testing.chatbot.dto.AiGeneratedQuestionDTO;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.mapper.AiGeneratedQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiQuestionService {

    @Autowired
    private AiGeneratedQuestionMapper  aiGeneratedQuestionMapper;

    @Autowired
    private GeminiService geminiService;

    /**
     * Gọi AI để sinh câu hỏi theo yêu cầu giáo viên, lưu vào DB và trả về danh sách.
     *
     * @param teacherId ID của giáo viên tạo câu hỏi
     * @param topic Chủ đề (ví dụ: "Đạo hàm")
     * @param difficulty Độ khó ("Easy", "Medium", "Hard")
     * @param quantity Số lượng câu hỏi cần tạo
     * @return Danh sách câu hỏi đã lưu vào DB
     */
    public List<AiGeneratedQuestion> generate(Integer teacherId, String topic, String difficulty, int quantity) {

        String prompt = String.format("""
    Bạn là chuyên gia tạo đề kiểm tra. Hãy tạo chính xác %d câu hỏi trắc nghiệm theo định dạng JSON sau:

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

    Yêu cầu:
    - Chủ đề: %s
    - Độ khó: %s
    - Mỗi câu hỏi phải rõ ràng, không mơ hồ, không gây hiểu sai
    - Nếu không chắc chắn về kiến thức, hãy bỏ qua câu hỏi đó
    - Không tạo câu hỏi suy diễn hoặc không có đáp án rõ ràng
    - Đảm bảo đúng định dạng JSON như trên, không thêm mô tả ngoài JSON
    """, quantity, difficulty, topic, topic, difficulty);


        String aiText = geminiService.chat(prompt, List.of());


        String json = geminiService.extractJsonFromText(aiText);


        List<AiGeneratedQuestion> questions = geminiService.parseQuestionsFromJson(json);


        for (AiGeneratedQuestion q : questions) {
            q.setTeacherId(teacherId);
            q.setStatus("PENDING");
            q.setCreatedAt(LocalDateTime.now());
            aiGeneratedQuestionMapper.insertQuestion(q);
        }
        System.out.println("question" +questions );


        return questions;
    }

    /**
     * Lấy danh sách câu hỏi AI đã tạo theo giáo viên.
     *
     * @param teacherId ID giáo viên
     * @return Danh sách câu hỏi
     */
    public List<AiGeneratedQuestion> getByTeacher(Integer teacherId) {
        return aiGeneratedQuestionMapper.getByTeacherId(teacherId);
    }

    /**
     * Cập nhật trạng thái câu hỏi (ví dụ: ACCEPTED, REJECTED).
     *
     * @param questionId ID câu hỏi
     * @param status Trạng thái mới
     */
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
        question.setStatus("PENDING");
        question.setCreatedAt(LocalDateTime.now());
        aiGeneratedQuestionMapper.insertQuestion(question);
    }




}
