package org.example.backend.services.lessonService;

import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.LessonGroupResDto;

import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonGroupResDto getLessons(UUID groupId);

    void deleteLesson(UUID id);

    void changeTime(UUID groupId,String startTime, String endTime);

    void markStudents(UUID groupId, List<StudentMarkDto> studentMarks);
}
