package org.example.backend.services.courseService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.*;
import org.example.backend.repository.CardSkillRepo;
import org.example.backend.repository.CourseCardRepo;
import org.example.backend.repository.CourseSectionRepo;
import org.example.backend.repository.CourseSectionTranslationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService{

    private final CourseSectionRepo courseSectionRepo;
    private final CourseSectionTranslationRepo courseSectionTranslationRepo;
    private final CourseCardRepo courseCardRepo;
    private final CardSkillRepo cardSkillRepo;


    @Override
    public void addCourse(String titleUz, String titleRu, String titleEn) {
        CourseSection courseSection = new CourseSection();
        CourseSection saved = courseSectionRepo.save(courseSection);

        // 2. CourseSectionTranslation yaratish
        CourseSectionTranslation translationUz = new CourseSectionTranslation();
        translationUz.setTitle(titleUz);
        translationUz.setLanguage(Lang.UZ);
        translationUz.setCourseSection(saved);

        CourseSectionTranslation translationRu = new CourseSectionTranslation();
        translationRu.setTitle(titleRu);
        translationRu.setLanguage(Lang.RU);
        translationRu.setCourseSection(saved);

        CourseSectionTranslation translationEn = new CourseSectionTranslation();
        translationEn.setTitle(titleEn);
        translationEn.setLanguage(Lang.EN);
        translationEn.setCourseSection(saved);

        courseSectionTranslationRepo.save(translationUz);
        courseSectionTranslationRepo.save(translationRu);
        courseSectionTranslationRepo.save(translationEn);
    }

    @Transactional
    @Override
    public void editCourse(UUID id, String titleUz, String titleRu, String titleEn) {
        CourseSection courseSection = courseSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseSection not found with id: " + id));

        courseSection.getTranslations().forEach(translation -> {
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
            courseSectionTranslationRepo.save(translation);
        });
    }

    @Transactional
    @Override
    public List<CourseSectionResDto> getCourses() {
        List<CourseSectionResDto> courseSectionResDtosList = new ArrayList<>();
        courseSectionRepo.findAll().forEach(courseSection -> {
            CourseSectionResDto courseSectionResDto = new CourseSectionResDto();
            courseSectionResDto.setId(courseSection.getId());

            List<CourseTranslationsResDto> translationDtos = courseSection.getTranslations()
                    .stream()
                    .map(translation -> {
                        CourseTranslationsResDto dto = new CourseTranslationsResDto();
                        dto.setId(translation.getId());
                        dto.setTitle(translation.getTitle());
                        dto.setLang(String.valueOf(translation.getLanguage()));
                        return dto;
                    }).collect(Collectors.toList());

            courseSectionResDto.setTranslations(translationDtos);

            courseSectionResDtosList.add(courseSectionResDto);
        });

        return courseSectionResDtosList;
    }

    @Override
    @Transactional
    public List<CourseSectionWithCardDto> getAllCoursesWithCard(String lang) {
        List<CourseSection> sections = courseSectionRepo.findAll();
        List<CourseSectionWithCardDto> sectionsWithCardDtos = new ArrayList<>();

        for (CourseSection section : sections) {
            CourseSectionWithCardDto course = new CourseSectionWithCardDto();
            course.setId(section.getId());

            // SECTION tarjimasini qo‘shish
            List<CourseSectionTranslation> translations = section.getTranslations();
            for (CourseSectionTranslation translation : translations) {
                Lang language = translation.getLanguage();
                if (language.name().equals(lang)) {
                    course.setTitle(translation.getTitle());
                    break;
                }
            }

            // CARD tarjimalarini ham qo‘shish
            List<CourseCard> courseCards = section.getCourseCards();
            List<CourseCardDto> cardDtos = new ArrayList<>();

            for (CourseCard card : courseCards) {
                CourseCardDto cardDto = new CourseCardDto();
                cardDto.setId(card.getId());
                cardDto.setImageUrl(card.getImageUrl());
                cardDto.setRating(card.getRating());

                List<CourseCardTranslation> cardTranslations = card.getTranslations();
                for (CourseCardTranslation cardTranslation : cardTranslations) {
                    if (lang.equals(cardTranslation.getLanguage().toString())) {
                        cardDto.setTitle(cardTranslation.getTitle());
                        break;
                    }
                }

                List<CardSkillDto> cardSkillDtos = new ArrayList<>();
                for (CardSkill cardSkill : card.getCardSkills()) {
                    CardSkillDto cardSkillDto = new CardSkillDto();
                    cardSkillDto.setId(cardSkill.getId());

                    List<CardSkillTranslation> skillTranslations = cardSkill.getTranslations();
                    for (CardSkillTranslation skillTranslation : skillTranslations) {
                        if(lang.equals(skillTranslation.getLanguage().toString())) {
                            cardSkillDto.setTitle(skillTranslation.getTitle());
                            break;
                        }
                    }

                    cardSkillDtos.add(cardSkillDto);

                }
                cardDto.setCardSkills(cardSkillDtos);
                cardDtos.add(cardDto);
            }

            course.setCards(cardDtos);
            sectionsWithCardDtos.add(course);
        }

        return sectionsWithCardDtos;
    }




    @Override
    @Transactional
    public void delete(UUID id) {
        CourseSection courseSection = courseSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseSection not found with id: " + id));

        // 1. CourseSection ichidagi CourseCardlarni olish
        courseCardRepo.findAllByCourseSectionId(courseSection.getId()).forEach(courseCard -> {

            // 2. Har bir CourseCard uchun CardSkilllarni o'chirish
            cardSkillRepo.findAllByCourseCard_Id(courseCard.getId()).forEach(cardSkill -> {
                cardSkillRepo.delete(cardSkill); // yoki cardSkillRepo.deleteById(cardSkill.getId());
            });

            deleteImage(courseCard.getImageUrl());
            // 3. CourseCard'ni o'chirish
            courseCardRepo.delete(courseCard);
        });

        // 4. CourseSection'ni o'chirish
        courseSectionRepo.delete(courseSection);
    }

    public void deleteImage(String imgUrl) {
        if (imgUrl == null || imgUrl.isBlank()) return;

        try {
            // uploads papkaga yo‘l
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File imageFile = new File(uploadDir + imgUrl.replace("/uploads", ""));

            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                if (!deleted) {
                    System.err.println("❌ Rasmni o‘chirish muvaffaqiyatsiz: " + imageFile.getAbsolutePath());
                }
            } else {
                System.err.println("⚠️ Rasm topilmadi: " + imageFile.getAbsolutePath());
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Rasmni o‘chirishda xatolik: " + e.getMessage());
        }
    }


}
