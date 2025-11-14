package org.example.student_testing.student.service;


import org.example.student_testing.student.dto.CourseDTO;
import org.example.student_testing.student.entity.Course;
import org.example.student_testing.student.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    /***
     *
     * @return
     */
    public List<CourseDTO> getAllCourse() {
        return courseMapper.findAll().
                stream()
                .map(this:: toDTO).collect(Collectors.toList())
                ;
    }

    /**
     * convert Course entity to CourseDTO
     * @param course Course entity
     * @return CourseDTO
     */
    public CourseDTO toDTO(Course course) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(course.getCourseId());
        courseDTO.setCourseName(course.getCourseName());
        courseDTO.setTeacherId(course.getTeacherId());
        courseDTO.setCreatedAt(course.getCreatedAt());
        courseDTO.setUpdatedAt(course.getUpdatedAt());
        return courseDTO;
    }

    /**
     * convert CourseDTO to Course entity
     * @param courseDTO CourseDTO
     * @return Course entity
     */
    public Course toEntity(CourseDTO courseDTO) {
        Course course = new Course();
        course.setCourseId(courseDTO.getCourseId());
        course.setCourseName(courseDTO.getCourseName());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setCreatedAt(courseDTO.getCreatedAt());
        course.setUpdatedAt(courseDTO.getUpdatedAt());
        return course;
    }

    public void createCourse(CourseDTO courseDTO) {

        courseMapper.create(toEntity(courseDTO));


    }

    public void updateCourse(CourseDTO courseDTO) {
        courseMapper.update(toEntity(courseDTO));
    }

    public void deleteCourse(Integer courseId) {
        courseMapper.delete(courseId);

    }

    public CourseDTO getCourseById(Integer courseId) {
       return toDTO(courseMapper.findByCourseId(courseId));
    }
}
