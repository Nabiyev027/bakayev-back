package org.example.backend.services.cardSkill;

import org.example.backend.entity.CardSkill;

import java.util.List;
import java.util.UUID;

public interface CardSkillService {
    void addCardSkill(String title, String lang);

    void editCardSkill(UUID id, String title, String lang);

    List<CardSkill> getAllCards();

    void delete(UUID id);
}
