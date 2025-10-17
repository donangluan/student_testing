package org.example.student_testing.test.service;


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

    public List<QuestionDTO> getAllQuestions(){
        return questionMapper.findAll();
    }

    public QuestionDTO getQuestionById(Integer questionId){
        return questionMapper.findById(questionId);
    }

    @Transactional
    public void createQuestion(QuestionDTO questionDTO){
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO,question);
        questionMapper.insert(question);
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
}
