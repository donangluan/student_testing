package org.example.student_testing.test.service;


import org.example.student_testing.test.dto.TopicDTO;
import org.example.student_testing.test.mapper.TopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private TopicMapper topicMapper;

    public List<TopicDTO> findAll() {
        return topicMapper.findAll();
    }

    public TopicDTO findById(Integer difficultyId) {
        return topicMapper.findById(difficultyId);
    }

    public void insert(TopicDTO dto) {
        topicMapper.insert(dto);
    }

    public void update(TopicDTO dto) {
        topicMapper.update(dto);
    }

    public void delete(Integer id) {
        topicMapper.delete(id);
    }


    public Map<Integer, String> findAllAsMap() {
        List<TopicDTO> list = topicMapper.findAll();
        return list.stream().collect(Collectors.toMap(TopicDTO::getTopicId, TopicDTO::getTopicName));
    }

    public List<TopicDTO> findByCourseId(Integer courseId) {
        return topicMapper.findTopicsByCourseId(courseId);
    }
    public List<TopicDTO> findTopicsByCourseIds(List<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return new ArrayList<>();
        return topicMapper.findTopicsByCourseIds(courseIds);
    }


    public Integer getIdByName(String name) {
        return topicMapper.findIdByName(name);
    }

    public Integer getCourseIdByTopicId(Integer topicId) {
        return topicMapper.findCourseIdByTopicId(topicId);
    }

}
