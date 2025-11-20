package org.example.student_testing.test.service;

import org.example.student_testing.test.dto.ClassDTO;
import org.example.student_testing.test.entity.StudentClass;
import org.example.student_testing.test.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassService {

    @Autowired
    private ClassMapper classMapper;

    public List<ClassDTO> getAllClasses() {
        return classMapper.findAll();
    }


    public List<ClassDTO> getClassesByIds(List<Integer> ids) {
        return classMapper.getClassesByIds(ids);
    }

    public ClassDTO getClassById(Integer id) {
        return classMapper.findById(id);
    }


    public void saveClass(ClassDTO classDTO) {
        if (classDTO.getClassId() == null) {

            classMapper.insert(classDTO);
        } else {

            classMapper.update(classDTO);
        }
    }


    public void deleteClass(Integer classId) {
        try {
            classMapper.delete(classId);
        } catch (Exception e) {

            throw new RuntimeException("Không thể xóa Lớp học ID " + classId + "" +
                    " vì có dữ liệu liên quan (Học sinh/Bài thi) đang tham chiếu.", e);
        }
    }
}
