package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseCardWithSkillDto {
    private UUID id;
    private String title;
    private List<CardSkillDto> skills;
}
