package org.example.backend.services.courseService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.CourseCard;
import org.example.backend.entity.CourseCardTranslation;
import org.example.backend.entity.CourseSection;
import org.example.backend.entity.CourseSectionTranslation;
import org.example.backend.repository.CourseSectionRepo;
import org.example.backend.repository.CourseSectionTranslationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService{

    private final CourseSectionRepo courseSectionRepo;
    private final CourseSectionTranslationRepo courseSectionTranslationRepo;


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
                    translation.setLanguage(Lang.UZ);
                }
                case RU -> {
                    translation.setTitle(titleRu);
                    translation.setLanguage(Lang.RU);
                }
                case EN -> {
                    translation.setTitle(titleEn);
                    translation.setLanguage(Lang.EN);
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

                List<CourseCardTranslation> cardTranslations = card.getTranslations();
                for (CourseCardTranslation cardTranslation : cardTranslations) {
                    if (lang.equals(cardTranslation.getLanguage().toString())) {
                        cardDto.setTitle(cardTranslation.getTitle());
                        break;
                    }
                }

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

        courseSectionRepo.delete(courseSection);
    }


}
