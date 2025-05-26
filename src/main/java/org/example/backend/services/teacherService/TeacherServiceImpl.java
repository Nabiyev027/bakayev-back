package org.example.backend.services.teacherService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.Lang;
import org.example.backend.dto.TeacherSectionDto;
import org.example.backend.entity.TeacherSection;
import org.example.backend.entity.TeacherSectionTranslation;
import org.example.backend.repository.TeacherSectionRepo;
import org.example.backend.repository.TeacherSectionTranslationRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherSectionRepo teacherSectionRepo;
    private final TeacherSectionTranslationRepo teacherSectionTranslationRepo;

    @Override
    public void addInfo(MultipartFile img, String teacherName, String ieltsBall, String certificate, String experience, String numberOfStudents, String description, String lang) {

        TeacherSection teacherSection = new TeacherSection();
        teacherSection.setTeacherName(teacherName);
        teacherSection.setIeltsBall(ieltsBall);
        teacherSection.setCertificate(certificate);
        teacherSection.setExperience(experience);
        teacherSection.setNumberOfStudents(numberOfStudents);
        String path = createImage(img);
        teacherSection.setImgUrl(path);
        TeacherSection saved = teacherSectionRepo.save(teacherSection);

        TeacherSectionTranslation teacherSectionTranslation = new TeacherSectionTranslation();
        teacherSectionTranslation.setDescription(description);
        teacherSectionTranslation.setLanguage(Lang.valueOf(lang));
        teacherSectionTranslation.setTeacherSection(saved);
        teacherSectionTranslationRepo.save(teacherSectionTranslation);
    }

    @Override
    public void updateInfo(UUID id, MultipartFile img, String teacherName, String ieltsBall, String certificate, String experience, String numberOfStudents, String description, String lang) {

        TeacherSection teacher = teacherSectionRepo.findById(id).orElseThrow(() ->
                new RuntimeException("O‘qituvchi topilmadi: " + id));

        if (img != null && !img.isEmpty()) {
            String oldImgUrl = teacher.getImgUrl(); // Teacher entityda rasm yo‘li bo‘lishi kerak
            String newImgUrl = replaceImage(oldImgUrl, img);
            teacher.setImgUrl(newImgUrl);
        }

        teacher.setTeacherName(teacherName);
        teacher.setIeltsBall(ieltsBall);
        teacher.setCertificate(certificate);
        teacher.setExperience(experience);
        teacher.setNumberOfStudents(numberOfStudents);

        teacher.getTranslations().forEach(translation -> {
            if(translation.getLanguage().equals(Lang.valueOf(lang))) {
                translation.setDescription(description);
                teacherSectionTranslationRepo.save(translation);
            }
        });

        // Ma’lumotni saqlash
        teacherSectionRepo.save(teacher);
    }

    @Override
    public void deleteTeacher(UUID id) {
        TeacherSection teacherSection = teacherSectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("TeacherSection topilmadi: " + id));

        // Translationlarni o‘chirish
        for (TeacherSectionTranslation translation : teacherSection.getTranslations()) {
            teacherSectionTranslationRepo.deleteById(translation.getId());
        }

        // Asosiy TeacherSection ni o‘chirish
        teacherSectionRepo.deleteById(id);
    }

    @Override
    public List<TeacherSectionDto> getInfo(String lang) {
        List<TeacherSectionDto> teacherSectionDtos = new ArrayList<>();

        for (TeacherSection teacher : teacherSectionRepo.findAll()) {
            TeacherSectionDto dto = new TeacherSectionDto();
            dto.setId(teacher.getId());
            dto.setName(teacher.getTeacherName());
            dto.setIeltsBall(teacher.getIeltsBall());
            dto.setCertificate(teacher.getCertificate());
            dto.setExperience(teacher.getExperience());
            dto.setNumberOfStudents(teacher.getNumberOfStudents());

            // Tarjimani topamiz
            Optional<TeacherSectionTranslation> matchedTranslation = teacher.getTranslations()
                    .stream()
                    .filter(t -> t.getLanguage().name().equalsIgnoreCase(lang))
                    .findFirst();

            matchedTranslation.ifPresent(t -> dto.setDescription(t.getDescription()));

            teacherSectionDtos.add(dto);
        }

        return teacherSectionDtos;
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
