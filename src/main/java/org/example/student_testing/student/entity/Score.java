package org.example.student_testing.student.entity;

import lombok.Data;

@Data
public class Score {

    private Integer scoreId ;

    private String studentId ;

    private String studentName ;

    private Integer courseId ;

    private String courseName ;

    private String subject;

    private Integer score;
}
