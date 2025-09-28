package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class LessonStudentMarksResDto {
    private UUID id;
    private String typeName;
    private Integer mark;
}
