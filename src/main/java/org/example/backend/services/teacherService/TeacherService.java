package org.example.backend.services.teacherService;

import org.example.backend.dtoResponse.TeacherSectionResDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TeacherService {
    void addInfo(MultipartFile img,
                 String firstName,
                 String lastName,
                 String ieltsBall,
                 String certificate,
                 Integer experience,
                 Integer numberOfStudents,
                 String descriptionUz,
                 String descriptionRu,
                 String descriptionEn);

    void updateInfo(UUID id, MultipartFile img,
                    String firstName,
                    String lastName,
                    String ieltsBall,
                    String certificate,
                    Integer experience,
                    Integer numberOfStudents,
                    String descriptionUz,
                    String descriptionRu,
                    String descriptionEn);

    void deleteTeacher(UUID id);

    List<TeacherSectionResDto> getTeacherSections();
}
