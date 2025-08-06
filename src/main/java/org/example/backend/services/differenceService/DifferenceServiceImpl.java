package org.example.backend.services.differenceService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.DifferenceResDto;
import org.example.backend.dtoResponse.DifferenceTranslationResDto;
import org.example.backend.entity.DifferenceSection;
import org.example.backend.entity.DifferenceSectionTranslation;
import org.example.backend.repository.DifferenceSectionRepo;
import org.example.backend.repository.DifferenceSectionTranslationRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DifferenceServiceImpl implements DifferenceService {
    private final DifferenceSectionRepo differenceSectionRepo;
    private final DifferenceSectionTranslationRepo differenceSectionTranslationRepo;

    @Override
    public void createDifference(MultipartFile img,
                                 String titleUz, String descriptionUz,
                                 String titleRu, String descriptionRu,
                                 String titleEn, String descriptionEn) {

        // DifferenceSection ni yaratish
        DifferenceSection differenceSection = new DifferenceSection();
        if (img != null && !img.isEmpty()) {
            String imgPath = createImage(img);
            differenceSection.setImgUrl(imgPath);
        }
        DifferenceSection saved = differenceSectionRepo.save(differenceSection);

        // O'zbek tili uchun tarjima
        DifferenceSectionTranslation uzTranslation = new DifferenceSectionTranslation();
        uzTranslation.setTitle(titleUz);
        uzTranslation.setDescription(descriptionUz);
        uzTranslation.setLanguage(Lang.UZ);
        uzTranslation.setDifferenceSection(saved);
        differenceSectionTranslationRepo.save(uzTranslation);

        // Rus tili uchun tarjima
        DifferenceSectionTranslation ruTranslation = new DifferenceSectionTranslation();
        ruTranslation.setTitle(titleRu);
        ruTranslation.setDescription(descriptionRu);
        ruTranslation.setLanguage(Lang.RU);
        ruTranslation.setDifferenceSection(saved);
        differenceSectionTranslationRepo.save(ruTranslation);

        // Ingliz tili uchun tarjima
        DifferenceSectionTranslation enTranslation = new DifferenceSectionTranslation();
        enTranslation.setTitle(titleEn);
        enTranslation.setDescription(descriptionEn);
        enTranslation.setLanguage(Lang.EN);
        enTranslation.setDifferenceSection(saved);
        differenceSectionTranslationRepo.save(enTranslation);
    }

    @Transactional
    @Override
    public void deleteRef(UUID id) {
        DifferenceSection differenceSection = differenceSectionRepo.findById(id).get();
        differenceSection.getTranslations().forEach(translation -> {
            differenceSectionTranslationRepo.deleteById(translation.getId());
        });
        deleteImage(differenceSection.getImgUrl());
        differenceSectionRepo.delete(differenceSection);
    }

    @Transactional
    @Override
    public void editDif(UUID id, MultipartFile img, String titleUz, String descriptionUz,
                        String titleRu, String descriptionRu,
                        String titleEn, String descriptionEn) {

        DifferenceSection differenceSection = differenceSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("HomeSection topilmadi"));

        // 🔄 Yangi rasm bo‘lsa almashtiramiz, bo‘lmasa eski rasmni qayta yozamiz
        if (img != null && !img.isEmpty()) {
            String newImageUrl = replaceImage(differenceSection.getImgUrl(), img);
            differenceSection.setImgUrl(newImageUrl);
        }

        // Tarjimalarni yangilash
        differenceSection.getTranslations().forEach(translation -> {
            switch (translation.getLanguage()) {
                case UZ -> {
                    translation.setTitle(titleUz);
                    translation.setDescription(descriptionUz);
                }
                case RU -> {
                    translation.setTitle(titleRu);
                    translation.setDescription(descriptionRu);
                }
                case EN -> {
                    translation.setTitle(titleEn);
                    translation.setDescription(descriptionEn);
                }
            }
            differenceSectionTranslationRepo.save(translation);
        });

        differenceSectionRepo.save(differenceSection);
    }

    @Transactional
    @Override
    public List<DifferenceResDto> getDifference() {
        List<DifferenceResDto> differenceResDtoList = new ArrayList<>();
        differenceSectionRepo.findAll().forEach(differenceSection -> {
            DifferenceResDto differenceResDto = new DifferenceResDto();
            differenceResDto.setId(differenceSection.getId());
            differenceResDto.setImgUrl(differenceSection.getImgUrl());

            List<DifferenceTranslationResDto> translationDtos = differenceSection.getTranslations()
                    .stream()
                    .map(translation -> {
                        DifferenceTranslationResDto dto = new DifferenceTranslationResDto();
                        dto.setId(translation.getId());
                        dto.setTitle(translation.getTitle());
                        dto.setDescription(translation.getDescription());
                        dto.setLang(String.valueOf(translation.getLanguage()));
                        return dto;
                    }).collect(Collectors.toList());

            differenceResDto.setDifferenceTranslationResDtos(translationDtos);

            differenceResDtoList.add(differenceResDto);
        });

        return differenceResDtoList;
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
