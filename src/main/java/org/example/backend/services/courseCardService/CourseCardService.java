package org.example.backend.services.courseCardService;

import org.example.backend.entity.CourseCard;

import java.util.List;
import java.util.UUID;

public interface CourseCardService {
    void addCourseCard(String title, String lang);

    void editCourseCard(UUID id, String title, String lang);

    List<CourseCard> getAllCards();

    void delete(UUID id);
}
