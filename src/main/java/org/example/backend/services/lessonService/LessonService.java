package org.example.backend.services.lessonService;

import org.example.backend.entity.Lesson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface LessonService {
    List<Lesson> getLessons(UUID groupId);

    void postLesson(UUID groupId, String lessonType);

    void editLesson(UUID lessonId, String lessonType);

    void deletelesson(UUID id);
}
