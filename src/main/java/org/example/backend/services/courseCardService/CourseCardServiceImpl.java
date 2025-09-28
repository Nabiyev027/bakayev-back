package org.example.backend.services.courseCardService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.CourseCardResDto;
import org.example.backend.dtoResponse.CourseCardTranslationResDto;
import org.example.backend.entity.CourseCard;
import org.example.backend.entity.CourseCardTranslation;
import org.example.backend.entity.CourseSection;
import org.example.backend.repository.CourseCardRepo;
import org.example.backend.repository.CourseCardTranslationRepo;
import org.example.backend.repository.CourseSectionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseCardServiceImpl implements CourseCardService {

    private final CourseCardRepo courseCardRepo;
    private final CourseCardTranslationRepo courseCardTranslationRepo;
    private final CourseSectionRepo courseSectionRepo;

    @Override
    public void addCourseCard(UUID perId, MultipartFile img, String titleUz, String titleRu, String titleEn, Integer rating) {
        // Section topiladi
        CourseSection section = courseSectionRepo.findById(perId)
                .orElseThrow(() -> new RuntimeException("Course section not found"));

        CourseCard courseCard = new CourseCard();
        courseCard.setCourseSection(section); // <-- muhim

        if (img != null && !img.isEmpty()) {
            String imageUrl = createImage(img);
            courseCard.setImageUrl(imageUrl);
        }

        courseCard.setRating((rating == null || rating == 0) ? 0 : rating);

        CourseCard saved = courseCardRepo.save(courseCard);

        CourseCardTranslation uz = new CourseCardTranslation();
        uz.setTitle(titleUz);
        uz.setCourseCard(saved);
        uz.setLanguage(Lang.UZ);
        courseCardTranslationRepo.save(uz);

        CourseCardTranslation ru = new CourseCardTranslation();
        ru.setTitle(titleRu);
        ru.setCourseCard(saved);
        ru.setLanguage(Lang.RU);
        courseCardTranslationRepo.save(ru);

        CourseCardTranslation en = new CourseCardTranslation();
        en.setTitle(titleEn);
        en.setCourseCard(saved);
        en.setLanguage(Lang.EN);
        courseCardTranslationRepo.save(en); // ← tuzatildi
    }

    @Override
    @Transactional
    public void editCourseCard(UUID id,MultipartFile img, String titleUz, String titleRu, String titleEn, Integer rating) {

        CourseCard courseCard = courseCardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseCard not found with id: " + id));

        if (img != null && !img.isEmpty()) {
            String newImageUrl = replaceImage(courseCard.getImageUrl(), img);
            courseCard.setImageUrl(newImageUrl);
        }

        courseCard.getTranslations().forEach(translation -> {
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
        });
    }

    @Override
    @Transactional
    public List<CourseCardResDto> getAllCards(UUID courseId) {
        List<CourseCardResDto> courseCardResDtosList = new ArrayList<>();


        courseCardRepo.findAllByCourseSectionId(courseId).forEach(courseCard -> {
            CourseCardResDto courseCardResDto = new CourseCardResDto();
            courseCardResDto.setId(courseCard.getId());
            courseCardResDto.setImgUrl(courseCard.getImageUrl());
            courseCardResDto.setRating(courseCard.getRating());

            List<CourseCardTranslationResDto> translationDtos = courseCard.getTranslations()
                    .stream()
                    .map(translation -> {
                        CourseCardTranslationResDto dto = new CourseCardTranslationResDto();
                        dto.setId(translation.getId());
                        dto.setTitle(translation.getTitle());
                        dto.setLang(String.valueOf(translation.getLanguage()));
                        return dto;
                    }).collect(Collectors.toList());

            courseCardResDto.setTranslations(translationDtos);

            courseCardResDtosList.add(courseCardResDto);
        });

        return courseCardResDtosList;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        CourseCard courseCard = courseCardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseCard not found with id: " + id));
        String imageUrl = courseCard.getImageUrl();

        if(imageUrl != null && !imageUrl.isEmpty()) {
            deleteImage(courseCard.getImageUrl());
        }
        courseCardRepo.delete(courseCard);
    }

    private String replaceImage(String oldImgUrl, MultipartFile newImg) {
        Optional.ofNullable(oldImgUrl)
                .filter(url -> !url.isEmpty())
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .map(fileName -> Paths.get(System.getProperty("user.dir"), "uploads", fileName))
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Eski rasmni o‘chirishda xatolik: " + e.getMessage());
                    }
                });

        return createImage(newImg);
    }

    private String createImage(MultipartFile img) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File uploadsFolder = new File(uploadDir);

            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + img.getOriginalFilename();
            File destination = new File(uploadsFolder, uniqueFileName);
            img.transferTo(destination);

            // Agar rasmlar frontend static fayllarida ko‘rsatilsa:
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace(); // Konsolda to‘liq xatoni ko‘rsatish uchun
            throw new RuntimeException("Rasmni saqlab bo‘lmadi: " + e.getMessage(), e);
        }

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
