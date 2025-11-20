package org.example.student_testing.test.service;

import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.dto.TestSubmissionDTO;
import org.example.student_testing.test.mapper.StudentAnswerMapper;
import org.example.student_testing.test.mapper.TestMapper;
import org.example.student_testing.test.mapper.TestSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestSubmissionService {

    @Autowired
    private TestSubmissionMapper testSubmissionMapper;

    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private  TestMapper testMapper;

    @Autowired
    private  TestSessionService testSessionService;

    @Autowired
    private  QuestionService questionService;

    @Autowired
    private  TestResultService testResultService;

    public List<TestSubmissionDTO> getAllSubmissionsForTeacher( String teacherUsername) {
        return testSubmissionMapper.getAllSubmissionsForTeacher( teacherUsername);
    }

    public int countGraded() {
        return testSubmissionMapper.countGradedSubmissions();
    }

    public void save(TestSubmissionDTO submission) {
        testSubmissionMapper.insert(submission);
    }


    public List<String> findActiveStudentsForTest(Integer testId) {

        return testMapper.findStudentsAssignedButNotSubmitted(testId);
    }


    @Transactional
    public void forceSubmit(Integer testId, String studentUsername) {

        Map<Integer, String> parsedAnswers = new HashMap<>();


        testSessionService.getSession(testId, studentUsername).ifPresent(session -> {
            parsedAnswers.putAll(session.getAnswersMap());
        });

        studentAnswerService.saveAnswers(testId, studentUsername, parsedAnswers);


        int correctCount = 0;
        List<QuestionDTO> questions = questionService.getQuestionsByTestId(testId);
        int totalQuestions = questions.size();

        for (QuestionDTO q : questions) {
            String studentAnswer = parsedAnswers.get(q.getQuestionId());
            String correctOption = questionService.getCorrectOption(q.getQuestionId());

            if (studentAnswer != null && correctOption != null && studentAnswer.equalsIgnoreCase(correctOption)) {
                correctCount++;
            }
        }

        double finalScore = (totalQuestions > 0) ?
                Math.round(((double) correctCount / totalQuestions) * 1000.0) / 100.0 : 0.0;



        TestResultDTO result = new TestResultDTO();
        result.setTestId(testId);
        result.setStudentUsername(studentUsername);
        result.setScore(finalScore);
        result.setCompletedAt(LocalDateTime.now());

        testResultService.save(result);


        testSessionService.clearSession(testId, studentUsername);
    }

}
