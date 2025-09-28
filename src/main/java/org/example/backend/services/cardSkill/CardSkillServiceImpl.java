package org.example.backend.services.cardSkill;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.CardSkillResDto;
import org.example.backend.dtoResponse.CardSkillTranslationResDto;
import org.example.backend.entity.CardSkill;
import org.example.backend.entity.CardSkillTranslation;
import org.example.backend.entity.CourseCard;
import org.example.backend.entity.CourseCardTranslation;
import org.example.backend.repository.CardSkillRepo;
import org.example.backend.repository.CardSkillTranslationRepo;
import org.example.backend.repository.CourseCardRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardSkillServiceImpl implements CardSkillService {

    private final CardSkillRepo cardSkillRepo;
    private final CardSkillTranslationRepo cardSkillTranslationRepo;
    private final CourseCardRepo courseCardRepo;

    @Override
    public void addCardSkill(UUID cardId, String titleUz, String titleRu, String titleEn) {
        CardSkill cardSkill = new CardSkill();
        CourseCard courseCard = courseCardRepo.findById(cardId).get();
        cardSkill.setCourseCard(courseCard);
        CardSkill saved = cardSkillRepo.save(cardSkill);

        CardSkillTranslation uzTranslation = new CardSkillTranslation();
        uzTranslation.setTitle(titleUz);
        uzTranslation.setLanguage(Lang.UZ);
        uzTranslation.setCardSkill(saved);
        cardSkillTranslationRepo.save(uzTranslation);

        CardSkillTranslation ruTranslation = new CardSkillTranslation();
        ruTranslation.setTitle(titleRu);
        ruTranslation.setLanguage(Lang.RU);
        ruTranslation.setCardSkill(saved);
        cardSkillTranslationRepo.save(ruTranslation);

        CardSkillTranslation enTranslation = new CardSkillTranslation();
        enTranslation.setTitle(titleEn);
        enTranslation.setLanguage(Lang.EN);
        enTranslation.setCardSkill(saved);
        cardSkillTranslationRepo.save(enTranslation);

    }

    @Transactional
    @Override
    public void editCardSkill(UUID skillId, String titleUz, String titleRu, String titleEn) {
        CardSkill cardSkill = cardSkillRepo.findById(skillId)
                .orElseThrow(() -> new RuntimeException("CardSkill not found with id: " + skillId));

        cardSkill.getTranslations().forEach(translation -> {
            switch (translation.getLanguage()) {
                case UZ -> {
                    translation.setTitle(titleUz);
                }
                case RU -> {
                    translation.setTitle(titleRu);
                }
                case EN -> {
                    translation.setTitle(titleEn);
                }
            }
            cardSkillTranslationRepo.save(translation);
        });

    }

    @Override
    @Transactional
    public List<CardSkillResDto> getCardSkills(UUID id) {
        List<CardSkillResDto> cardSkillResDtos = new ArrayList<>();

        cardSkillRepo.findAllByCourseCard_Id(id).forEach(cardSkill -> {
            CardSkillResDto cardSkillResDto = new CardSkillResDto();
            cardSkillResDto.setId(cardSkill.getId());

            List<CardSkillTranslationResDto> translationResDtos = cardSkill.getTranslations()
                    .stream()
                    .map(translation->{
                        CardSkillTranslationResDto dto = new CardSkillTranslationResDto();
                        dto.setId(translation.getId());
                        dto.setTitle(translation.getTitle());
                        dto.setLang(String.valueOf(translation.getLanguage()));
                        return dto;
                    }).collect(Collectors.toList());
            cardSkillResDto.setTranslations(translationResDtos);
            cardSkillResDtos.add(cardSkillResDto);
        });

        return cardSkillResDtos;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        cardSkillRepo.deleteById(id);
    }

}