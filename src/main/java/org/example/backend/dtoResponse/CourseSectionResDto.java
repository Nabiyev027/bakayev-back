package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseSectionResDto {
    private UUID id;
    private List<CourseTranslationsResDto> translations;
}
