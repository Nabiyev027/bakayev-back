package org.example.backend.services.headerService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.HeaderSectionDto;
import org.example.backend.dtoResponse.HeaderSectionTranslationResDto;
import org.example.backend.entity.HomeSection;
import org.example.backend.entity.HomeSectionTranslation;
import org.example.backend.repository.HeaderSectionRepo;
import org.example.backend.repository.HomeSectionTranslationRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeaderServiceImpl implements HeaderService {

    private final HeaderSectionRepo headerSectionRepo;
    private final HomeSectionTranslationRepo homeSectionTranslationRepo;

    @Override
    public void postOrEdit(MultipartFile img, String titleUz, String titleRu, String titleEn) {
        // Yagona HomeSection olish (bo‘lmasa yangi yaratamiz)
        HomeSection homeSection = headerSectionRepo.findTopByOrderByIdAsc().orElse(new HomeSection());

        // Rasm yuborilgan bo‘lsa, uni saqlaymiz va yangilaymiz
        if (img != null && !img.isEmpty()) {
            String imgPath = replaceImage(homeSection.getImgUrl(), img);
            homeSection.setImgUrl(imgPath);
        }

        HomeSection saved = headerSectionRepo.save(homeSection);

        HomeSectionTranslation uzTranslation = homeSectionTranslationRepo.findByHomeSectionIdAndLanguage(saved.getId(), Lang.UZ)
                .orElse(new HomeSectionTranslation());
        uzTranslation.setTitle(titleUz);
        uzTranslation.setLanguage(Lang.UZ);
        uzTranslation.setHomeSection(saved);
        homeSectionTranslationRepo.save(uzTranslation);

        HomeSectionTranslation ruTranslation = homeSectionTranslationRepo.findByHomeSectionIdAndLanguage(saved.getId(), Lang.RU)
                .orElse(new HomeSectionTranslation());
        ruTranslation.setTitle(titleRu);
        ruTranslation.setLanguage(Lang.RU);
        ruTranslation.setHomeSection(saved);
        homeSectionTranslationRepo.save(ruTranslation);

        HomeSectionTranslation enTranslation = homeSectionTranslationRepo.findByHomeSectionIdAndLanguage(saved.getId(), Lang.EN)
                .orElse(new HomeSectionTranslation());
        enTranslation.setTitle(titleEn);
        enTranslation.setLanguage(Lang.EN);
        enTranslation.setHomeSection(saved);
        homeSectionTranslationRepo.save(enTranslation);

    }

    @Override
    public HeaderSectionDto getHeader() {
        HomeSection homeSection = headerSectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("HomeSection da ma'lumot yo'q"));

        HeaderSectionDto headerSectionDto = new HeaderSectionDto();
        headerSectionDto.setId(homeSection.getId());
        headerSectionDto.setImgUrl(homeSection.getImgUrl());

        List<HeaderSectionTranslationResDto> translations = new ArrayList<>();
        homeSection.getTranslations().forEach(t -> {
            HeaderSectionTranslationResDto dto = new HeaderSectionTranslationResDto();
            dto.setTitle(t.getTitle());
            dto.setLang(String.valueOf(t.getLanguage())); // Agar t.getLanguage() bu enum bo‘lsa, toString() yetarli
            translations.add(dto);
        });
        headerSectionDto.setTranslations(translations);

        return headerSectionDto;
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

}
