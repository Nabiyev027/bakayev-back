package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class LessonTypeResDto {
    private UUID id;
    private String name;
    private Boolean status;
}
