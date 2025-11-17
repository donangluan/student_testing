package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.QuestionDTO;

import java.util.List;

@Mapper
public interface TestQuestionMapper {

    void insertQuestionForFixedTest(
            @Param("testId") Integer testId,
            @Param("questionId") Integer questionId,
            @Param("difficultyId") Integer difficultyId,
            @Param("orderNo") Integer orderNo,
            @Param("source") String source
    );

    // 2. Dùng để chèn câu hỏi riêng cho học sinh (Dùng cho đề Unique/Dynamic)
    void insertQuestionForStudent(
            @Param("testId") Integer testId,
            @Param("questionId") Integer questionId,
            @Param("studentUsername") String studentUsername,
            @Param("difficultyId") Integer difficultyId,
            @Param("orderNo") Integer orderNo,
            @Param("source") String source
    );

    // --- Phương thức tải đề (Phải thêm lại để Service hoạt động) ---

    // 3. Tải câu hỏi cố định/chung (student_username IS NULL)
    List<QuestionDTO> findFixedQuestionsByTestId(
            @Param("testId") Integer testId
    );

    // 4. Tải câu hỏi riêng cho học sinh (student_username = #{studentUsername})
    List<QuestionDTO> findDynamicQuestionsByTestIdAndStudent(
            @Param("testId") Integer testId,
            @Param("studentUsername") String studentUsername
    );

    // --- Các phương thức Utility khác (Đã được đơn giản hóa/giữ lại) ---

    void deleteTestQuestion(Integer questionId);

    int countByQuestionId(@Param("questionId") Integer questionId);

    // Giữ lại hàm cũ, mặc dù findFixedQuestionsByTestId có thể thay thế nó.
    List<QuestionDTO> findQuestionsByTestId(@Param("testId") Integer testId);

    int countQuestionsInTest(@Param("testId") Integer testId);

    Integer findMaxOrderNoByTestId(@Param("testId") Integer testId);

    int countAssignedQuestionsForStudent(@Param("testId") Integer testId, @Param("studentUsername") String studentUsername);

}
