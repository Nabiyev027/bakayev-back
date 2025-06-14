package org.example.backend.services.courseService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.CourseSectionWithCardDto;
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
    public List<CourseSection> getAllCourses() {
        List<CourseSection> sections = courseSectionRepo.findAll();
        List<CourseSectionWithCardDto> sectionWithCardDtos = new ArrayList<>();

        sections.forEach(section -> {
            CourseSectionWithCardDto course = new CourseSectionWithCardDto();
            course.setId(section.getId());

        });

        return sections;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        CourseSection courseSection = courseSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseSection not found with id: " + id));

        courseSectionRepo.delete(courseSection);

    }


}
