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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService{
    private final AboutSectionRepo aboutSectionRepo;
    private final AboutSectionTranslationRepo aboutSectionTranslationRepo;

    @Transactional
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
    public void aboutPostAndUpdate(MultipartFile img, MultipartFile video, String description1, String description2, String lang) {
        // 1. Mavjud AboutSection ni olish yoki yangi yaratish
        AboutSection aboutSection = aboutSectionRepo.findTopByOrderByIdAsc()
                .orElse(new AboutSection());

        // 2. Rasm mavjud bo‘lsa, almashtirish yoki yaratish
        if (img != null && !img.isEmpty()) {
            String newImg = aboutSection.getImgUrl() != null
                    ? replaceImage(aboutSection.getImgUrl(), img)
                    : createImage(img);
            aboutSection.setImgUrl(newImg);
        }

        // 3. Video mavjud bo‘lsa, almashtirish yoki yaratish
        if (video != null && !video.isEmpty()) {
            String newVid = aboutSection.getVideoUrl() != null
                    ? replaceVideo(aboutSection.getVideoUrl(), video)
                    : createVideo(video);
            aboutSection.setVideoUrl(newVid);
        }

        // 4. AboutSection ni saqlaymiz
        AboutSection saved = aboutSectionRepo.save(aboutSection);

        // 5. Tarjima ma’lumotini olish yoki yangi yaratish
        AboutSectionTranslation translation = aboutSectionTranslationRepo
                .findByAboutSectionIdAndLanguage(saved.getId(), Lang.valueOf(lang))
                .orElse(new AboutSectionTranslation());

        translation.setDescription1(description1);
        translation.setDescription2(description2);
        translation.setLanguage(Lang.valueOf(lang));
        translation.setAboutSection(saved);

        // 6. Tarjimani saqlaymiz
        aboutSectionTranslationRepo.save(translation);
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
