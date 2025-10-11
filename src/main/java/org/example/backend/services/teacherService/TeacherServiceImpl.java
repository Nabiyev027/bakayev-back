package org.example.backend.services.teacherService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dtoResponse.TeacherSectionDataResDto;
import org.example.backend.dtoResponse.TeacherSectionImgResDto;
import org.example.backend.dtoResponse.TeacherSectionResDto;
import org.example.backend.dtoResponse.TeacherSectionTranslationResDto;
import org.example.backend.entity.TeacherSection;
import org.example.backend.entity.TeacherSectionTranslation;
import org.example.backend.repository.TeacherSectionRepo;
import org.example.backend.repository.TeacherSectionTranslationRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
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
public class TeacherServiceImpl implements TeacherService {

    private final TeacherSectionRepo teacherSectionRepo;
    private final TeacherSectionTranslationRepo teacherSectionTranslationRepo;

    @Override
    public void addInfo(MultipartFile img, String firstName, String lastName, String ieltsBall, String certificate, Integer experience, Integer numberOfStudents, String descriptionUz, String descriptionRu, String descriptionEn) {
        TeacherSection teacherSection = new TeacherSection();
        teacherSection.setFirstName(firstName);
        teacherSection.setLastName(lastName);
        teacherSection.setIeltsBall(ieltsBall);
        teacherSection.setCertificate(certificate);
        teacherSection.setExperience(experience);
        teacherSection.setNumberOfStudents(numberOfStudents);
        if (img != null && !img.isEmpty()) {
            String imgPath = createImage(img);
            teacherSection.setImgUrl(imgPath);
        }
        TeacherSection saved = teacherSectionRepo.save(teacherSection);

        TeacherSectionTranslation uzTranslation = new TeacherSectionTranslation();
        uzTranslation.setDescription(descriptionUz);
        uzTranslation.setLanguage(Lang.UZ);
        uzTranslation.setTeacherSection(saved);
        teacherSectionTranslationRepo.save(uzTranslation);

        TeacherSectionTranslation ruTranslation = new TeacherSectionTranslation();
        ruTranslation.setDescription(descriptionRu);
        ruTranslation.setLanguage(Lang.RU);
        ruTranslation.setTeacherSection(saved);
        teacherSectionTranslationRepo.save(ruTranslation);

        TeacherSectionTranslation enTranslation = new TeacherSectionTranslation();
        enTranslation.setDescription(descriptionEn);
        enTranslation.setLanguage(Lang.EN);
        enTranslation.setTeacherSection(saved);
        teacherSectionTranslationRepo.save(enTranslation);

    }

    @Transactional
    @Override
    public void updateInfo(UUID id, MultipartFile img, String firstname, String lastName, String ieltsBall, String certificate, Integer experience, Integer numberOfStudents, String descriptionUz, String descriptionRu, String descriptionEn) {

        TeacherSection teacher = teacherSectionRepo.findById(id).orElseThrow(() ->
                new RuntimeException("O‘qituvchi topilmadi: " + id));

        if (img != null && !img.isEmpty()) {
            String oldImgUrl = teacher.getImgUrl(); // Teacher entityda rasm yo‘li bo‘lishi kerak
            String newImgUrl = replaceImage(oldImgUrl, img);
            teacher.setImgUrl(newImgUrl);
        }

        teacher.setFirstName(firstname);
        teacher.setLastName(lastName);
        teacher.setIeltsBall(ieltsBall);
        teacher.setCertificate(certificate);
        teacher.setExperience(experience);
        teacher.setNumberOfStudents(numberOfStudents);

        teacher.getTranslations().forEach(translation->{
            switch (translation.getLanguage()) {
                case UZ -> {
                    translation.setDescription(descriptionUz);
                }
                case RU -> {
                    translation.setLanguage(Lang.RU);
                }
                case EN -> {
                    translation.setDescription(descriptionEn);
                }
            }
            teacherSectionTranslationRepo.save(translation);
        });


        teacherSectionRepo.save(teacher);
    }

    @Transactional
    @Override
    public void deleteTeacher(UUID id) {
        TeacherSection teacherSection = teacherSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("TeacherSection topilmadi: " + id));

        for (TeacherSectionTranslation translation : teacherSection.getTranslations()) {
            teacherSectionTranslationRepo.deleteById(translation.getId());
        }
        deleteImage(teacherSection.getImgUrl());
        teacherSectionRepo.deleteById(id);
    }

    @Transactional
    @Override
    public List<TeacherSectionResDto> getTeacherSections() {
        List<TeacherSectionResDto> teacherSectionDtos = new ArrayList<>();

        for (TeacherSection teacher : teacherSectionRepo.findAll()) {
            TeacherSectionResDto dto = new TeacherSectionResDto();
            dto.setId(teacher.getId());
            dto.setImgUrl(teacher.getImgUrl());
            dto.setFirstName(teacher.getFirstName());
            dto.setLastName(teacher.getLastName());
            dto.setIeltsBall(teacher.getIeltsBall());
            dto.setCertificate(teacher.getCertificate());
            dto.setExperience(teacher.getExperience());
            dto.setNumberOfStudents(teacher.getNumberOfStudents());

            List<TeacherSectionTranslationResDto> translationDtos = teacher.getTranslations()
                            .stream()
                                    .map(translation->{
                                        TeacherSectionTranslationResDto dto1 = new TeacherSectionTranslationResDto();
                                        dto1.setId(translation.getId());
                                        dto1.setDescription(translation.getDescription());
                                        dto1.setLang(String.valueOf(translation.getLanguage()));
                                        return dto1;
                                    }).collect(Collectors.toList());

            dto.setTranslations(translationDtos);

            teacherSectionDtos.add(dto);
        }

        return teacherSectionDtos;
    }

    @Override
    public List<TeacherSectionImgResDto> getTeacherSectionsWithImage() {
        List<TeacherSectionImgResDto> teacherSectionImgResDtos = new ArrayList<>();
        List<TeacherSection> all = teacherSectionRepo.findAll();
        all.forEach(teacherSection -> {
           TeacherSectionImgResDto dto = new TeacherSectionImgResDto();
           dto.setId(teacherSection.getId());
           dto.setImageUrl(teacherSection.getImgUrl());
           dto.setName(teacherSection.getFirstName() + " " + teacherSection.getLastName());
           dto.setIeltsBall(teacherSection.getIeltsBall());
           teacherSectionImgResDtos.add(dto);
        });

        return teacherSectionImgResDtos;

    }

    @Transactional
    @Override
    public TeacherSectionDataResDto getTeacherSectionsData(UUID teacherId, String lang) {
        TeacherSection teacherSection = teacherSectionRepo.findById(teacherId).get();

        TeacherSectionDataResDto dto = new TeacherSectionDataResDto();
        dto.setId(teacherSection.getId());
        dto.setFirstName(teacherSection.getFirstName());
        dto.setLastName(teacherSection.getLastName());
        dto.setIeltsBall(teacherSection.getIeltsBall());
        dto.setCertificate(teacherSection.getCertificate());
        dto.setExperience(teacherSection.getExperience());
        dto.setNumberOfStudents(teacherSection.getNumberOfStudents());
        dto.setImgUrl(teacherSection.getImgUrl());

        for (TeacherSectionTranslation translation : teacherSection.getTranslations()) {
            if (translation.getLanguage().toString().equals(lang)) {
                dto.setDescription(translation.getDescription());
            }
        }

        return dto;

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
