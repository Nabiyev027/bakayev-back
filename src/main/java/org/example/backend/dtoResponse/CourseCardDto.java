package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseCardDto {
    private UUID id;
    private String title;
    private String imageUrl;
    private Integer rating;
    private List<CardSkillDto> cardSkills;
}
