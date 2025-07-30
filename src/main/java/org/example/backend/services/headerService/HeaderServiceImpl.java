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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeaderServiceImpl implements HeaderService {

    private final HeaderSectionRepo headerSectionRepo;
    private final HomeSectionTranslationRepo homeSectionTranslationRepo;

    @Override
    public void postOrEdit(String title, MultipartFile img, String lang) {
        // Yagona HomeSection olish (bo‘lmasa yangi yaratamiz)
        HomeSection homeSection = headerSectionRepo.findTopByOrderByIdAsc().orElse(new HomeSection());

        // Rasm yuborilgan bo‘lsa, uni saqlaymiz va yangilaymiz
        if (img != null && !img.isEmpty()) {
            String imgPath = replaceImage(homeSection.getImgUrl(), img);
            homeSection.setImgUrl(imgPath);
        }


        HomeSection saved = headerSectionRepo.save(homeSection);

        // Tarjima bo‘yicha tekshiramiz, bo‘lmasa yangi yaratamiz
        HomeSectionTranslation translation = homeSectionTranslationRepo
                .findByHomeSectionIdAndLanguage(saved.getId(), Lang.valueOf(lang))
                .orElse(new HomeSectionTranslation());

        translation.setTitle(title);
        translation.setHomeSection(saved);
        translation.setLanguage(Lang.valueOf(lang));

        homeSectionTranslationRepo.save(translation);
    }

    @Override
    public HeaderSectionDto getHeader(String lang) {
        // Barcha HomeSection larni olib, birinchi elementni olish
        HomeSection homeSection = headerSectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("HomeSection da ma'lumot yoq"));

        HeaderSectionDto headerSectionDto = new HeaderSectionDto();
        headerSectionDto.setImgUrl(homeSection.getImgUrl());

        homeSection.getTranslations().forEach(translation -> {
            if (lang.equalsIgnoreCase(translation.getLanguage().name())) {
                headerSectionDto.setTitle(translation.getTitle());
            }
        });

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
