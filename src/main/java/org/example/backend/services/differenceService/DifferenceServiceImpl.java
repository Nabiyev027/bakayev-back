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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DifferenceServiceImpl implements DifferenceService {

    private final DifferenceSectionRepo differenceSectionRepo;
    private final DifferenceSectionTranslationRepo differenceSectionTranslationRepo;

    @Override
    public void createDifference(MultipartFile img, String title, String description, String lang) {
        DifferenceSection differenceSection = new DifferenceSection();
        String path = createImage(img);
        differenceSection.setImgUrl(path);
        DifferenceSection saved = differenceSectionRepo.save(differenceSection);
        DifferenceSectionTranslation differenceSectionTranslation = new DifferenceSectionTranslation();
        differenceSectionTranslation.setTitle(title);
        differenceSectionTranslation.setDescription(description);
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
