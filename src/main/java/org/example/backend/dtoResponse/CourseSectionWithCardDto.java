package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseSectionWithCardDto {
    private UUID id;
    private String title;
    private List<CourseCardDto> cards;
}
