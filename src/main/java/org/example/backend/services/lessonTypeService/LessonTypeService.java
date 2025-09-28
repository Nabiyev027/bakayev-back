package org.example.backend.services.lessonTypeService;

import org.example.backend.dtoResponse.LessonTypeResDto;

import java.util.List;
import java.util.UUID;

public interface LessonTypeService {

    List<LessonTypeResDto> getLessonTypes();

    void postLessonType(String typeName);

    void deleteType(UUID lessonTypeId);

}
