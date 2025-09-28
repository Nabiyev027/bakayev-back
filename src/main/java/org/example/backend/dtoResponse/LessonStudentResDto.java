package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LessonStudentResDto {
    private UUID id;
    private String name;
    private List<LessonStudentMarksResDto> lessonMarks;
}
