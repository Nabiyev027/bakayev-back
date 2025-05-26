package org.example.backend.services.aboutService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dto.AboutSectionDto;
import org.example.backend.entity.AboutSection;
import org.example.backend.entity.AboutSectionTranslation;
import org.example.backend.repository.AboutSectionRepo;
import org.example.backend.repository.AboutSectionTranslationRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService{
    private final AboutSectionRepo aboutSectionRepo;
    private final AboutSectionTranslationRepo aboutSectionTranslationRepo;

    @Override
    public AboutSectionDto getAbout(String lang) {
        AboutSection found = aboutSectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("About section not found"));

        AboutSectionDto aboutSectionDto = new AboutSectionDto();
        aboutSectionDto.setImgUrl(found.getImgUrl());
        aboutSectionDto.setVideoUrl(found.getVideoUrl());

        // Null bo'lmasligi va mavjud tilga tekshiruv
        if (found.getTranslations() != null) {
            found.getTranslations().stream()
                    .filter(t -> t.getLanguage().toString().equalsIgnoreCase(lang))
                    .findFirst()
                    .ifPresent(translation -> {
                        aboutSectionDto.setDescription1(translation.getDescription1());
                        aboutSectionDto.setDescription2(translation.getDescription2());
                    });
        }

        return aboutSectionDto;
    }

    @Override
    public void addAbout(MultipartFile img, String video, String description1, String description2, String lang) {
        AboutSection aboutSection = new AboutSection();
        String path = createImage(img);
        aboutSection.setImgUrl(path);
        aboutSection.setVideoUrl(video);
        AboutSection saved = aboutSectionRepo.save(aboutSection);
        AboutSectionTranslation aboutSectionTranslation = new AboutSectionTranslation();
        aboutSectionTranslation.setDescription1(description1);
        aboutSectionTranslation.setDescription2(description2);
        aboutSectionTranslation.setLanguage(Lang.valueOf(lang));
        aboutSectionTranslation.setAboutSection(saved);
        aboutSectionTranslationRepo.save(aboutSectionTranslation);

    }

    @Override
    public void editAbout(UUID id, MultipartFile img, String video, String description1, String description2, String lang) {
        AboutSection found = aboutSectionRepo.findById(id).orElseThrow(() -> new RuntimeException("About section not found"));


        // Agar yangi rasm yuborilgan bo‘lsa, almashtiramiz
        if (img != null && !img.isEmpty()) {
            String newImageUrl = replaceImage(found.getImgUrl(), img);
            found.setImgUrl(newImageUrl);
        }

        // Tarjimani yangilaymiz
        found.getTranslations().forEach(translation -> {
            if (translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setDescription1(description1);
                translation.setDescription2(description2);
                aboutSectionTranslationRepo.save(translation);
            }

        });


        aboutSectionRepo.save(found);
    }

    @Override
    public void deleteAbout(UUID id) {
        AboutSection about = aboutSectionRepo.findById(id).orElseThrow(() -> new RuntimeException("About section not found"));
        aboutSectionTranslationRepo.deleteAll(about.getTranslations());
        aboutSectionRepo.delete(about);
    }


    private String replaceImage(String oldImgUrl, MultipartFile newImg) {
        try {
            // static papkaning to‘liq yo‘lini olish
            File staticFolder = new ClassPathResource("static").getFile();

            // Eski rasmni o‘chirish
            if (oldImgUrl != null && !oldImgUrl.isEmpty()) {
                File oldImageFile = new File(staticFolder.getAbsolutePath() + oldImgUrl);
                if (oldImageFile.exists()) {
                    oldImageFile.delete();
                }
            }

            // Yangi rasmni saqlash
            return createImage(newImg);

        } catch (IOException e) {
            throw new RuntimeException("Rasmni almashtirishda xatolik yuz berdi", e);
        }
    }

    private String createImage(MultipartFile img) {
        try {
            // static/uploads papkasi joylashgan manzilni olish
            File uploadsFolder = new ClassPathResource("static/uploads/").getFile();

            // Agar papka mavjud bo'lmasa - yaratamiz
            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            // Unikal fayl nomi yaratamiz
            String uniqueFileName = UUID.randomUUID().toString() + "_" + img.getOriginalFilename();

            // Faylni to'liq yo'liga saqlaymiz
            File destination = new File(uploadsFolder, uniqueFileName);
            img.transferTo(destination);

            // Frontendda ko‘rsatish uchun nisbiy yo‘lni qaytaramiz
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Rasmni saqlab bo‘lmadi", e);
        }
    }
}
