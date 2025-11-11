package org.example.student_testing.test.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // THÊM IMPORT SLF4J

@Service
public class QuestionService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TestQuestionMapper testQuestionMapper;


    @Autowired
    private AiGeneratedQuestionMapper aiGeneratedQuestionMapper;



    private void normalizeQuestionOptions(QuestionDTO q) {

        if ("ai".equalsIgnoreCase(q.getSource()) && q.getOptionA() == null) {


            AiGeneratedQuestion aiData = aiGeneratedQuestionMapper.findByOfficialQuestionId(q.getQuestionId());

            if (aiData != null ) {
                Map<String, String> optionsMap = aiData.getOptionsMap();

                if (optionsMap == null && aiData.getOptions() != null) {
                    try {

                        optionsMap = objectMapper.readValue(
                                aiData.getOptions(),
                                new TypeReference<Map<String, String>>() {}
                        );

                        aiData.setOptionsMap(optionsMap);
                    } catch (Exception e) {
                        logger.error("Lỗi phân tích JSON Options từ string cho câu hỏi AI ID: {}", q.getQuestionId(), e);

                        return;
                    }
                }

                if (optionsMap != null) {
                    q.setOptionA(optionsMap.get("A"));
                    q.setOptionB(optionsMap.get("B"));
                    q.setOptionC(optionsMap.get("C"));
                    q.setOptionD(optionsMap.get("D"));
                    logger.info("Đã chuẩn hóa options cho câu hỏi AI ID: {}", q.getQuestionId());
                } else {
                    logger.warn("Dữ liệu optionsMap/options string không khả dụng cho AI ID: {}", q.getQuestionId());
                }

            } else {
                logger.warn("Không tìm thấy dữ liệu AI gốc cho Question ID: {}", q.getQuestionId());
            }
        }
    }



    public List<QuestionDTO> getQuestionsByTestId(Integer testId) {
        List<QuestionDTO> questions = questionMapper.findQuestionsByTestId(testId);
        questions.forEach(this::normalizeQuestionOptions);
        return questions;
    }


    public List<QuestionDTO> getQuestionsForTest(Integer testId) {
        List<QuestionDTO> questions = questionMapper.findQuestionsByTestId(testId);
        questions.forEach(this::normalizeQuestionOptions);
        return questions;
    }


    public List<QuestionDTO> getQuestionsByTestIdAndStudent(Integer testId, String username) {
        List<QuestionDTO> questions = questionMapper.getQuestionsByTestIdAndStudent(testId, username);
        questions.forEach(this::normalizeQuestionOptions);
        return questions;
    }



    public List<QuestionDTO> getAllQuestions(){
        return questionMapper.findAll();
    }

    public QuestionDTO getQuestionById(Integer questionId){

        QuestionDTO question = questionMapper.findById(questionId);


        if (question != null) {
            normalizeQuestionOptions(question);
        }

        return question;

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

        List<QuestionDTO> manualQuestions = questionMapper.findByCourseAndTopic(courseName, topicName);


        List<AiGeneratedQuestion> aiQuestions = aiGeneratedQuestionMapper.findByCourseAndTopic(courseName, topicName);


        for (AiGeneratedQuestion ai : aiQuestions) {
            QuestionDTO dto = new QuestionDTO();
            dto.setQuestionId(ai.getId());
            dto.setContent(ai.getQuestionContent());
            dto.setCorrectOption(ai.getCorrectAnswer());
            dto.setDifficultyId(convertDifficulty(ai.getDifficulty()));
            dto.setTopicName(ai.getTopic());
            dto.setCreatedAt(ai.getCreatedAt());
            dto.setCreatedBy("AI");

            Map<String, String> optionsMap = ai.getOptionsMap();

            if (optionsMap == null && ai.getOptions() != null) {
                try {
                    optionsMap = objectMapper.readValue(
                            ai.getOptions(),
                            new TypeReference<Map<String, String>>() {}
                    );
                } catch (Exception e) {
                    logger.error("Lỗi phân tích JSON Options khi hiển thị preview cho câu hỏi AI ID: {}", ai.getId(), e);
                }
            }

            if (optionsMap != null) {
                dto.setOptionA(optionsMap.get("A"));
                dto.setOptionB(optionsMap.get("B"));
                dto.setOptionC(optionsMap.get("C"));
                dto.setOptionD(optionsMap.get("D"));
            }

            manualQuestions.add(dto);
        }

        return manualQuestions;
    }




    public String getDifficulty(Integer questionId) {

        QuestionDTO manual = questionMapper.findById(questionId);
        if (manual != null) {
            return convertDifficultyToText(manual.getDifficultyId());
        }


        AiGeneratedQuestion ai = aiGeneratedQuestionMapper.findById(questionId);
        if (ai != null) {
            return ai.getDifficulty();
        }

        return "UNKNOWN";
    }

    public String convertDifficultyToText(Integer difficultyId) {
        if (difficultyId == null) {
            return "UNKNOWN";
        }

        return switch (difficultyId.intValue()) {
            case 1 -> "EASY";
            case 2 -> "MEDIUM";
            case 3 -> "HARD";
            default -> "UNKNOWN";
        };
    }
}