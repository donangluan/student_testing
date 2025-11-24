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


    public List<Integer> findAllOfficialIds() {

        return aiGeneratedQuestionMapper.findAllOfficialIds();


    }


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
        if(topicId == null){
            System.out.println("Lỗi: Không tìm thấy Topic ID cho tên: "+topic );
            throw new RuntimeException("Không tìm thấy câu hỏi vì chủ đề không tồn tại: " +topic);
        }

        String courseName = topicService.findCourseNameByTopicId(topicId);
        if (courseName == null) {

            System.err.println("Cảnh báo: Không tìm thấy Course Name cho Topic ID: " + topicId);
        }

        String vietnameseDifficulty = mapDifficultyToVietnamese(difficulty);

        Integer difficultyId = difficultyService.getIdByName(vietnameseDifficulty);
        if (difficultyId == null) {
            throw new RuntimeException("Độ khó không hợp lệ: " + difficulty);
        }

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
            q.setContent(content);
            q.setCorrectAnswer(correct);
            q.setDifficulty(difficulty);
            q.setTopic(topic);
            q.setTopicId(topicId);
            q.setStatus("ACCEPTED");
            q.setCourseName(courseName);
            q.setCreatedAt(LocalDateTime.now());
            q.setTeacherId(teacherId);
            q.setSource("ai");
            q.setOptionA(a);
            q.setOptionB(b);
            q.setOptionC(c);
            q.setOptionD(d);


            q.setOptionsMap(optionsMap);

            try {
                q.setOptions(mapper.writeValueAsString(optionsMap));
            } catch (Exception e) {
                System.out.println(" Lỗi JSON tại index " + index + ": " + e.getMessage());
                continue;
            }

            toSave.add(q);
        }

        List<AiGeneratedQuestion> savedAndOfficial = new ArrayList<>();
        for (AiGeneratedQuestion q : toSave) {
            aiGeneratedQuestionMapper.insertQuestion(q);
            System.out.println(" Đã lưu: " + q.getContent() + " → ID: " + q.getId());
            if (q.getId() != null) {


                QuestionDTO qDTO = new QuestionDTO();
                qDTO.setContent(q.getContent());


                qDTO.setOptionA(q.getOptionsMap().get("A"));
                qDTO.setOptionB(q.getOptionsMap().get("B"));
                qDTO.setOptionC(q.getOptionsMap().get("C"));
                qDTO.setOptionD(q.getOptionsMap().get("D"));
                qDTO.setCorrectOption(q.getCorrectAnswer());
                qDTO.setDifficultyId(difficultyId);
                qDTO.setTopicId(q.getTopicId());
                qDTO.setCreatedBy(username);
                qDTO.setSource("ai");


                questionMapper.insert(qDTO);


                if (qDTO.getQuestionId() != null) {

                    Integer officialId = qDTO.getQuestionId();


                    q.setOfficialQuestionId(officialId);

                    aiGeneratedQuestionMapper.updateOfficialQuestionId(q.getId(), officialId);
                    savedAndOfficial.add(q);
                    System.out.println(" Đã gán Official Question ID: " + qDTO.getQuestionId());
                } else {
                    System.err.println("Lỗi: Không lấy được questionId sau khi INSERT câu hỏi chính thức.");
                }

            }
        }

        return savedAndOfficial;
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


            Integer existingId = questionMapper.findIdByContent(aiQ.getContent());
            if (existingId != null) {

                continue;
            }


            Integer topicId = aiQ.getTopicId();
            String topicName = aiQ.getTopic();

            System.out.printf("DEBUG GÁN: Đang xử lý AI Q ID=%d. Topic Name gốc: '%s'%n", aiQ.getId(), topicName);
            if (topicId == null && aiQ.getTopic() != null) {
                topicId = topicService.getIdByName(aiQ.getTopic());
            }
            if (topicId == null) {
                System.err.printf(" LỖI DỮ LIỆU: Bỏ qua câu hỏi AI ID = %d. Không tìm thấy Topic ID cho tên: %s%n", aiQ.getId(), aiQ.getTopic());
                continue;
            }

            String vietnameseDifficulty = mapDifficultyToVietnamese(aiQ.getDifficulty());
            Integer difficultyId = difficultyService.getIdByName(vietnameseDifficulty);

            if (difficultyId == null) {
                System.err.printf(" LỖI DỮ LIỆU: Bỏ qua câu hỏi AI ID = %d. Không tìm thấy Difficulty ID cho tên: %s (Tiếng Việt: %s)%n",
                        aiQ.getId(), aiQ.getDifficulty(), vietnameseDifficulty);
                continue;
            }


            QuestionDTO q = new QuestionDTO();
            q.setContent(aiQ.getContent());
            q.setOptionA(aiQ.getOptionA());
            q.setOptionB(aiQ.getOptionB());
            q.setOptionC(aiQ.getOptionC());
            q.setOptionD(aiQ.getOptionD());
            q.setCorrectOption(aiQ.getCorrectAnswer());


            q.setDifficultyId(difficultyId);
            q.setTopicId(topicId);

            q.setCreatedBy(aiQ.getCreatedBy());
            q.setSource(aiQ.getSource() != null ? aiQ.getSource() : "ai");


            questionMapper.insert(q);


            if (q.getQuestionId() != null) {
                aiQ.setOfficialQuestionId(q.getQuestionId());
                aiGeneratedQuestionMapper.updateOfficialQuestionId(aiQ.getId(), q.getQuestionId());
            }

            System.out.printf("  ĐÃ INSERT THÀNH CÔNG: Câu hỏi AI ID = %d vào bảng questions (Official ID = %d) với Topic ID=%d, Difficulty ID=%d%n",
                    aiQ.getId(), q.getQuestionId(), topicId, difficultyId);
        }
    }

    public List<AiGeneratedQuestion> getAiQuestionsByTestId(Integer topicId) {
        return aiGeneratedQuestionMapper.findByTopicId(topicId);
    }

    private String mapDifficultyToVietnamese(String englishDifficulty) {
        if (englishDifficulty == null) return null;
        return switch (englishDifficulty.toLowerCase().trim()) {
            case "easy" -> "Dễ";
            case "medium" -> "Trung bình";
            case "hard" -> "Khó";
            default -> englishDifficulty;
        };
    }



}
