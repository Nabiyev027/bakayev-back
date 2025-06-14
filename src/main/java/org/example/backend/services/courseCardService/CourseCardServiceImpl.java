package org.example.backend.services.courseCardService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.entity.CourseCard;
import org.example.backend.entity.CourseCardTranslation;
import org.example.backend.repository.CourseCardRepo;
import org.example.backend.repository.CourseCardTranslationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseCardServiceImpl implements CourseCardService {

    private final CourseCardRepo courseCardRepo;
    private final CourseCardTranslationRepo courseCardTranslationRepo;

    @Override
    public void addCourseCard(String title, String lang) {
        CourseCard courseCard = new CourseCard();
        CourseCard save = courseCardRepo.save(courseCard);
        CourseCardTranslation translation = new CourseCardTranslation();
        translation.setTitle(title);
        translation.setLanguage(Lang.valueOf(lang));
        translation.setCourseCard(save);
        courseCardTranslationRepo.save(translation);
    }

    @Override
    public void editCourseCard(UUID id, String title, String lang) {

        CourseCard courseCard = courseCardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseCard not found with id: " + id));

        courseCard.getTranslations().forEach(translation -> {
            if(translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setTitle(title);
                courseCardTranslationRepo.save(translation);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseCard> getAllCards() {
        return courseCardRepo.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        CourseCard courseCard = courseCardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseCard not found with id: " + id));

        courseCardRepo.delete(courseCard);
    }


}
