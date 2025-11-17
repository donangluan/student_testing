    package org.example.student_testing.test.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import jakarta.servlet.http.HttpSession;
    import org.example.student_testing.chatbot.dto.AiGeneratedQuestionDTO;
    import org.example.student_testing.chatbot.entity.AiGeneratedQuestion;
    import org.example.student_testing.chatbot.service.AiGenerateQuestionService;
    import org.example.student_testing.chatbot.service.GeminiService;
    import org.example.student_testing.student.dto.CourseDTO;
    import org.example.student_testing.student.dto.StudentDTO;
    import org.example.student_testing.student.service.CourseService;
    import org.example.student_testing.student.service.StudentService;
    import org.example.student_testing.student.service.UserService;
    import org.example.student_testing.test.dto.*;
    import org.example.student_testing.test.mapper.QuestionMapper;
    import org.example.student_testing.test.mapper.TestQuestionMapper;
    import org.example.student_testing.test.service.*;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.net.URLEncoder;
    import java.nio.charset.StandardCharsets;
    import java.security.Principal;
    import java.time.LocalDateTime;
    import java.util.*;
    import java.util.stream.Collectors;

    @Controller
    @RequestMapping("/teacher/tests")
    @PreAuthorize("hasRole('TEACHER')")
    public class TeacherController {



        @Autowired
        private TestService testService;
        @Autowired private QuestionService questionService;
        @Autowired private TopicService topicService;
        @Autowired private TestQuestionService testQuestionService;
        @Autowired private StudentService studentService;
        @Autowired private DifficultyService difficultyService;
        @Autowired
        private  CourseService courseService;

        @Autowired
        private TeacherService teacherService;

        @Autowired
        private TestSubmissionService testSubmissionService;

        @Autowired
        private GeminiService geminiService;

        @Autowired
        private AiGenerateQuestionService aiGenerateQuestionService;

        @Autowired
        private TestQuestionMapper testQuestionMapper;

        @Autowired
        private QuestionMapper questionMapper;

        @Autowired
        private  ObjectMapper objectMapper;


        @GetMapping
        public String showTestList(@AuthenticationPrincipal UserDetails userDetails,Model model) {
            model.addAttribute("tests", testService.findAll());
            model.addAttribute("studentUsername", userDetails.getUsername());
            return "teacher/test/list";
        }

        // TeacherController.java

// ...

        @GetMapping("/detail/{testId}")
        public String showTestDetail(@PathVariable Integer testId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam(value = "viewStudent", required = false) String viewStudentUsername, // 🚨 THÊM PARAM NÀY
                                     Model model) {

            TestDTO test = testService.getTestById(testId);
            if (test == null) {
                return "redirect:/teacher/tests";
            }

            List<QuestionDTO> questions;
            List<String> assignedStudents = testService.getAssignedStudents(testId);
            String studentToView = null;

            // Kiểm tra loại đề. Đề Dynamic và Unique đều gán câu hỏi riêng.
            boolean isStudentSpecificTest = test.getTestType() != null &&
                    (test.getTestType().equalsIgnoreCase("Dynamic") ||
                            test.getTestType().equalsIgnoreCase("Unique"));

            // Logic gán câu hỏi và xem đề:
            if (isStudentSpecificTest && !assignedStudents.isEmpty()) {

                // 1. Xác định học sinh cần xem đề: Ưu tiên param truyền vào (khi giáo viên chuyển đổi)
                // Nếu không có param, chọn học sinh đầu tiên làm mặc định.
                if (viewStudentUsername != null && assignedStudents.contains(viewStudentUsername)) {
                    studentToView = viewStudentUsername;
                } else {
                    studentToView = assignedStudents.get(0); // Mặc định là học sinh đầu tiên
                }

                // 2. Lấy câu hỏi SỬ DỤNG BỘ LỌC TÊN HỌC SINH (Hàm Service đã được thêm)
                // 🚨 ĐIỂM SỬA 1: GỌI HÀM SERVICE CHUNG HOẶC HÀM RIÊNG ĐƯỢC CHỨA TRONG SERVICE
                // Nếu loadDynamicTestQuestions đã được sửa, ta tiếp tục dùng nó.
                questions = testQuestionService.loadDynamicTestQuestions(testId, studentToView);

                model.addAttribute("isStudentSpecificTest", true);
                model.addAttribute("assignedStudents", assignedStudents); // Danh sách học sinh để chuyển đổi
                model.addAttribute("studentToView", studentToView);     // Học sinh đang xem

                System.out.printf("DEBUG VIEW: Đề %d (%s). Đã tải %d câu hỏi lọc theo học sinh %s.%n",
                        testId, test.getTestType(), questions.size(), studentToView);

            } else {
                // Đề Mixed, AI, hoặc loại khác (dùng bộ câu hỏi chung)
                // 🚨 ĐIỂM SỬA 2: SỬ DỤNG HÀM TẢI ĐỀ CHUNG MỚI (findFixedQuestionsByTestId)
                // Thay thế: questions = testQuestionMapper.findQuestionsByTestId(testId);

                // Giả định bạn có hàm findFixedQuestionsByTestId trong TestQuestionMapper (Đã hướng dẫn ở phần trước)
                questions = testQuestionMapper.findFixedQuestionsByTestId(testId);
                model.addAttribute("isStudentSpecificTest", false);

                System.out.printf("DEBUG VIEW: Đề %d (%s). Đã tải %d câu hỏi chung.%n",
                        testId, test.getTestType(), questions.size());
            }

            Integer conversationId = testId * 1000 + 1;

            model.addAttribute("questions", questions);
            model.addAttribute("test", test);
            model.addAttribute("conversationId", conversationId);
            model.addAttribute("testId", testId);

            // ...

            return "teacher/test/detail";
        }
        @GetMapping("/assign")
        public String showAssignForm(@RequestParam Integer testId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {

            TestDTO test = testService.getTestById(testId);
            String courseName = test.getCourseName();
            String topicName = test.getTopicName();

            List<String> assignedUsernames = testService.getAssignedStudents(testId);
            List<StudentDTO> students = studentService.findByUsernames(assignedUsernames);


            List<QuestionDTO> manualQuestions = questionService.findByCourseAndTopic(courseName, topicName);
            for (QuestionDTO q : manualQuestions) {
                q.setSource("manual");
                System.out.println("📋 Thủ công: ID = " + q.getQuestionId() + " → source = " + q.getSource());
            }


            List<AiGeneratedQuestion> aiQuestions = aiGenerateQuestionService.findByCourse(courseName);
            List<QuestionDTO> aiConvertedQuestions = questionService.convertAiQuestionsToDTO(aiQuestions);



            System.out.println("Tổng số câu hỏi AI: " + aiConvertedQuestions.size());



            model.addAttribute("testId", testId);
            model.addAttribute("students", students);
            model.addAttribute("manualQuestions", manualQuestions);
            model.addAttribute("aiQuestions", aiConvertedQuestions);
            model.addAttribute("course", courseName);
            model.addAttribute("topic", topicName);

            return "teacher/test/assign";
        }





        @PostMapping("/assign")
        public String assignQuestions(@RequestParam Integer testId,
                                      @RequestParam List<Integer> questionIds,
                                      @RequestParam String studentUsername,
                                      @RequestParam Map<String, String> questionSources,
                                      RedirectAttributes redirectAttributes) {


            testService.assignTestToStudent(testId, studentUsername);

            try {

                testQuestionService.assignQuestionsInBatch(
                        testId,
                        questionIds,
                        studentUsername,
                        questionSources
                );



                redirectAttributes.addFlashAttribute("success",
                        "Đã gán thành công " + questionIds.size() + " câu hỏi cho học sinh: " + studentUsername);

            } catch (Exception e) {
                System.err.println("Lỗi gán câu hỏi trong Controller: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi gán câu hỏi. Chi tiết: " + e.getMessage());
            }

            return "redirect:/teacher/tests/detail/" + testId;
        }



        @GetMapping("/create-mixed")
        public String showMixedTopicForm(@RequestParam(required = false) List<Integer> selectedCourseIds,
                @AuthenticationPrincipal UserDetails userDetails, Model model) {

            model.addAttribute("courses", teacherService.getAllCourses());
            model.addAttribute("selectedCourseIds", selectedCourseIds);
            model.addAttribute("groupedTopics", selectedCourseIds == null ? null : teacherService.getGroupedTopics(selectedCourseIds));
            model.addAttribute("students", teacherService.getStudents(userDetails.getUsername()));
            model.addAttribute("mixedTestDTO", new MixedTopicTestDTO());
            return "teacher/test/create-mixed";
        }

        @PostMapping("/select-courses")
        public String handleCourseSelection(@RequestParam(required = false) List<Integer> selectedCourseIds,
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            Model model) {
            return showMixedTopicForm(selectedCourseIds, userDetails, model);
        }


        @PostMapping("/create-mixed")
        public String createMixedTopicTest(@ModelAttribute MixedTopicTestDTO mixedTestDTO,
                                           @RequestParam List<String> studentUsernames,
                                           @AuthenticationPrincipal UserDetails userDetails
                                           ) {
            System.out.println("testName = " + mixedTestDTO.getTestName());
            System.out.println(" topicDistribution = " + mixedTestDTO.getTopicDistribution());
            System.out.println(" selectedCourseIds = " + mixedTestDTO.getSelectedCourseIds());

            mixedTestDTO.setCreatedBy(userDetails.getUsername());
            testService.createMixedTopicTest(mixedTestDTO, studentUsernames);
            return "redirect:/teacher/tests";
        }

        @PostMapping("/generate")
        public String generateTest(@ModelAttribute UniqueTestRequest request,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model
                                   ) {

            request.setCreatedBy(userDetails.getUsername());
            request.setTestType("Unique");

            if (request.getStudentUsername() == null || request.getStudentUsername().isEmpty()) {
                throw new IllegalArgumentException("Phải chọn ít nhất một học sinh để gán đề.");
            }


            List<QuestionDTO> selectedQuestions = questionService.previewQuestions(
                    request.getTopicId(), request.getNumberOfQuestions()
            );

            if (selectedQuestions.size() < request.getNumberOfQuestions()) {
                model.addAttribute("warning", "️ Chỉ có " + selectedQuestions.size() + " câu hỏi phù hợp với yêu cầu.");
                model.addAttribute("request", request);
                model.addAttribute("topics", topicService.findAll());
                model.addAttribute("difficultyLevels", difficultyService.findAll());
                model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
                return "teacher/test/generate";
            }
            testService.generateUniqueTest(request,userDetails.getUsername());
            return "redirect:/teacher/tests";
        }

        @GetMapping("/generate")
        public String showGenerateForm( @RequestParam(required = false) Integer courseId,
                                        @AuthenticationPrincipal UserDetails userDetails,Model model) {

            List<CourseDTO> courses = courseService.getAllCourse();
            List<TopicDTO> topics = (courseId != null)
                    ? topicService.findByCourseId(courseId)
                    : new ArrayList<>();
            model.addAttribute("courses", courses);
            model.addAttribute("topics", topics);
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("students", studentService.getStudentsForTeacher(userDetails.getUsername()));
            model.addAttribute("request", new UniqueTestRequest());

            return "teacher/test/generate";
        }



        @GetMapping("/submissions")
        public String showTestSubmissions(
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          Model model) {
            String teacherUsername = userDetails.getUsername();
            List<TestSubmissionDTO> submissions = testSubmissionService.getAllSubmissionsForTeacher(teacherUsername);
            model.addAttribute("submissions", submissions);

            return "teacher/test/submissions";
        }

        @PostMapping("/generate-ai")
        public String generateAiQuestions(@RequestParam String topic,
                                          @RequestParam String difficulty,
                                          @RequestParam Integer count,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          Model model,
                                          HttpSession session
                                          ) {

            String prompt = String.format("""
        Tạo %d câu hỏi trắc nghiệm về chủ đề '%s' dành cho học sinh lớp 8.
        Mỗi câu hỏi nên có 4 đáp án và 1 đáp án đúng. Độ khó: %s.
        Trả về JSON với cấu trúc:
        {
          "questions": [
            {
              "content": "...",
              "optionA": "...",
              "optionB": "...",
              "optionC": "...",
              "optionD": "...",
              "correctAnswer": "...",
              "difficulty": "%s",
              "topic": "%s"
            }
          ]
        }
        Chỉ trả về JSON, không thêm văn bản bên ngoài.
        """, count, topic, difficulty, difficulty, topic);

            String rawText = geminiService.chat(prompt, List.of());
            String json = geminiService.extractJsonFromText(rawText);
            List<AiGeneratedQuestion> questions = geminiService.parseQuestionsFromJson(json);
            session.setAttribute("previewQuestions", questions);

            model.addAttribute("questions", questions);
            model.addAttribute("topic", topic);
            model.addAttribute("difficulty", difficulty);
            model.addAttribute("count", count);
            return "teacher/test/review-ai";
        }

        @GetMapping("/generate-ai")
        public String showAiForm(Model model) {
            model.addAttribute("topic", "");
            model.addAttribute("difficultyLevels", List.of("Easy", "Medium", "Hard"));
            model.addAttribute("count", 5);
            return "teacher/test/generate-ai-form";
        }
        @PostMapping("/save-ai-questions")
        public String saveAiQuestions(@RequestParam Map<String, String> formData,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {

            String username = userDetails.getUsername();
            String topic = formData.get("topic");
            String difficulty = formData.get("difficulty");
            Integer testId = formData.containsKey("testId") ? Integer.parseInt(formData.get("testId")) : null;


            List<AiGeneratedQuestion> previewQuestions =
                    (List<AiGeneratedQuestion>) session.getAttribute("previewQuestions");

            if (previewQuestions == null || previewQuestions.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không có câu hỏi AI nào để lưu.");
                return "redirect:/teacher/tests/generate-ai-form";
            }


            List<Integer> selectedIndexes = formData.entrySet().stream()
                    .filter(e -> e.getKey().startsWith("selectedIndexes["))
                    .map(Map.Entry::getValue)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (selectedIndexes.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Bạn chưa chọn câu hỏi nào để lưu.");
                return "redirect:/teacher/tests/generate-ai-form";
            }


            Map<String, String> contents = new HashMap<>();
            Map<String, String> corrects = new HashMap<>();
            Map<String, String> answersA = new HashMap<>();
            Map<String, String> answersB = new HashMap<>();
            Map<String, String> answersC = new HashMap<>();
            Map<String, String> answersD = new HashMap<>();

            for (int index : selectedIndexes) {
                String key = String.valueOf(index);
                contents.put(key, formData.getOrDefault("contents[" + key + "]", ""));
                corrects.put(key, extractAnswerLetter(formData.getOrDefault("corrects[" + key + "]", "")));
                answersA.put(key, formData.getOrDefault("answers[" + key + "][A]", ""));
                answersB.put(key, formData.getOrDefault("answers[" + key + "][B]", ""));
                answersC.put(key, formData.getOrDefault("answers[" + key + "][C]", ""));
                answersD.put(key, formData.getOrDefault("answers[" + key + "][D]", ""));
            }


            List<AiGeneratedQuestion> savedQuestions = aiGenerateQuestionService.processAndSave(
                    selectedIndexes, contents, corrects, topic, difficulty,
                    answersA, answersB, answersC, answersD, username
            );

            if (savedQuestions.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không lưu được câu hỏi nào.");
                return "redirect:/teacher/tests/generate-ai-form";
            }


            if (testId != null) {
                List<Integer> questionIds = savedQuestions.stream()
                        .map(AiGeneratedQuestion::getOfficialQuestionId)
                        .filter(Objects::nonNull)
                        .toList();

                testService.assignQuestionsToTest(testId, questionIds);
                redirectAttributes.addFlashAttribute("success",
                        "Đã lưu và gán " + questionIds.size() + " câu hỏi vào đề #" + testId);
            } else {
                redirectAttributes.addFlashAttribute("success",
                        " Đã lưu thành công " + savedQuestions.size() + " câu hỏi từ AI.");
            }

            return "redirect:/teacher/tests";
        }




        private String extractAnswerLetter(String raw) {
            if (raw == null) return null;
            return raw.trim().substring(0, 1);
        }


        @PostMapping("/discard-ai-question")
        public String discardAiQuestion(@RequestParam Integer index, HttpSession session, RedirectAttributes redirectAttributes) {
            List<AiGeneratedQuestion> questions = (List<AiGeneratedQuestion>) session.getAttribute("previewQuestions");
            if (questions != null && index >= 0 && index < questions.size()) {
                questions.remove((int) index);
                session.setAttribute("previewQuestions", questions);
            }
            System.out.println("Đã nhận yêu cầu xóa câu hỏi index = " + index);

            return "redirect:/teacher/tests/review-ai";
        }

        @GetMapping("/review-ai")
        public String showReviewPage(HttpSession session, Model model) {
            List<AiGeneratedQuestion> questions = (List<AiGeneratedQuestion>) session.getAttribute("previewQuestions");
            model.addAttribute("questions", questions);
            model.addAttribute("topic", questions.isEmpty() ? "" : questions.get(0).getTopic());
            model.addAttribute("difficulty", questions.isEmpty() ? "" : questions.get(0).getDifficulty());
            model.addAttribute("count", questions.size());
            return "teacher/test/review-ai";
        }


        @PostMapping("/create-ai-test")
        public String createAiTest(@RequestParam String testName,
                                   @RequestParam String topic,
                                   @RequestParam List<Integer> questionIds,
                                   @RequestParam List<String> studentUsernames,
                                   @AuthenticationPrincipal UserDetails userDetails) {

            String teacherUsername = userDetails.getUsername();
            testService.createAiTest(testName, topic, questionIds, studentUsernames, teacherUsername);
            return "redirect:/teacher/tests";
        }


        @GetMapping("/create-dynamic")
        public String showDynamicTestForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {


            if (!model.containsAttribute("test")) {
                TestDTO testDTO = new TestDTO();


                if (testDTO.getCriteriaList().isEmpty()) {
                    testDTO.getCriteriaList().add(new TestCriteriaDTO());
                }

                model.addAttribute("test", testDTO);
            }
            List<StudentDTO> students = studentService.getStudentsForTeacher(userDetails.getUsername());
            model.addAttribute("allStudents", students);

            model.addAttribute("allTopics", topicService.findAll());
            model.addAttribute("allDifficulties", difficultyService.findAll());



            return "teacher/test/create_dynamic_form";
        }


        @PostMapping("/create-dynamic")
        public String createDynamicTest(
                @ModelAttribute("test") TestDTO testDTO,
                @RequestParam(value = "studentUsername", required = false) List<String> studentUsernames,
                @AuthenticationPrincipal UserDetails userDetails,
                RedirectAttributes redirectAttributes) {

            // 1. Lấy danh sách criteria đã được bind tự động từ form
            List<TestCriteriaDTO> criteriaListFromForm = testDTO.getCriteriaList();

            // ... (BƯỚC 2 & 3: Lọc và Kiểm tra Tiêu chí - Giữ nguyên)
            List<TestCriteriaDTO> finalCriteriaList = new ArrayList<>();
            for (TestCriteriaDTO criteria : criteriaListFromForm) {
                if (criteria.getTopicId() != null && criteria.getDifficultyId() != null &&
                        criteria.getQuestionCount() != null && criteria.getQuestionCount() > 0) {
                    finalCriteriaList.add(criteria);
                }
            }

            if (finalCriteriaList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Số lượng câu hỏi cần rút phải lớn hơn 0 hoặc tiêu chí chưa đầy đủ.");
                redirectAttributes.addFlashAttribute("test", testDTO);
                return "redirect:/teacher/tests/create-dynamic";
            }

            // ... (BƯỚC 4: Kiểm tra Học sinh - Giữ nguyên)
            if (studentUsernames == null || studentUsernames.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một học sinh để gán đề.");
                redirectAttributes.addFlashAttribute("test", testDTO);
                return "redirect:/teacher/tests/create-dynamic";
            }

            // --- BƯỚC 5: Xử lý Service (Tạo đề và Gán) ---
            try {
                String createdBy = userDetails.getUsername();
                testDTO.setCreatedBy(createdBy);

                // ==========================================================
                // 🚨 BƯỚC 5A: CHUYỂN ĐỔI CÂU HỎI AI (BỔ SUNG QUAN TRỌNG)
                // Kích hoạt tất cả câu hỏi AI đã tạo liên quan đến các chủ đề được chọn
                // thành Official Questions trong bảng 'question'.
                // ==========================================================

                Set<Integer> topicIds = finalCriteriaList.stream()
                        .map(TestCriteriaDTO::getTopicId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                for (Integer topicId : topicIds) {
                    // Lấy Course ID từ Topic ID
                    Integer courseId = topicService.getCourseIdByTopicId(topicId);

                    if (courseId != null) {
                        // 1. Tìm tất cả câu hỏi AI cho Course này
                        List<AiGeneratedQuestion> aiQuestions = aiGenerateQuestionService.findByCourseId(courseId);

                        // 2. Chuyển đổi và lưu vào bảng 'question'
                        aiGenerateQuestionService.convertAiQuestionsToOfficial(aiQuestions);
                    }
                }
                // ==========================================================
                // 🚨 KẾT THÚC BƯỚC 5A: ĐẢM BẢO DỮ LIỆU ĐÃ ĐỦ
                // ==========================================================

                // --- BƯỚC 5B: Tạo đề và lấy ID ---
                Integer newTestId = testService.createDynamicTest(testDTO, finalCriteriaList); // Rút đề từ bảng 'question' đã đầy đủ

                // Gán câu hỏi ngẫu nhiên và đề thi cho tất cả học sinh được chọn.
                testService.assignQuestionsToStudents(
                        newTestId,
                        finalCriteriaList,
                        studentUsernames,
                        createdBy
                );

                redirectAttributes.addFlashAttribute("success",
                        "Đã tạo đề thi động và gán cho " + studentUsernames.size() + " học sinh thành công.");
                return "redirect:/teacher/tests";

            } catch (Exception e) {
                // Nếu có lỗi, chuyển hướng về form và giữ lại dữ liệu đã nhập
                redirectAttributes.addFlashAttribute("error", "Lỗi trong quá trình tạo hoặc gán đề: " + e.getMessage());
                redirectAttributes.addFlashAttribute("test", testDTO);
                return "redirect:/teacher/tests/create-dynamic";
            }
        }


        @PostMapping("/add-criteria")
        public String addCriteriaRow(
                @ModelAttribute("test") TestDTO testDTO,
                @AuthenticationPrincipal UserDetails userDetails,
                Model model) {

            // Thêm một đối tượng rỗng vào danh sách hiện tại
            testDTO.getCriteriaList().add(new TestCriteriaDTO());

            // Đặt lại các thuộc tính vào Model để Thymeleaf render lại form
            model.addAttribute("test", testDTO);
            model.addAttribute("allTopics", topicService.findAll());
            model.addAttribute("allDifficulties", difficultyService.findAll());
            List<StudentDTO> students = studentService.getStudentsForTeacher(userDetails.getUsername());
            model.addAttribute("allStudents", students);

            // Cần forward (trả về tên view) thay vì redirect để giữ ModelAttributes
            return "teacher/test/create_dynamic_form";
        }

        @PostMapping("/remove-criteria")
        public String removeCriteriaRow(
                @RequestParam("removeIndex") Integer index, // Nhận chỉ mục cần xóa
                @ModelAttribute("test") TestDTO testDTO,RedirectAttributes redirectAttributes,
                @AuthenticationPrincipal UserDetails userDetails,

                Model model) {
// Chỉ xóa nếu danh sách có nhiều hơn 1 phần tử
            List<TestCriteriaDTO> criteriaList = testDTO.getCriteriaList();

            // Lấy danh sách học sinh (cần cho cả hai nhánh if/else)
            List<StudentDTO> students = studentService.getStudentsForTeacher(userDetails.getUsername());


            if (criteriaList.size() > 1 && index != null && index >= 0 && index < criteriaList.size()) {
                criteriaList.remove(index.intValue());

                // Đặt lại các thuộc tính vào Model để Thymeleaf render lại form
                model.addAttribute("test", testDTO);
                model.addAttribute("allTopics", topicService.findAll());
                model.addAttribute("allDifficulties", difficultyService.findAll());

                // 🚨 BỔ SUNG: Phải thêm danh sách học sinh
                model.addAttribute("allStudents", students);

                return "teacher/test/create_dynamic_form";
            } else if (criteriaList.size() == 1) {
                // Nếu cố gắng xóa dòng cuối cùng, redirect với thông báo lỗi
                redirectAttributes.addFlashAttribute("error", "Phải có ít nhất một tiêu chí câu hỏi.");
                // Khi redirect, phải truyền lại testDTO để giữ lại dữ liệu form
                redirectAttributes.addFlashAttribute("test", testDTO);

                // 🚨 KHI REDIRECT, studentService.getStudentsForTeacher() phải được chạy lại trong showDynamicTestForm
                return "redirect:/teacher/tests/create-dynamic";
            }

            // Trường hợp lỗi khác, mặc định trả về view
            model.addAttribute("test", testDTO);
            model.addAttribute("allTopics", topicService.findAll());
            model.addAttribute("allDifficulties", difficultyService.findAll());
            model.addAttribute("allStudents", students);

            return "teacher/test/create_dynamic_form";
        }


    }
