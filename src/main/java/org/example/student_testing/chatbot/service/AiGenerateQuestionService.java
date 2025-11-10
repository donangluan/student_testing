package org.example.student_testing.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.mapper.AiGeneratedQuestionMapper;
import org.example.student_testing.student.mapper.UserMapper;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.service.DifficultyService;
import org.example.student_testing.test.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiGenerateQuestionService {

    @Autowired
    private AiGeneratedQuestionMapper aiGeneratedQuestionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private DifficultyService difficultyService;

    @Autowired
    private TopicService topicService;


    public List<AiGeneratedQuestion> processAndSave(
            List<Integer> selectedIndexes,
            Map<String, String> contents,
            Map<String, String> corrects,
            String topic,
            String difficulty,
            Map<String, String> answersA,
            Map<String, String> answersB,
            Map<String, String> answersC,
            Map<String, String> answersD,
            String username
    ) {
        Integer teacherId = userMapper.findTeacherIdByUsername(username);
        if (teacherId == null) throw new RuntimeException("Không tìm thấy teacherId cho username: " + username);

        Integer topicId = topicService.getIdByName(topic);

        List<AiGeneratedQuestion> toSave = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Integer index : selectedIndexes) {
            String key = String.valueOf(index);
            String content = contents.get(key);
            String correct = corrects.get(key);
            String a = answersA.get(key);
            String b = answersB.get(key);
            String c = answersC.get(key);
            String d = answersD.get(key);

            if (isBlank(content) || isBlank(correct) || isBlank(a) || isBlank(b) || isBlank(c) || isBlank(d)) continue;

            Map<String, String> optionsMap = Map.of("A", a, "B", b, "C", c, "D", d);

            AiGeneratedQuestion q = new AiGeneratedQuestion();
            q.setQuestionContent(content);
            q.setCorrectAnswer(correct);
            q.setDifficulty(difficulty);
            q.setTopic(topic);
            q.setTopicId(topicId);
            q.setStatus("ACCEPTED");
            q.setCreatedAt(LocalDateTime.now());
            q.setTeacherId(teacherId);
            q.setSource("ai");

            q.setOptionsMap(optionsMap);

            try {
                q.setOptions(mapper.writeValueAsString(optionsMap));
            } catch (Exception e) {
                System.out.println(" Lỗi JSON tại index " + index + ": " + e.getMessage());
                continue;
            }

            toSave.add(q);
        }

        List<AiGeneratedQuestion> saved = new ArrayList<>();
        for (AiGeneratedQuestion q : toSave) {
            aiGeneratedQuestionMapper.insertQuestion(q);
            System.out.println(" Đã lưu: " + q.getQuestionContent() + " → ID: " + q.getId());
            if (q.getId() != null) saved.add(q);
        }

        return saved;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


    public void saveSingle(AiGeneratedQuestion q) {
        if (q.getTopicId() == null && q.getTopic() != null) {
            Integer topicId = topicService.getIdByName(q.getTopic());
            q.setTopicId(topicId);
        }
        if (q.getSource() == null) {
            q.setSource("ai");
        }
        aiGeneratedQuestionMapper.insertQuestion(q);
    }

    public List<AiGeneratedQuestion> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return aiGeneratedQuestionMapper.findByIds(ids);
    }

    public AiGeneratedQuestion findById(Integer id) {
        return aiGeneratedQuestionMapper.findById(id);
    }

    public List<AiGeneratedQuestion> findByCourseAndTopic(String courseName, String topicName) {
        return aiGeneratedQuestionMapper.findByCourseAndTopic(courseName, topicName);
    }

    public List<AiGeneratedQuestion> findByCourse(String courseName) {
        return aiGeneratedQuestionMapper.findByCourse(courseName);
    }

    public List<AiGeneratedQuestion> findByCourseId(Integer courseId) {
        return aiGeneratedQuestionMapper.findByCourseId(courseId);
    }

    public List<Integer> findAllIds() {
        return aiGeneratedQuestionMapper.findAllIds();
    }

    @Transactional
    public void convertAiQuestionsToOfficial(List<AiGeneratedQuestion> aiQuestions) {
        for (AiGeneratedQuestion aiQ : aiQuestions) {

            if (aiQ.getSource() == null || aiQ.getSource().trim().isEmpty()) {
                aiQ.setSource("ai");
            }

            System.out.printf(" ID = %d | source = %s | content = %s%n",
                    aiQ.getId(), aiQ.getSource(), aiQ.getQuestionContent());

            Integer existingId = questionMapper.findIdByContent(aiQ.getQuestionContent());
            if (existingId != null) {
                System.out.printf(" Bỏ qua câu hỏi AI ID = %d vì đã tồn tại trong bảng questions (ID = %d)%n", aiQ.getId(), existingId);
                continue;
            }

            QuestionDTO q = new QuestionDTO();
            q.setContent(aiQ.getQuestionContent());
            q.setOptionA(aiQ.getOptionA());
            q.setOptionB(aiQ.getOptionB());
            q.setOptionC(aiQ.getOptionC());
            q.setOptionD(aiQ.getOptionD());
            q.setCorrectOption(aiQ.getCorrectAnswer());
            q.setDifficultyId(difficultyService.getIdByName(aiQ.getDifficulty()));
            q.setTopicId(aiQ.getTopicId());
            q.setCreatedBy(aiQ.getCreatedBy());
            q.setSource(aiQ.getSource());

            questionMapper.insert(q);
            System.out.printf(" Đã insert câu hỏi AI ID = %d vào bảng questions%n", aiQ.getId());
        }
    }


    public List<AiGeneratedQuestion> getAiQuestionsByTestId(Integer topicId) {
        return aiGeneratedQuestionMapper.findByTopicId(topicId);
    }

}
