package org.example.student_testing.test.service;

import org.example.student_testing.test.dto.TestSubmissionDTO;
import org.example.student_testing.test.mapper.TestSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestSubmissionService {

    @Autowired
    private TestSubmissionMapper testSubmissionMapper;

    public List<TestSubmissionDTO> getAllSubmissionsForTeacher( String teacherUsername) {
        return testSubmissionMapper.getAllSubmissionsForTeacher( teacherUsername);
    }

    public int countGraded() {
        return testSubmissionMapper.countGradedSubmissions();
    }

    public void save(TestSubmissionDTO submission) {
        testSubmissionMapper.insert(submission);
    }

}
