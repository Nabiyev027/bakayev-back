package org.example.backend.services.courseCardService;

import org.example.backend.dtoResponse.CourseCardResDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CourseCardService {
    void addCourseCard(UUID perId, MultipartFile img, String titleUz, String titleRu, String titleEn, Integer rating);

    void editCourseCard(UUID id, MultipartFile img,
                        String titleUz, String titleRu,
                        String titleEn, Integer rating);

    List<CourseCardResDto> getAllCards(UUID courseId);

    void delete(UUID id);
}
