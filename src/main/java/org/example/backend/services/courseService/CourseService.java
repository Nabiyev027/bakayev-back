package org.example.backend.services.courseService;

import org.example.backend.entity.CourseSection;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    void addCourse(String title, String lang);

    void editCourse(UUID id, String title, String lang);

    List<CourseSection> getAllCourses();

    void delete(UUID id);
}
