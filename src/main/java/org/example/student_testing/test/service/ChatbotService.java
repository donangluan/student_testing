package org.example.student_testing.test.service;

import org.example.student_testing.test.dto.ChatbotMessageDTO;
import org.example.student_testing.test.mapper.ChatbotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotService {

    @Autowired
    private ChatbotMapper chatbotMapper;

    @Autowired
    private AiClientService aiClientService;


    public String reply(String message, String username, String role) {
        String response;

        try {
            response = aiClientService.ask(message); // Gọi AI
        } catch (Exception e) {
            // Fallback nếu AI lỗi hoặc quota hết
            response = fallbackReply(message);
        }

        ChatbotMessageDTO dto = new ChatbotMessageDTO();
        dto.setUsername(username);
        dto.setRole(role);
        dto.setMessage(message);
        dto.setResponse(response);
        chatbotMapper.insertMessage(dto);

        return response;
    }

    private String fallbackReply(String message) {
        message = message.toLowerCase();
        if (message.contains("nộp bài")) {
            return "Bạn có thể nộp bài kiểm tra tại mục 'Bài kiểm tra' trong trang cá nhân.";
        } else if (message.contains("lớp")) {
            return "Bạn đang học lớp được gán bởi admin. Vào mục 'Lớp học' để xem chi tiết.";
        } else if (message.contains("giáo viên")) {
            return "Giáo viên của bạn sẽ hiển thị trong danh sách lớp.";
        } else {
            return "Chatbot hiện đang quá tải hoặc bạn đã hết lượt hỏi. Bạn có thể hỏi về lớp, bài kiểm tra, hoặc giáo viên.";
        }
    }

    public List<ChatbotMessageDTO> getHistory(String username) {
        return chatbotMapper.getMessagesByUser(username);
    }
}
