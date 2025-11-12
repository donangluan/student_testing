package org.example.student_testing;

import jakarta.servlet.http.HttpServletRequest;
import org.example.student_testing.student.entity.StudentProfile;
import org.example.student_testing.student.service.StudentProfileService;
import org.example.student_testing.test.dto.QuestionDTO;
import org.example.student_testing.test.dto.TestResultDTO;
import org.example.student_testing.test.dto.TopicScoreDTO;
import org.example.student_testing.test.mapper.TestResultMapper;
import org.example.student_testing.test.service.PracticeService;
import org.example.student_testing.test.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentProfileService studentProfileService;
    private final TestResultService testResultService;
    private final PracticeService practiceService;


    public StudentDashboardController(
            StudentProfileService studentProfileService,
            TestResultService testResultService,
            PracticeService practiceService) {
        this.studentProfileService = studentProfileService;
        this.testResultService = testResultService;
        this.practiceService = practiceService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Principal principal, Model model) {
        String username = principal.getName();


        StudentProfile profile = studentProfileService.findStudentProfileByUsername(username);


        if (profile == null) {

            profile = new StudentProfile();
            profile.setUsername(username);
            profile.setFullName(username);
        }



        Integer totalCompleted = testResultService.countCompletedTests(username);
        Double avgScore = testResultService.getAverageScore(username);


        List<String> weakTopics = testResultService.getWeakTopics(username)
                .stream()
                .limit(3)
                .map(TopicScoreDTO::getTopicName)
                .collect(Collectors.toList());

        model.addAttribute("username", username);
        model.addAttribute("profile", profile);
        model.addAttribute("totalTestsCompleted", totalCompleted);
        model.addAttribute("averageScore", avgScore);
        model.addAttribute("weakTopics", weakTopics);



        return "student/dashboard";
    }




    @GetMapping("/practice/personalized")
    public String startPersonalizedPractice(Model model, Principal principal) {
        String username = principal.getName();

        List<QuestionDTO> practiceQuestions = practiceService.generatePersonalizedPractice(username);

        if (practiceQuestions.isEmpty()) {
            model.addAttribute("errorMessage", "Hệ thống chưa có đủ dữ liệu để tạo bài luyện tập cá nhân hóa. Vui lòng làm bài thi trước.");
            return "student/error_page";
        }

        model.addAttribute("questions", practiceQuestions);
        model.addAttribute("testTitle", "Bài Luyện Tập Cá Nhân Hóa");

        return "student/practice_test";
    }


    @PostMapping("/practice/submit")
    public String submitPersonalizedPractice(
            HttpServletRequest request,
            Model model,
            Principal principal) {

        String username = principal.getName();


        Map<Integer, String> studentAnswers = processPracticeForm(request);

        if (studentAnswers.isEmpty()) {
            model.addAttribute("errorMessage", "Không nhận được đáp án nào. Vui lòng thử lại.");
            return "student/error_page";
        }


        Map<String, Object> results = practiceService.gradePracticeTest(studentAnswers);


        model.addAttribute("totalQuestions", results.get("totalQuestions"));
        model.addAttribute("correctCount", results.get("correctCount"));
        model.addAttribute("score", results.get("score"));
        model.addAttribute("detailedResults", results.get("detailedResults"));

        return "student/practice_result";
    }


    // --- 4. HIỂN THỊ LỊCH SỬ LÀM BÀI ---
    @GetMapping("/history")
    public String showTestHistory(Model model, Principal principal) {
        String username = principal.getName();

        List<TestResultDTO> history = testResultService.findHistoryByUsername(username);

        model.addAttribute("history", history);
        return "student/test_history";
    }

    // --- 5. HIỂN THỊ CHI TIẾT BÀI THI ---
    @GetMapping("/history/details/{testId}")
    public String showTestDetails(
            @PathVariable Integer testId,
            Model model,
            Principal principal) {

        String username = principal.getName();

        Map<String, Object> detailedResults = testResultService.getDetailedResult(testId, username);

        model.addAttribute("testId", testId);
        model.addAttribute("questions", detailedResults.get("questions"));

        return "student/test_details";
    }


    private Map<Integer, String> processPracticeForm(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("answer_"))
                .collect(Collectors.toMap(
                        entry -> {
                            String indexStr = entry.getKey().substring("answer_".length());
                            return Integer.parseInt(request.getParameter("questionId_" + indexStr));
                        },
                        entry -> entry.getValue()[0]
                ));
    }
}
