package org.example.student_testing.test.service;

import org.example.student_testing.test.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestSchedulingService {

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private TestSubmissionService testSubmissionService;


    @Scheduled(fixedRate = 60000)
    public void autoSubmitExpiredTests() {
        LocalDateTime now = LocalDateTime.now();


        List<Integer> expiredTestIds = testMapper.findExpiredTestIds(now);

        if (expiredTestIds.isEmpty()) {
            return;
        }

        System.out.printf(" [SCHEDULER] Tìm thấy %d bài thi đã hết hạn cần thu hồi (trước %s).%n",
                expiredTestIds.size(), now);

        for (Integer testId : expiredTestIds) {

            List<String> activeStudents = testSubmissionService.findActiveStudentsForTest(testId);

            if (activeStudents.isEmpty()) {

                continue;
            }

            for (String studentUsername : activeStudents) {

                testSubmissionService.forceSubmit(testId, studentUsername);
                System.out.printf("   Tự động nộp bài cho %s (Test ID: %d)%n", studentUsername, testId);
            }
        }
    }
}
