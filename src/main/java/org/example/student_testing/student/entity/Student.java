package org.example.student_testing.student.entity;

import lombok.Data;

import java.util.Date;


@Data
public class Student {

    private String studentId;

    private String username;

    private String fullName;

    private Date dob;

    private Boolean gender;

    private String email;

    private Integer courseId;

    private String status;

}
