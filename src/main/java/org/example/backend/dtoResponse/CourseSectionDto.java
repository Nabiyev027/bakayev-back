package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseSectionDto {
    private UUID id;
    private String title;
}
