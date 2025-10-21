package org.example.student_testing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {
        "org.example.student_testing.student.mapper",
        "org.example.student_testing.test.mapper"
})
public class StudentTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentTestingApplication.class, args);
    }

}
