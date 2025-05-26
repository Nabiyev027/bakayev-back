package org.example.backend.services.studentService;

import org.example.backend.entity.StudentSection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StudentService {
    void addStudent(MultipartFile img, String name, String listening, String reading, String writing, String speaking, String overall);

    List<StudentSection> getStudentInfo();

    void updateStudent(UUID id, MultipartFile img,String name, String listening, String reading, String writing, String speaking, String overall);

    void deleteStudent(UUID id);
}
