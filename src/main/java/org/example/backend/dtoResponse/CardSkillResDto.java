package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CardSkillResDto {
    private UUID id;
    private List<CardSkillTranslationResDto> translations;
}
