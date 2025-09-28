package org.example.backend.services.cardSkill;

import org.example.backend.dtoResponse.CardSkillResDto;

import java.util.List;
import java.util.UUID;

public interface CardSkillService {
    void addCardSkill(UUID cardId,String titleUz, String titleRu, String titleEn);

    void editCardSkill(UUID skillId, String titleUz, String titleRu, String titleEn);

    List<CardSkillResDto> getCardSkills(UUID id);

    void delete(UUID id);
}
