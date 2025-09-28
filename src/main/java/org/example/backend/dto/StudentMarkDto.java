package org.example.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class StudentMarkDto {
    private UUID studentId;
    private UUID typeId;
    private Integer mark;
}
