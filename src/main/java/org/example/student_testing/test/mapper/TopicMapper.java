package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student_testing.test.dto.TopicDTO;


import java.util.List;

@Mapper
public interface TopicMapper {

    List<TopicDTO> findAll();
    TopicDTO findById(@Param("topicId") Integer topicId);

    void insert(TopicDTO topicDTO);
    void update(TopicDTO topicDTO);
    void delete(@Param("topicId") Integer topicId);

    List<TopicDTO> findTopicsByCourseId(Integer courseId);

    List<TopicDTO> findTopicsByCourseIds(@Param("courseIds") List<Integer> courseIds);



}
