package org.example.backend.services.teacherService;

import org.example.backend.dto.TeacherSectionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TeacherService {
    void addInfo(MultipartFile img, String teacherName, String ieltsBall, String certificate, String experience, String numberOfStudents, String description, String lang);

    void updateInfo(UUID id, MultipartFile img,
                    String teacherName,
                    String ieltsBall,
                    String certificate,
                    String experience,
                    String numberOfStudents,
                    String description, String lang);

    void deleteTeacher(UUID id);

    List<TeacherSectionDto> getInfo(String lang);
}
