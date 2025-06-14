package org.example.backend.services.cardSkill;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.entity.CardSkill;
import org.example.backend.entity.CardSkillTranslation;
import org.example.backend.repository.CardSkillRepo;
import org.example.backend.repository.CardSkillTranslationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardSkillServiceImpl implements CardSkillService {


    private final CardSkillRepo cardSkillRepository;
    private final CardSkillTranslationRepo cardSkillTranslationRepository;

    @Override
    public void addCardSkill(String title, String lang) {
        CardSkill cardSkill = new CardSkill();
        CardSkill save = cardSkillRepository.save(cardSkill);

        CardSkillTranslation cardSkillTranslation = new CardSkillTranslation();
        cardSkillTranslation.setTitle(title);
        cardSkillTranslation.setLanguage(Lang.valueOf(lang));
        cardSkillTranslation.setCardSkill(save);
        cardSkillTranslationRepository.save(cardSkillTranslation);

    }

    @Override
    public void editCardSkill(UUID id, String title, String lang) {

        CardSkill cardSkill = cardSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));

        cardSkill.getTranslations().forEach(translation -> {
            if(translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setTitle(title);
                cardSkillTranslationRepository.save(translation);
            }
        });

    }

    @Override
    @Transactional(readOnly = true)
    public List<CardSkill> getAllCards() {
        return cardSkillRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        CardSkill cardSkill = cardSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CardSkill not found with id: " + id));

        cardSkillRepository.delete(cardSkill);
    }

}