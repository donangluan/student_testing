package org.example.student_testing.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.entity.StudentClass;

import java.util.List;

@Mapper
public interface ClassMapper {

    List<ClassDTO> findAllClasses();

    List<ClassDTO> getClassesByIds(@Param("list") List<Integer> ids);

    ClassDTO getClassById(@Param("classId") Integer classId);


    List<ClassDTO> findAll();

    ClassDTO findById(Integer classId);

    void insert(ClassDTO dto);

    void update(ClassDTO dto);

    void delete(Integer classId);
}
