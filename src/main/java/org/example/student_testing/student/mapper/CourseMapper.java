package org.example.student_testing.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student_testing.student.entity.Course;


import java.util.List;

@Mapper
public interface CourseMapper {

//    get list of courses
    List<Course> findAll();
//    get list of course by courseId
    Course findByCourseId(Integer courseId);

    void create(Course course);
    void update(Course course);
    void delete(Integer courseId);

    String findCourseNameById(int courseId);


}
