package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseCardDto {
    private UUID id;
    private String title;
}
