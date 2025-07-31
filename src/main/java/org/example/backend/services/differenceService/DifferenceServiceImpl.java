package org.example.backend.services.differenceService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.entity.DifferenceSection;
import org.example.backend.entity.DifferenceSectionTranslation;
import org.example.backend.repository.DifferenceSectionRepo;
import org.example.backend.repository.DifferenceSectionTranslationRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DifferenceServiceImpl implements DifferenceService {

    private final DifferenceSectionRepo differenceSectionRepo;
    private final DifferenceSectionTranslationRepo differenceSectionTranslationRepo;

    @Override
    public void createDifference(MultipartFile img, String title, String description, String lang) {
        DifferenceSection differenceSection = new DifferenceSection();
        if (img != null && !img.isEmpty()) {
            String imgPath = createImage(img);
            differenceSection.setImgUrl(imgPath);
        }
        DifferenceSection saved = differenceSectionRepo.save(differenceSection);
        DifferenceSectionTranslation differenceSectionTranslation = new DifferenceSectionTranslation();
        differenceSectionTranslation.setTitle(title);
        differenceSectionTranslation.setDescription(description);
        differenceSectionTranslation.setLanguage(Lang.valueOf(lang));
        differenceSectionTranslation.setDifferenceSection(saved);
        differenceSectionTranslationRepo.save(differenceSectionTranslation);
    }

    @Override
    public void deleteRef(UUID id) {
        DifferenceSection differenceSection = differenceSectionRepo.findById(id).get();
        differenceSection.getTranslations().forEach(translation -> {
            differenceSectionTranslationRepo.deleteById(translation.getId());
        });
        differenceSectionRepo.delete(differenceSection);
    }

    @Override
    public void editDif(UUID id, MultipartFile img, String title, String description, String lang) {
        DifferenceSection differenceSection = differenceSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("HomeSection topilmadi"));

        // Agar yangi rasm yuborilgan bo‘lsa, almashtiramiz
        if (img != null && !img.isEmpty()) {
            String newImageUrl = replaceImage(differenceSection.getImgUrl(), img);
            differenceSection.setImgUrl(newImageUrl);
        }

        // Tarjimani yangilaymiz
        differenceSection.getTranslations().forEach(translation -> {
            if (translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setTitle(title);
                translation.setDescription(description);
                differenceSectionTranslationRepo.save(translation);
            }
        });


        differenceSectionRepo.save(differenceSection);

    }

    @Override
    public List<DifferenceSection> getDifference() {
        List<DifferenceSection> allDifference = differenceSectionRepo.findAll();
        return allDifference;
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
