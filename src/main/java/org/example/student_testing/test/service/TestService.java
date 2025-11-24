package org.example.student_testing.test.service;


import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
import org.example.student_testing.chatbot.mapper.ChatConversationMapper;
import org.example.student_testing.chatbot.service.AiGenerateQuestionService;
import org.example.student_testing.test.dto.*;
import org.example.student_testing.test.entity.Question;
import org.example.student_testing.test.mapper.QuestionMapper;
import org.example.student_testing.test.mapper.TestMapper;
import org.example.student_testing.test.mapper.TestQuestionMapper;
import org.example.student_testing.test.mapper.TestResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class    TestService {

    private final TestMapper testMapper;
    private final QuestionMapper questionMapper;
    @Autowired
    private  QuestionService questionService;


    @Autowired
    private  TopicService topicService;
    @Autowired
    private TestQuestionMapper testQuestionMapper;

    @Autowired
    private AiGenerateQuestionService  aiGenerateQuestionService;

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Autowired
    private TestResultMapper testResultMapper;



    public TestService(TestMapper testMapper, QuestionMapper questionMapper) {
        this.testMapper = testMapper;
        this.questionMapper = questionMapper;
    }

    private void cleanAccessCode(TestDTO test) {
        if (test.getAccessCode() != null && test.getAccessCode().trim().isEmpty()) {
            test.setAccessCode(null);
        }
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

    private Set<Integer> getAiQuestionIdSet() {
        List<Integer> aiIds = aiGenerateQuestionService.findAllIds();
        return new HashSet<>(aiIds);
    }

    @Transactional
    @CacheEvict(value = "student-tests", allEntries = true)
    public void generateUniqueTest(UniqueTestRequest request, String createdBy) {

        TestDTO test = new TestDTO();
        test.setTestName(request.getTestName());
        test.setTestType(request.getTestType());
        test.setCreatedBy(createdBy);
        test.setCreatedAt(LocalDateTime.now());
        test.setTopicId(request.getTopicId());
        test.setDurationMinutes(request.getDurationMinutes());
        test.setPublished(true);
        test.setStartTime(request.getStartTime());
        test.setEndTime(request.getEndTime());

        test.setAccessCode(request.getAccessCode());
        cleanAccessCode(test);

        testMapper.insertTest(test);


        Integer courseId = topicService.getCourseIdByTopicId(request.getTopicId());


        List<AiGeneratedQuestion> aiQuestions = aiGenerateQuestionService.findByCourseId(courseId);


        aiGenerateQuestionService.convertAiQuestionsToOfficial(aiQuestions);


        List<QuestionDTO> selectedQuestions = questionMapper.randomQuestionsByTopic(
                request.getTopicId(), request.getNumberOfQuestions()
        );

        int requested = request.getNumberOfQuestions();
        int actual = selectedQuestions.size();

        if (actual < requested) {
            throw new RuntimeException(
                    String.format("Không đủ câu hỏi cho chủ đề. Yêu cầu %d, chỉ tìm thấy %d.", requested, actual)
            );
        }


        Set<Integer> aiQuestionIds = getAiQuestionIdSet();

        int order = 1;

        for (QuestionDTO q : selectedQuestions) {
            Integer qId = q.getQuestionId();
            if (qId == null) continue;


            Integer topicId = q.getTopicId();
            if (topicId == null) {
                System.err.println("CẢNH BÁO: Question ID " + qId + " thiếu Topic ID. Bỏ qua.");
                continue;
            }

            String source = aiQuestionIds.contains(qId) ? "ai" : "manual";


            testQuestionMapper.insertQuestionForStudent(
                    test.getTestId(),
                    qId,
                    createdBy,

                    q.getDifficultyId(),
                    topicId,
                    order++,
                    source
            );
        }


        for (String studentUsername : request.getStudentUsername()) {
            if (studentUsername == null || studentUsername.isBlank()) continue;

            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(test.getTestId());
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);
        }
    }


    public List<TestDTO> findAll() {
        return testMapper.findAllTests();
    }

    @Transactional
    @CacheEvict(value = "student-tests", allEntries = true)
    public void createMixedTopicTest(MixedTopicTestDTO dto, List<String> studentUsernames) {
        if (dto.getTopicDistribution() == null || dto.getTopicDistribution().isEmpty()) {
            throw new IllegalArgumentException("Phân phối chủ đề không được để trống.");
        }

        TestDTO testDTO = new TestDTO();
        testDTO.setTestName(dto.getTestName());
        testDTO.setCreatedBy(dto.getCreatedBy());
        testDTO.setTestType("Mixed");
        testDTO.setCreatedAt(LocalDateTime.now());

        testDTO.setPublished(true);
        testDTO.setStartTime(dto.getStartTime());
        testDTO.setEndTime(dto.getEndTime());

        testDTO.setAccessCode(dto.getAccessCode()); // Lấy mã từ DTO
        cleanAccessCode(testDTO);

        testMapper.insertTest(testDTO);

        Integer testId = testDTO.getTestId();


        Set<Integer> aiQuestionIds = getAiQuestionIdSet();


        Set<Integer> uniqueQuestionIds = new HashSet<>();

        int questionOrder = 1;


        for (Map.Entry<Integer, Integer> entry : dto.getTopicDistribution().entrySet()) {
            Integer topicId = entry.getKey();
            Integer count = entry.getValue();

            if (count == null || count <= 0) continue;

            List<QuestionDTO> questionDTOs = questionMapper.findRandomQuestionsByTopic(topicId, count);
            if (questionDTOs == null || questionDTOs.isEmpty()) {
                System.out.println(" Không tìm thấy câu hỏi cho chủ đề " + topicId);
                continue;
            }



            for (QuestionDTO q : questionDTOs) {
                Integer qId = q.getQuestionId();
                if (qId == null) continue;


                if (!uniqueQuestionIds.add(qId)) {
                    System.out.println("CẢNH BÁO: Câu hỏi ID " + qId + " đã bị trùng khi rút. Bỏ qua.");
                    continue;
                }



                String source = aiQuestionIds.contains(qId) ? "ai" : "manual";


                testQuestionMapper.insertQuestionForFixedTest(
                        testId,
                        qId,
                        q.getDifficultyId(),
                        q.getTopicId(),
                        questionOrder++,
                        source
                );
            }
        }


        for (String studentUsername : studentUsernames) {
            if (studentUsername == null || studentUsername.isBlank()) continue;

            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(testId);
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);


        }
    }






    public int getTotalTests() {
        return testMapper.countAllTests();
    }

    @Cacheable(value = "student-tests", key = "#studentUsername")
    public List<TestDTO> findTestsForStudent(String studentUsername) {
        System.out.println(">>> [DB CALL] Đang lấy danh sách bài thi cho sinh viên: " + studentUsername);
        return testMapper.findTestsAssignedToStudent(studentUsername);
    }

    public List<TestResultDTO> getResultsForStudent(String studentUsername) {
        return testMapper.findResultsByStudent(studentUsername);
    }
    @CacheEvict(value = "student-tests", key = "#studentUsername")
    public void assignTestToStudent(Integer testId, String studentUsername) {
        TestDTO existingTest = testMapper.findTestById(testId);
        if (existingTest == null) {

            throw new IllegalArgumentException("Lỗi gán: Bài kiểm tra ID " + testId + " không tồn tại.");
        }

        TestAssignmentDTO ta = new TestAssignmentDTO();
        ta.setTestId(testId);
        ta.setStudentUsername(studentUsername);
        ta.setAssignedAt(LocalDateTime.now());
        testMapper.insertTestAssignment(ta);
    }
    @Cacheable(value = "tests", key = "#testId")
    public TestDTO getTestById(Integer testId) {
        System.out.println(">>> [DB CALL] Đang lấy chi tiết Test ID: " + testId);

        return testMapper.findTestById(testId);
    }


    @CacheEvict(value = "student-tests", allEntries = true)
    public void createAiTest(String testName, String topic, List<Integer> questionIds,
                             List<String> studentUsernames, String teacherUsername) {


        TestDTO test = new TestDTO();
        test.setTestName(testName);
        test.setTopicName(topic);
        test.setTestType("AI");
        test.setCreatedBy(teacherUsername);
        test.setCreatedAt(LocalDateTime.now());

        test.setPublished(true);
        test.setStartTime(LocalDateTime.now());
        testMapper.insertTest(test);


        int order = 1;


        Map<Integer, QuestionDTO> questionMap = questionService.findQuestionMapByIds(questionIds);
        List<Integer> aiIds = aiGenerateQuestionService.findAllIds();

        for (Integer qId : questionIds) {
            QuestionDTO q = questionMap.get(qId);
            if (q == null) continue;

            String source = aiIds.contains(qId) ? "ai" : "manual";

            testQuestionMapper.insertQuestionForFixedTest(
                    test.getTestId(),
                    qId,
                    q.getDifficultyId(),
                    q.getTopicId(),
                    order++,
                    source
            );
        }


        for (String studentUsername : studentUsernames) {
            TestAssignmentDTO ta = new TestAssignmentDTO();
            ta.setTestId(test.getTestId());
            ta.setStudentUsername(studentUsername);
            ta.setAssignedAt(LocalDateTime.now());
            testMapper.insertTestAssignment(ta);
        }


    }


    public List<String> getAssignedStudents(Integer testId) {
        if (testMapper.findTestById(testId) == null) {

            return Collections.emptyList();
        }

        return testMapper.getAssignedStudents(testId);
    }



    @CacheEvict(value = "student-tests", allEntries = true)
    public void assignQuestionsToTest(Integer testId, List<Integer> questionIds) {
        TestDTO test = testMapper.findTestById(testId);
        if (test == null) throw new RuntimeException("Không tìm thấy đề kiểm tra");


        int order = testQuestionMapper.countQuestionsInTest(testId) + 1;

        List<Integer> aiIds = aiGenerateQuestionService.findAllIds();

        Map<Integer, QuestionDTO> questionMap = questionService.findQuestionMapByIds(questionIds);

        for (Integer qId : questionIds) {
            QuestionDTO q = questionMap.get(qId);
            if (q == null) continue;

            String source = aiIds.contains(qId) ? "ai" : "manual";

            testQuestionMapper.insertQuestionForFixedTest(
                    testId,
                    qId,
                    q.getDifficultyId(),
                    q.getTopicId(),
                    order++,
                    source
            );
        }


    }
    @Transactional
    public Integer getOrCreateConversationId(Integer testId, String studentUsername) {
        Integer existing = testMapper.findConversationId(testId, studentUsername);
        if (existing != null) return existing;

        Integer newId = (int) (System.currentTimeMillis() % 1000000);


        testMapper.insertConversation(testId, studentUsername, newId);


        chatConversationMapper.insertConversation(newId,studentUsername);

        return newId;
    }


    public Optional<String> isTestAvailable(Integer testId, String  studentUsername) {

        TestDTO test = testMapper.findTestById(testId);
        if (test == null) {
            return Optional.of("Bài thi không tồn tại.");
        }


        if (!test.isPublished()) {
            return Optional.of("Bài thi chưa được công bố.");
        }


        LocalDateTime now = LocalDateTime.now();
        if (test.getStartTime() != null && now.isBefore(test.getStartTime())) {
            return Optional.of("Bài thi chưa đến thời điểm bắt đầu: " + test.getStartTime());
        }


        if (test.getEndTime() != null && now.isAfter(test.getEndTime())) {
            return Optional.of("Bài thi đã quá thời gian kết thúc.");
        }


        Integer maxAttempts = test.getMaxAttempts();
        if (maxAttempts != null && maxAttempts > 0) {
            Integer completedAttempts = testResultMapper.countCompletedAttempts(testId, studentUsername);
            if (completedAttempts >= maxAttempts) {
                return Optional.of("Bạn đã sử dụng hết " + maxAttempts + " lần làm bài cho bài thi này.");
            }
        }


        return Optional.empty();
    }




    @Transactional
    public Integer createDynamicTest(TestDTO test, List<TestCriteriaDTO> criteriaList) {

        test.setTestType("Dynamic");
        test.setIsDynamic(true);



        if (test.getCreatedAt() == null) {
            test.setCreatedAt(LocalDateTime.now());
        }
        test.setPublished(true);

        cleanAccessCode(test);

        testMapper.insertTest(test);


        Integer newTestId = test.getTestId();
        if (newTestId == null) {

            throw new RuntimeException("Không thể lấy ID của đề thi mới tạo.");
        }


        for (TestCriteriaDTO criteria : criteriaList) {
            criteria.setTestId(newTestId);
            testMapper.insertCriteria(criteria);
        }


        return newTestId;


    }







    public void assignQuestionsToStudents(Integer testId, List<TestCriteriaDTO> criteriaList,
                                          List<String> studentUsernames, String createdBy) {


        if (criteriaList == null || criteriaList.isEmpty()) {
            throw new IllegalArgumentException("Không thể gán đề thi động vì thiếu cấu hình tiêu chí.");
        }

        if (testQuestionMapper.countQuestionsInTest(testId) == 0) {
            System.out.println("DEBUG: Tạo câu hỏi lần đầu cho đề thi chung/fixed ID " + testId);


            String dummyStudent = studentUsernames.stream().findFirst().orElse(null);
            if (dummyStudent != null) {

                generateFixedTestQuestions(testId, createdBy, criteriaList);
            }
        }

        Set<String> uniqueStudents = new HashSet<>(studentUsernames);

        for (String studentUsername : uniqueStudents) {
            if (studentUsername == null || studentUsername.isBlank()) continue;


            try {
                TestAssignmentDTO ta = new TestAssignmentDTO();
                ta.setTestId(testId);
                ta.setStudentUsername(studentUsername);
                ta.setAssignedAt(LocalDateTime.now());
                testMapper.insertTestAssignment(ta);
            } catch (Exception e) {

                System.err.println("CẢNH BÁO: Không thể gán bài thi " + testId + " cho "
                        + studentUsername + ". Lỗi: " + e.getMessage());

            }
        }
    }



@Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void generateAndAssignTestQuestions(Integer testId,
                                                            String studentUsername, String createdBy,
                                                            List<TestCriteriaDTO> criteriaList) {


        int count = testMapper.countAssignedQuestionsForStudent(testId, studentUsername);
        if (count > 0) {
            System.out.printf("DEBUG BẢO VỆ: Đã tìm thấy %d câu hỏi được gán cho %s trong Test ID %d." +
                    " BỎ QUA GÁN LẶP LẠI.%n", count, studentUsername, testId);
            return;
        }


    List<Question> allSelectedQuestions = new ArrayList<>();
    Set<Integer> selectedQuestionIds = new HashSet<>();
        Set<Integer> aiQuestionIds = getAiQuestionIdSet();



        int totalQuestionsExpected = 0;

        for (TestCriteriaDTO criteria : criteriaList) {
            totalQuestionsExpected += criteria.getQuestionCount();

            System.out.println("DEBUG RÚT: TestID=" + testId +
                    " | Student=" + studentUsername +
                    " | TopicID=" + criteria.getTopicId() +
                    " | DiffID=" + criteria.getDifficultyId() +
                    " | Count=" + criteria.getQuestionCount());


            List<Question> randomQuestions = testMapper.findRandomQuestionsByCriteria(
                    criteria.getTopicId(),
                    criteria.getDifficultyId(),
                    criteria.getQuestionCount() * 3
            );


            int assignedCountForCriteria = 0;

            if (randomQuestions.size() < criteria.getQuestionCount()) {
                System.err.printf("LỖI CẢNH BÁO DỮ LIỆU: Rút được %d/%d câu hỏi cho T%d/D%d." +
                                " KHÔNG ĐỦ CÂU HỎI TRONG CSDL!%n",
                        randomQuestions.size(), criteria.getQuestionCount(),
                        criteria.getTopicId(), criteria.getDifficultyId());
            } else {
                System.out.printf("DEBUG KẾT QUẢ: Đã rút %d câu hỏi (dư) cho tiêu chí T%d/D%d.%n",
                        randomQuestions.size(), criteria.getTopicId(), criteria.getDifficultyId());
            }


            for (Question q : randomQuestions) {
                Integer qId = q.getQuestionId();
                if (qId == null) continue;


                boolean isUniqueGlobally = selectedQuestionIds.add(qId);

                if (!isUniqueGlobally) {
                    System.out.println("CẢNH BÁO: Câu hỏi ID " + qId +
                            " bị trùng lặp giữa các tiêu chí. Bỏ qua và tìm câu khác.");
                    continue;
                }


                if (assignedCountForCriteria >= criteria.getQuestionCount()) {

                    break;
                }


                allSelectedQuestions.add(q);

                assignedCountForCriteria++;
            }


            if (assignedCountForCriteria != criteria.getQuestionCount()) {
                System.err.printf("LỖI CẢNH BÁO DỮ LIỆU: Chỉ gán được %d/%d câu hỏi cho T%d/D%d. " +
                                "LỖI DỮ LIỆU/TRÙNG LẶP CAO!%n",
                        assignedCountForCriteria, criteria.getQuestionCount(),
                        criteria.getTopicId(), criteria.getDifficultyId());
            }
        }

    Collections.shuffle(allSelectedQuestions);
    System.out.printf("DEBUG RANDOM: Đã đảo lộn thứ tự %d câu hỏi cho %s.%n",
            allSelectedQuestions.size(), studentUsername);


    int orderNo = 1;

    for (Question q : allSelectedQuestions) {
        String source = (q.getQuestionId() != null && aiQuestionIds.contains(q.getQuestionId())) ? "ai" : "manual";


        Integer topicId = q.getTopicId();
        if (topicId == null) {
            System.err.println("CẢNH BÁO: Question ID " + q.getQuestionId() + " thiếu Topic ID. Bỏ qua.");
            continue;
        }

        testQuestionMapper.insertQuestionForStudent(
                testId,
                q.getQuestionId(),
                studentUsername,
                q.getDifficultyId(),
                topicId,
                orderNo++,
                source
        );
    }

    int assignedCount = testQuestionMapper.countAssignedQuestionsForStudent(testId, studentUsername);
    int expectedCount = allSelectedQuestions.size();
    if (assignedCount != expectedCount) {
        System.err.printf("LỖI GÁN CUỐI CÙNG: Tổng câu hỏi được chèn thực tế (%d) " +
                        "KHÔNG KHỚP với số câu hỏi duy nhất đã lọc (%d) cho Test ID %d. Lỗi giao dịch nghiêm trọng!%n",
                assignedCount, expectedCount, testId);


        if (assignedCount == 0 && expectedCount > 0) {
            throw new RuntimeException("GÁN ĐỀ THI THẤT BẠI: Đã chọn câu hỏi nhưng không chèn được vào CSDL." +
                    " Cần kiểm tra Mapper XML hoặc lỗi SQL.");
        }
    }



    try {
        TestAssignmentDTO ta = new TestAssignmentDTO();
        ta.setTestId(testId);
        ta.setStudentUsername(studentUsername);
        ta.setAssignedAt(LocalDateTime.now());
        testMapper.insertTestAssignment(ta);
    } catch (Exception e) {

        System.err.println("LỖI CHÈN TEST ASSIGNMENT: Thất bại khi chèn vào bảng test_assignments. Lỗi: "
                + e.getMessage());
        throw new RuntimeException("Lỗi nghiêm trọng khi hoàn tất gán đề thi: " + e.getMessage(), e);
    }

    }



    public List<QuestionDTO> getQuestionsForTestView(Integer testId, String studentUsernameToView) {


        TestDTO test = testMapper.findTestById(testId);


        if (test != null && test.getIsDynamic() != null && test.getIsDynamic()) {


            if (studentUsernameToView != null && !studentUsernameToView.isBlank()) {
                System.out.println("DEBUG VIEW: Đề Động (Học sinh). Lấy câu hỏi gán riêng cho: "
                        + studentUsernameToView);
                return testQuestionMapper.findDynamicQuestionsByTestIdAndStudent(testId, studentUsernameToView);
            }


            else {

                List<String> assignedStudents = testMapper.getAssignedStudents(testId);

                if (!assignedStudents.isEmpty()) {
                    String sampleStudent = assignedStudents.get(0);
                    System.out.println("DEBUG VIEW: Đề Động (Giáo viên). " +
                            "Hiển thị câu hỏi gán cho học sinh mẫu: " + sampleStudent);

                    return testQuestionMapper.findDynamicQuestionsByTestIdAndStudent(testId, sampleStudent);
                } else {

                    System.err.println("CẢNH BÁO VIEW: Đề Động. Chưa có học sinh nào được gán để lấy mẫu câu hỏi.");
                    return Collections.emptyList();
                }
            }
        }


        else {
            return testQuestionMapper.findFixedQuestionsByTestId(testId);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void generateFixedTestQuestions(Integer testId, String createdBy,
                                                        List<TestCriteriaDTO> criteriaList) {

        if (testQuestionMapper.countQuestionsInTest(testId) > 0) {
            System.out.printf("DEBUG BẢO VỆ: Đề thi Fixed ID %d đã có câu hỏi. BỎ QUA TẠO LẠI.%n", testId);
            return;
        }

        int orderNo = 1;
        Set<Integer> aiQuestionIds = getAiQuestionIdSet();
        Set<Integer> selectedQuestionIds = new HashSet<>();
        int totalQuestionsExpected = 0;

        for (TestCriteriaDTO criteria : criteriaList) {
            totalQuestionsExpected += criteria.getQuestionCount();

            System.out.println("DEBUG RÚT: TestID=" + testId + " | TopicID=" +
                    criteria.getTopicId() + " | DiffID=" + criteria.getDifficultyId() + " | Count=" +
                    criteria.getQuestionCount());

            List<Question> randomQuestions = testMapper.findRandomQuestionsByCriteria(
                    criteria.getTopicId(),
                    criteria.getDifficultyId(),
                    criteria.getQuestionCount() * 3
            );

            int assignedCountForCriteria = 0;


            if (randomQuestions.size() < criteria.getQuestionCount()) {
                System.err.printf("LỖI CẢNH BÁO DỮ LIỆU: Rút được %d/%d câu hỏi cho T%d/D%d." +
                                " KHÔNG ĐỦ CÂU HỎI TRONG CSDL!%n",
                        randomQuestions.size(), criteria.getQuestionCount(),
                        criteria.getTopicId(), criteria.getDifficultyId());
            } else {
                System.out.printf("DEBUG KẾT QUẢ: Đã rút %d câu hỏi (dư) cho tiêu chí T%d/D%d.%n",
                        randomQuestions.size(), criteria.getTopicId(), criteria.getDifficultyId());
            }

            for (Question q : randomQuestions) {
                Integer qId = q.getQuestionId();
                if (qId == null) continue;

                boolean isUniqueGlobally = selectedQuestionIds.add(qId);

                if (!isUniqueGlobally) {
                    System.out.println("CẢNH BÁO: Câu hỏi ID " + qId +
                            " bị trùng lặp giữa các tiêu chí. Bỏ qua và tìm câu khác.");
                    continue;
                }

                if (assignedCountForCriteria >= criteria.getQuestionCount()) {
                    break;
                }

                String source = (q.getQuestionId() != null &&
                        aiQuestionIds.contains(q.getQuestionId())) ? "ai" : "manual";

                testQuestionMapper.insertQuestionForFixedTest(
                        testId,
                        q.getQuestionId(),
                        q.getDifficultyId(),
                        q.getTopicId(),
                        orderNo++,
                        source
                );

                assignedCountForCriteria++;
            }

            if (assignedCountForCriteria != criteria.getQuestionCount()) {
                System.err.printf("LỖI CẢNH BÁO DỮ LIỆU: Chỉ gán được %d/%d câu hỏi cho T%d/D%d." +
                                " LỖI DỮ LIỆU/TRÙNG LẶP CAO!%n",
                        assignedCountForCriteria, criteria.getQuestionCount(),
                        criteria.getTopicId(), criteria.getDifficultyId());
            }
        }


        int assignedCount = testQuestionMapper.countQuestionsInTest(testId);
        int expectedCount = selectedQuestionIds.size();
        if (assignedCount != expectedCount) {
            System.err.printf("LỖI GÁN CUỐI CÙNG (FIXED): Tổng câu hỏi được chèn thực tế (%d)" +
                            " KHÔNG KHỚP với số câu hỏi duy nhất đã lọc (%d) cho Test ID %d.%n",
                    assignedCount, expectedCount, testId);
            if (assignedCount == 0 && expectedCount > 0) {

                throw new RuntimeException("TẠO ĐỀ FIXED THẤT BẠI: Đã chọn câu hỏi " +
                        "nhưng không chèn được vào CSDL. Kiểm tra Mapper XML.");
            }
        }
    }





}