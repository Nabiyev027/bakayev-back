package org.example.backend.services.headerService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dto.HeaderSectionDto;
import org.example.backend.entity.HomeSection;
import org.example.backend.entity.HomeSectionTranslation;
import org.example.backend.repository.HeaderSectionRepo;
import org.example.backend.repository.HomeSectionTranslationRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeaderServiceImpl implements HeaderService {

    private final HeaderSectionRepo headerSectionRepo;
    private final HomeSectionTranslationRepo homeSectionTranslationRepo;

    @Override
    public void postTitle(String title, MultipartFile img, String lang) {
        HomeSection homeSection = new HomeSection();
        String imgPath = createImage(img);
        homeSection.setImgUrl(imgPath);
        HomeSection saved = headerSectionRepo.save(homeSection);
        HomeSectionTranslation homeSectionTranslation = new HomeSectionTranslation();
        homeSectionTranslation.setTitle(title);
        homeSectionTranslation.setHomeSection(saved);
        homeSectionTranslation.setLanguage(Lang.valueOf(lang));
        homeSectionTranslationRepo.save(homeSectionTranslation);
    }

    @Override
    public void editTitle(UUID id, String title, MultipartFile img, String lang) {
        HomeSection homeSection = headerSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("HomeSection topilmadi"));

        // Agar yangi rasm yuborilgan bo‘lsa, almashtiramiz
        if (img != null && !img.isEmpty()) {
            String newImageUrl = replaceImage(homeSection.getImgUrl(), img);
            homeSection.setImgUrl(newImageUrl);
        }

        // Tarjimani yangilaymiz
        homeSection.getTranslations().forEach(translation -> {
            if (translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setTitle(title);
            }
        });

        headerSectionRepo.save(homeSection);
    }

    @Override
    public HeaderSectionDto getHeader(UUID id, String lang) {
        HomeSection homeSection = headerSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("HomeSection topilmadi"));
        HeaderSectionDto headerSectionDto = new HeaderSectionDto();

        headerSectionDto.setImgUrl(homeSection.getImgUrl());

        homeSection.getTranslations().forEach(translation -> {
            if (translation.getLanguage().equals(Lang.valueOf(lang))) {
                headerSectionDto.setTitle(translation.getTitle());
            }
        });

        return headerSectionDto;

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
