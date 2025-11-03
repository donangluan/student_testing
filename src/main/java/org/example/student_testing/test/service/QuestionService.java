package org.example.student_testing.test.service;


import org.example.student_testing.chatbot.dto.AiGeneratedQuestionDTO;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.mapper.AiGeneratedQuestionMapper;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TestQuestionMapper testQuestionMapper;

    @Autowired
    private AiGeneratedQuestionMapper aiGeneratedQuestionMapper;


    public List<QuestionDTO> getAllQuestions(){
        return questionMapper.findAll();
    }

    public QuestionDTO getQuestionById(Integer questionId){
        return questionMapper.findById(questionId);
    }

    @Transactional
    public void createQuestion(QuestionDTO questionDTO){
        questionMapper.insert(questionDTO);
    }

    public void update(QuestionDTO questionDTO){
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO,question);
        questionMapper.update(question);

    }

    public void delete(Integer questionId){
        questionMapper.delete(questionId);
    }

    public List<QuestionDTO> getQuestionsByTestId(Integer testId) {
        return questionMapper.findQuestionsByTestId(testId);
    }

    public List<QuestionDTO> getQuestionsForTest(Integer testId) {
        return questionMapper.findQuestionsByTestId(testId);
    }

    public boolean isCorrect(Integer questionId, String selectedOption) {
        String correct = questionMapper.getCorrectOption(questionId);
        return correct != null && correct.equalsIgnoreCase(selectedOption);
    }

    public String getCorrectOption(Integer questionId) {
        return questionMapper.getCorrectOption(questionId);
    }

    public boolean canDeleteQuestion(Integer questionId) {
        return  testQuestionMapper.countByQuestionId(questionId) == 0;
    }

    public boolean deleteQuestion(Integer questionId) {
        if (canDeleteQuestion(questionId)) {
            questionMapper.delete(questionId);
            return true;
        }
        return false;
    }

    public List<QuestionDTO> previewQuestions(Integer topicId, Integer limit) {
        if (topicId == null || limit == null || limit <= 0) {
            throw new IllegalArgumentException("Thông tin đầu vào không hợp lệ.");
        }

        return questionMapper.randomQuestionsByTopic(topicId, limit);
    }

    public List<QuestionDTO> getQuestionsByTestIdAndStudent(Integer testId, String username) {
        return questionMapper.getQuestionsByTestIdAndStudent(testId, username);
    }

    public Question toEntity(QuestionDTO dto) {
        Question q = new Question();
        q.setQuestionId(dto.getQuestionId());
        q.setContent(dto.getContent());
        q.setOptionA(dto.getOptionA());
        q.setOptionB(dto.getOptionB());
        q.setOptionC(dto.getOptionC());
        q.setOptionD(dto.getOptionD());
        q.setCorrectOption(dto.getCorrectOption());
        q.setDifficultyId(dto.getDifficultyId());
        q.setTopicId(dto.getTopicId());
        q.setCreatedBy(dto.getCreatedBy());
        q.setCreatedAt(dto.getCreatedAt());
        return q;
    }

    public Integer getDifficultyByQuestionId(Integer questionId) {
        QuestionDTO manual = questionMapper.findById(questionId);
        if (manual != null) return manual.getDifficultyId();

        AiGeneratedQuestion ai = aiGeneratedQuestionMapper.findById(questionId);
        if (ai != null) return convertDifficulty(ai.getDifficulty());

        return null;
    }

    private Integer convertDifficulty(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            default -> null;
        };
    }

    public List<QuestionDTO> findByCourseAndTopic(String courseName, String topicName) {
        // 🔍 Lấy câu hỏi thủ công
        List<QuestionDTO> manualQuestions = questionMapper.findByCourseAndTopic(courseName, topicName);

        // 🔍 Lấy câu hỏi AI
        List<AiGeneratedQuestion> aiQuestions = aiGeneratedQuestionMapper.findByCourseAndTopic(courseName, topicName);

        // 🔁 Chuyển đổi AI sang QuestionDTO
        for (AiGeneratedQuestion ai : aiQuestions) {
            QuestionDTO dto = new QuestionDTO();
            dto.setQuestionId(ai.getId());
            dto.setContent(ai.getQuestionContent());
            dto.setCorrectOption(ai.getCorrectAnswer());
            dto.setDifficultyId(convertDifficulty(ai.getDifficulty()));
            dto.setTopicName(ai.getTopic());
            dto.setCreatedAt(ai.getCreatedAt());
            dto.setCreatedBy("AI");

            // Nếu có optionsMap
            if (ai.getOptionsMap() != null) {
                dto.setOptionA(ai.getOptionsMap().get("A"));
                dto.setOptionB(ai.getOptionsMap().get("B"));
                dto.setOptionC(ai.getOptionsMap().get("C"));
                dto.setOptionD(ai.getOptionsMap().get("D"));
            }

            manualQuestions.add(dto);
        }

        return manualQuestions;
    }




    public String getDifficulty(Integer questionId) {
        // 🔍 Kiểm tra trong bảng thủ công
        QuestionDTO manual = questionMapper.findById(questionId);
        if (manual != null) {
            return convertDifficultyToText(manual.getDifficultyId());
        }

        // 🔍 Nếu không có → kiểm tra trong bảng AI
        AiGeneratedQuestion ai = aiGeneratedQuestionMapper.findById(questionId);
        if (ai != null) {
            return ai.getDifficulty(); // đã là chuỗi: "Easy", "Medium", "Hard"
        }

        return "UNKNOWN";
    }

    private String convertDifficultyToText(Integer difficultyId) {
        return switch (difficultyId) {
            case 1 -> "EASY";
            case 2 -> "MEDIUM";
            case 3 -> "HARD";
            default -> "UNKNOWN";
        };
    }
}
