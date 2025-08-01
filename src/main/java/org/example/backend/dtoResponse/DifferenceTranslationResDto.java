package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class DifferenceTranslationResDto {
    private UUID id;
    private String title;
    private String description;
    private String lang;
}
