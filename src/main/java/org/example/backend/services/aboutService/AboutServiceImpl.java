package org.example.backend.services.aboutService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.AboutSectionResDto;
import org.example.backend.dtoResponse.AboutSectionTranslationResDto;
import org.example.backend.entity.AboutSection;
import org.example.backend.entity.AboutSectionTranslation;
import org.example.backend.repository.AboutSectionRepo;
import org.example.backend.repository.AboutSectionTranslationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService{
    private final AboutSectionRepo aboutSectionRepo;
    private final AboutSectionTranslationRepo aboutSectionTranslationRepo;

    @Transactional
    @Override
    public AboutSectionResDto getAbout() {
        AboutSection found = aboutSectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("About section not found"));

        AboutSectionResDto aboutSectionResDto = new AboutSectionResDto();
        aboutSectionResDto.setImgUrl(found.getImgUrl());
        aboutSectionResDto.setVideoUrl(found.getVideoUrl());

        List<AboutSectionTranslationResDto> translationDtos = found.getTranslations()
                .stream()
                .map(translation->{
                    AboutSectionTranslationResDto dto = new AboutSectionTranslationResDto();
                    dto.setDescription1(translation.getDescription1());
                    dto.setDescription2(translation.getDescription2());
                    dto.setLang(String.valueOf(translation.getLanguage()));
                    return dto;
                }).collect(Collectors.toList());

        aboutSectionResDto.setTranslations(translationDtos);

        return aboutSectionResDto;
    }

    @Override
    public void aboutPostAndUpdate(MultipartFile img, MultipartFile video, String description1Uz,
                                   String description1Ru, String description1En, String description2Uz,
                                   String description2Ru, String description2En) {

        AboutSection aboutSection = aboutSectionRepo.findTopByOrderByIdAsc()
                .orElse(new AboutSection());

        // 1. Rasmni yuklash yoki almashtirish
        if (img != null && !img.isEmpty()) {
            String newImg = aboutSection.getImgUrl() != null
                    ? replaceImage(aboutSection.getImgUrl(), img)
                    : createImage(img);
            aboutSection.setImgUrl(newImg);
        }

        // 2. Videoni yuklash yoki almashtirish
        if (video != null && !video.isEmpty()) {
            String newVid = aboutSection.getVideoUrl() != null
                    ? replaceVideo(aboutSection.getVideoUrl(), video)
                    : createVideo(video);
            aboutSection.setVideoUrl(newVid);
        }

        // 3. AboutSection ni saqlaymiz
        AboutSection saved = aboutSectionRepo.save(aboutSection);

        // 4. Har bir til uchun tarjimalarni saqlaymiz
        saveOrUpdateTranslation(saved, Lang.UZ, description1Uz, description2Uz);
        saveOrUpdateTranslation(saved, Lang.RU, description1Ru, description2Ru);
        saveOrUpdateTranslation(saved, Lang.EN, description1En, description2En);
    }

    private void saveOrUpdateTranslation(AboutSection section, Lang lang, String description1, String description2) {
        AboutSectionTranslation translation = aboutSectionTranslationRepo
                .findByAboutSectionIdAndLanguage(section.getId(), lang)
                .orElse(new AboutSectionTranslation());

        translation.setDescription1(description1);
        translation.setDescription2(description2);
        translation.setLanguage(lang);
        translation.setAboutSection(section);

        aboutSectionTranslationRepo.save(translation);
    }

    @Override
    public void deleteAbout(UUID id) {
        AboutSection about = aboutSectionRepo.findById(id).orElseThrow(() -> new RuntimeException("About section not found"));
        aboutSectionTranslationRepo.deleteAll(about.getTranslations());
        aboutSectionRepo.delete(about);
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

    private String replaceVideo(String oldVideoUrl, MultipartFile newVideo) {
        Optional.ofNullable(oldVideoUrl)
                .filter(url -> !url.isEmpty())
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .map(fileName -> Paths.get(System.getProperty("user.dir"), "uploads/video", fileName))
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Eski videoni o‘chirishda xatolik: " + e.getMessage());
                    }
                });

        return createVideo(newVideo);
    }

    private String createVideo(MultipartFile video) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/video";
            File uploadsFolder = new File(uploadDir);

            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + video.getOriginalFilename();
            File destination = new File(uploadsFolder, uniqueFileName);
            video.transferTo(destination);

            // Agar videolar frontend static fayllarida ko‘rsatilsa:
            return "/uploads/video/" + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace(); // Konsolda to‘liq xatoni ko‘rsatish uchun
            throw new RuntimeException("Videoni saqlab bo‘lmadi: " + e.getMessage(), e);
        }
    }
}
