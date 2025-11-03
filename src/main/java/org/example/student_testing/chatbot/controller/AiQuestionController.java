package org.example.student_testing.chatbot.controller;

import org.example.student_testing.chatbot.dto.AiGeneratedQuestionDTO;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.service.AiQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-questions")
public class AiQuestionController {



        @Autowired
        private AiQuestionService aiQuestionService;

        /**
         * API để giáo viên tạo câu hỏi bằng AI.
         *
         * @param teacherId ID giáo viên
         * @param topic Chủ đề (ví dụ: "Đạo hàm")
         * @param difficulty Độ khó ("Easy", "Medium", "Hard")
         * @param quantity Số lượng câu hỏi cần tạo
         * @return Danh sách câu hỏi đã sinh và lưu vào DB
         */
        @PostMapping("/generate")
        public ResponseEntity<List<AiGeneratedQuestion>> generateQuestions(
            @RequestParam Integer teacherId,
            @RequestParam String topic,
            @RequestParam String difficulty,
        @RequestParam int quantity
    ) {
        List<AiGeneratedQuestion> questions = aiQuestionService.generate(teacherId, topic, difficulty, quantity);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/save")
    public ResponseEntity<AiGeneratedQuestion> saveSingleQuestion(@RequestBody AiGeneratedQuestion question) {
        aiQuestionService.saveSingle(question);
        return ResponseEntity.ok(question);
    }

        /**
         * API để lấy danh sách câu hỏi AI đã tạo theo giáo viên.
         *
         * @param teacherId ID giáo viên
         * @return Danh sách câu hỏi
         */
        @GetMapping("/teacher/{teacherId}")
        public ResponseEntity<List<AiGeneratedQuestion>> getQuestionsByTeacher(@PathVariable Integer teacherId) {
        List<AiGeneratedQuestion> questions = aiQuestionService.getByTeacher(teacherId);
        return ResponseEntity.ok(questions);
    }

        /**
         * API để cập nhật trạng thái câu hỏi (ví dụ: ACCEPTED, REJECTED).
         *
         * @param questionId ID câu hỏi
         * @param status Trạng thái mới
         * @return Trạng thái cập nhật thành công
         */
        @PutMapping("/{questionId}/status")
        public ResponseEntity<String> updateQuestionStatus(
            @PathVariable Integer questionId,
            @RequestParam String status
    ) {
        aiQuestionService.updateStatus(questionId, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }


    @PutMapping("/update")
    public ResponseEntity<AiGeneratedQuestion> updateQuestion(@RequestBody AiGeneratedQuestion question) {
        aiQuestionService.updateQuestion(question);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiGeneratedQuestion> getQuestionById(@PathVariable Integer id) {
        AiGeneratedQuestion question = aiQuestionService.findById(id);
        return ResponseEntity.ok(question);
    }






}
