package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseCardTranslationResDto {
    private UUID id;
    private String title;
    private String lang;
}
