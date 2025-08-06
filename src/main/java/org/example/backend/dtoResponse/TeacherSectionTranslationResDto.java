package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class TeacherSectionTranslationResDto {
    private UUID id;
    private String description;
    private String lang;
}
