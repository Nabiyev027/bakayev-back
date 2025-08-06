package org.example.backend.services.studentService;

import org.example.backend.dtoResponse.StudentSectionResDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StudentService {
    void addStudent(MultipartFile img, String name, Double listening, Double reading, Double writing, Double speaking, Double overall);

    List<StudentSectionResDto> getStudentInfo();

    void updateStudent(UUID id, MultipartFile img, String name, Double listening, Double reading, Double writing, Double speaking, Double overall);

    void deleteStudent(UUID id);
}
