package org.example.student_testing.test.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.student_testing.test.dto.QuestionViewDTO;
import org.example.student_testing.test.dto.SubmissionViewDTO;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestResultMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTestViewService {


    private final TestResultMapper testResultMapper;
    private final QuestionMapper questionMapper;

    public List<SubmissionViewDTO> getSubmissionsWithAnswers(Integer testId) {
        List<SubmissionViewDTO> submissions = testResultMapper.findSubmissionsByTestId(testId);
        log.info("🔍 Found {} submissions for testId={}", submissions.size(), testId);

        for (SubmissionViewDTO submission : submissions) {
            List<QuestionViewDTO> questions = questionMapper.findQuestionsWithAnswer(testId, submission.getStudentUsername());
            log.info("📘 Student={} has {} answered questions", submission.getStudentUsername(), questions.size());
            submission.setQuestions(questions);
        }

        return submissions;
    }
}
