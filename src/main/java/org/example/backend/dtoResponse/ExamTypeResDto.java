package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class ExamTypeResDto {
    private UUID id;
    private String name;
}
