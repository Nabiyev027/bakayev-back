package org.example.backend.services.courseService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.CourseCardDto;
import org.example.backend.dtoResponse.CourseSectionWithCardDto;
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

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService{

    private final CourseSectionRepo courseSectionRepo;
    private final CourseSectionTranslationRepo courseSectionTranslationRepo;


    @Override
    public void addCourse(String title, String lang) {
        CourseSection courseSection = new CourseSection();
        CourseSection saved = courseSectionRepo.save(courseSection); // ID generatsiya bo'ladi

        // 2. CourseSectionTranslation yaratish
        CourseSectionTranslation translation = new CourseSectionTranslation();
        translation.setTitle(title);
        translation.setLanguage(Lang.valueOf(lang));
        translation.setCourseSection(saved); // Bog'lash

        // 3. Saqlash
        courseSectionTranslationRepo.save(translation);
    }

    @Override
    public void editCourse(UUID id, String title, String lang) {
        // 1. CourseSection ni topish
        CourseSection courseSection = courseSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseSection not found with id: " + id));

        courseSection.getTranslations().forEach(translation -> {
            if (translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setTitle(title);
                courseSectionTranslationRepo.save(translation);
            }
        });
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
