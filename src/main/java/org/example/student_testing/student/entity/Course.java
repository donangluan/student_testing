package org.example.student_testing.student.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Course {


    private Integer courseId;
    private String courseName;
    private String teacherId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
