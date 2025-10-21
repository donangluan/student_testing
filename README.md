# 🎓 Student Testing System

Hệ thống kiểm tra học sinh trực tuyến, hỗ trợ giáo viên tạo đề, giao bài, học sinh làm bài và xem kết quả. Phù hợp cho trung tâm luyện thi, trường học, hoặc hệ thống LMS mở rộng.

---

## ✅ Chức năng theo vai trò

| Chức năng | Admin | Giáo viên | Học sinh |
|-----------|-------|-----------|----------|
| Đăng ký tài khoản | | | ✅ |
| Xác minh OTP | | | ✅ |
| Đăng nhập hệ thống | ✅ | ✅ | ✅ |
| Tạo đề kiểm tra | | ✅ | |
| Gán câu hỏi vào đề | | ✅ | |
| Gán đề cho học sinh | | ✅ | |
| Làm bài kiểm tra (Unique/Mixed) | | | ✅ |
| Trả lời câu hỏi theo độ khó động | | | ✅ |
| Xem danh sách đề đã tạo | | ✅ | |
| Xem danh sách bài kiểm tra được giao | | | ✅ |
| Xem kết quả bài làm | ✅ | ✅ | ✅ |
| Xem chi tiết từng câu đã làm | ✅ | ✅ | ✅ |
| Phân tích điểm, xếp hạng | ✅ | ✅ | ✅ |
| Quản lý người dùng | ✅ | | |
| Phân quyền truy cập | ✅ | | |
| Cấu hình bảo mật | ✅ | | |

---

## 🧠 Kiến trúc hệ thống

- **Backend**: Spring Boot
- **Database**: PostgreSQL
- **ORM**: JPA/Hibernate
- **Authentication**: JWT (hoặc session-based nếu chưa tích hợp)
- **Phân quyền**: `user_roles` (student, teacher, admin)

---

## 🗃️ Cấu trúc dữ liệu chính

- `users`, `user_roles`, `students`, `teachers`, `teacher_profiles`
- `courses`, `test_templates`, `test_template_questions`
- `questions`, `answer_options`, `test_assignments`
- `student_answers`, `test_results`
- `topics`, `difficulty_levels`, `question_types`

---

## 🚀 Hướng dẫn chạy project

```bash
# Clone repo
git clone https://github.com/donangluan/student_testing.git

# Di chuyển vào thư mục
cd student_testing

# Chạy bằng Maven
./mvnw spring-boot:run
