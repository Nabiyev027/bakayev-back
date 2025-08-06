package org.example.backend.services.courseService;

import org.example.backend.dtoResponse.CourseSectionResDto;
import org.example.backend.dtoResponse.CourseSectionWithCardDto;
import org.example.backend.entity.CourseSection;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    void addCourse(String titleUz, String titleRu, String titleEn);

    void editCourse(UUID id, String titleUz, String titleRu, String titleEn);

    void delete(UUID id);

    List<CourseSectionWithCardDto> getAllCoursesWithCard(String lang);

    List<CourseSectionResDto> getCourses();

}
