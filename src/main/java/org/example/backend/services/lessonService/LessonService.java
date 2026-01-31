package org.example.backend.services.lessonService;

import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.LessonGroupResDto;
import org.example.backend.dtoResponse.LessonStudentByGroupResDto;
import org.example.backend.dtoResponse.LessonStudentResDto;

import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonGroupResDto getLessons(UUID groupId);

    void deleteLesson(UUID id);

    void changeTime(UUID groupId,String startTime, String endTime);

    void markStudents(UUID groupId, List<StudentMarkDto> studentMarks);

    List<LessonStudentByGroupResDto> getStudentLessonsByGroupIdAndUserIdAndType(UUID studentId, UUID groupId, String type);

    List<LessonStudentResDto> getStudentLessonsByGroupIdAndType(UUID groupId, String type);
}
